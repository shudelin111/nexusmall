package com.nexusmall.logistics.infrastructure.persistence;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nexusmall.logistics.config.ExpressApiConfig;
import com.nexusmall.logistics.domain.entity.LogisticsTrack;
import com.nexusmall.logistics.domain.enums.TrackStatusEnum;
import com.nexusmall.logistics.application.service.ExpressTrackService;
import com.nexusmall.logistics.application.service.LogisticsTrackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 第三方物流查询服务实现类
 * <p>
 * 业界标准?
 * - 支持快递鸟和快递100两家主流服务?
 * - 策略模式：根据配置自动选择服务?
 * - 失败降级：主服务商失败时切换到备用服务商
 * </p>
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpressTrackServiceImpl implements ExpressTrackService {

    private final ExpressApiConfig expressApiConfig;
    private final LogisticsTrackService logisticsTrackService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<LogisticsTrack> queryExpressTrack(String expressCompany, String expressNo) {
        log.info("【查询物流轨迹】expressCompany={}, expressNo={}", expressCompany, expressNo);

        try {
            // 1. 根据配置选择服务?
            String provider = expressApiConfig.getDefaultProvider();
            List<LogisticsTrack> tracks;

            if ("kuaidi100".equals(provider) && expressApiConfig.getKuaidi100().isEnabled()) {
                tracks = queryByKuaiDi100(expressCompany, expressNo);
            } else {
                // 默认使用快递鸟
                tracks = queryByKuaiDiNiao(expressCompany, expressNo);
            }

            // 2. 如果查询失败，尝试备用服务商
            if (tracks == null || tracks.isEmpty()) {
                log.warn("【查询物流轨迹】主服务商查询失败，尝试备用服务商");
                tracks = queryByBackupProvider(expressCompany, expressNo);
            }

            // 3. 同步到数据库
            if (tracks != null && !tracks.isEmpty()) {
                log.info("【查询物流轨迹成功】count={}", tracks.size());
                return tracks;
            } else {
                log.warn("【查询物流轨迹】未查询到轨迹信息");
                return Collections.emptyList();
            }

        } catch (Exception e) {
            log.error("【查询物流轨迹失败】expressCompany={}, expressNo={}", expressCompany, expressNo, e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean subscribeExpressTrack(String expressCompany, String expressNo, String callbackUrl) {
        log.info("【订阅物流轨迹】expressCompany={}, expressNo={}, callbackUrl={}", 
                expressCompany, expressNo, callbackUrl);

        try {
            String provider = expressApiConfig.getDefaultProvider();
            
            if ("kuaidi100".equals(provider) && expressApiConfig.getKuaidi100().isEnabled()) {
                return subscribeByKuaiDi100(expressCompany, expressNo, callbackUrl);
            } else {
                return subscribeByKuaiDiNiao(expressCompany, expressNo, callbackUrl);
            }
        } catch (Exception e) {
            log.error("【订阅物流轨迹失败】", e);
            return false;
        }
    }

    @Override
    public boolean unsubscribeExpressTrack(String expressCompany, String expressNo) {
        log.info("【取消订阅物流轨迹】expressCompany={}, expressNo={}", expressCompany, expressNo);
        
        // 快递鸟和快递100都不需要显式取消订阅，订阅会自动过期
        return true;
    }

    /**
     * 使用快递鸟查询物流轨迹
     * <p>
     * API文档：http://www.kdniao.com/api-track
     * </p>
     */
    private List<LogisticsTrack> queryByKuaiDiNiao(String expressCompany, String expressNo) {
        try {
            ExpressApiConfig.KuaiDiNiao config = expressApiConfig.getKuaidiniao();
            
            // 1. 构建请求参数
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("OrderCode", "");
            requestData.put("ShipperCode", getShipperCode(expressCompany));
            requestData.put("LogisticCode", expressNo);

            String requestDataJson = JSON.toJSONString(requestData);
            String dataSign = encrypt(requestDataJson, config.getApiKey(), "UTF-8");

            Map<String, String> params = new HashMap<>();
            params.put("RequestData", URLEncoder.encode(requestDataJson, "UTF-8"));
            params.put("EBusinessID", config.getBusinessId());
            params.put("RequestType", "1002"); // 即时查询
            params.put("DataSign", URLEncoder.encode(dataSign, "UTF-8"));
            params.put("DataType", "2"); // JSON格式

            // 2. 发送HTTP请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<String> entity = new HttpEntity<>(buildFormParams(params), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    config.getApiUrl(), entity, String.class);

            // 3. 解析响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JSONObject result = JSON.parseObject(response.getBody());
                
                // 检查是否成功
                if ("true".equals(result.getString("Success"))) {
                    return parseKuaiDiNiaoTracks(result.getJSONArray("Traces"), expressNo);
                } else {
                    log.warn("【快递鸟查询失败】Reason={}", result.getString("Reason"));
                }
            }

            return Collections.emptyList();

        } catch (Exception e) {
            log.error("【快递鸟查询异常】", e);
            return Collections.emptyList();
        }
    }

    /**
     * 使用快递100查询物流轨迹
     * <p>
     * API文档：https://www.kuaidi100.com/openapi/api_poll.shtml
     * </p>
     */
    private List<LogisticsTrack> queryByKuaiDi100(String expressCompany, String expressNo) {
        try {
            ExpressApiConfig.KuaiDi100 config = expressApiConfig.getKuaidi100();
            
            // 1. 构建请求参数
            String url = String.format("%s?customer=%s&sign=%s&param={\"com\":\"%s\",\"num\":\"%s\"}",
                    config.getApiUrl(),
                    config.getCustomerId(),
                    generateSign(expressCompany, expressNo, config.getApiKey()),
                    getKuaidi100ComCode(expressCompany),
                    expressNo);

            // 2. 发送HTTP请求
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            // 3. 解析响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JSONObject result = JSON.parseObject(response.getBody());
                
                // 检查是否成功
                if ("200".equals(result.getString("returnCode"))) {
                    return parseKuaiDi100Tracks(result.getJSONArray("data"), expressNo);
                } else {
                    log.warn("【快递100查询失败】message={}", result.getString("message"));
                }
            }

            return Collections.emptyList();

        } catch (Exception e) {
            log.error("【快递100查询异常】", e);
            return Collections.emptyList();
        }
    }

    /**
     * 使用备用服务商查询
     */
    private List<LogisticsTrack> queryByBackupProvider(String expressCompany, String expressNo) {
        String provider = expressApiConfig.getDefaultProvider();
        
        if ("kuaidiniao".equals(provider)) {
            // 主用快递鸟，备用快递100
            if (expressApiConfig.getKuaidi100().isEnabled()) {
                return queryByKuaiDi100(expressCompany, expressNo);
            }
        } else {
            // 主用快递100，备用快递鸟
            return queryByKuaiDiNiao(expressCompany, expressNo);
        }
        
        return Collections.emptyList();
    }

    /**
     * 快递鸟订阅物流轨迹
     */
    private boolean subscribeByKuaiDiNiao(String expressCompany, String expressNo, String callbackUrl) {
        try {
            ExpressApiConfig.KuaiDiNiao config = expressApiConfig.getKuaidiniao();
            
            // 1. 构建请求参数
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("OrderCode", "");
            requestData.put("ShipperCode", getShipperCode(expressCompany));
            requestData.put("LogisticCode", expressNo);
            requestData.put("PayType", "0"); // 运费支付方式?=现付
            requestData.put("Callback", callbackUrl);

            String requestDataJson = JSON.toJSONString(requestData);
            String dataSign = encrypt(requestDataJson, config.getApiKey(), "UTF-8");

            Map<String, String> params = new HashMap<>();
            params.put("RequestData", URLEncoder.encode(requestDataJson, "UTF-8"));
            params.put("EBusinessID", config.getBusinessId());
            params.put("RequestType", "1008"); // 物流跟踪订阅
            params.put("DataSign", URLEncoder.encode(dataSign, "UTF-8"));
            params.put("DataType", "2");

            // 2. 发送HTTP请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<String> entity = new HttpEntity<>(buildFormParams(params), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    config.getApiUrl(), entity, String.class);

            // 3. 解析响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JSONObject result = JSON.parseObject(response.getBody());
                return "true".equals(result.getString("Success"));
            }

            return false;

        } catch (Exception e) {
            log.error("【快递鸟订阅异常】", e);
            return false;
        }
    }

    /**
     * 快递100订阅物流轨迹
     */
    private boolean subscribeByKuaiDi100(String expressCompany, String expressNo, String callbackUrl) {
        try {
            ExpressApiConfig.KuaiDi100 config = expressApiConfig.getKuaidi100();
            
            // 1. 构建请求参数
            String url = String.format("%s?customer=%s&sign=%s&param={\"company\":\"%s\",\"number\":\"%s\",\"from\":\"\",\"to\":\"\",\"resultv2\":\"1\",\"callbackurl\":\"%s\"}",
                    "https://poll.kuaidi100.com/poll",
                    config.getCustomerId(),
                    generateSign(expressCompany, expressNo, config.getApiKey()),
                    getKuaidi100ComCode(expressCompany),
                    expressNo,
                    URLEncoder.encode(callbackUrl, "UTF-8"));

            // 2. 发送HTTP请求
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

            // 3. 解析响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JSONObject result = JSON.parseObject(response.getBody());
                return "200".equals(result.getString("returnCode"));
            }

            return false;

        } catch (Exception e) {
            log.error("【快递100订阅异常】", e);
            return false;
        }
    }

    /**
     * 解析快递鸟轨迹数据
     */
    private List<LogisticsTrack> parseKuaiDiNiaoTracks(com.alibaba.fastjson.JSONArray traces, String expressNo) {
        List<LogisticsTrack> tracks = new ArrayList<>();
        
        if (traces == null || traces.isEmpty()) {
            return tracks;
        }

        for (int i = 0; i < traces.size(); i++) {
            JSONObject trace = traces.getJSONObject(i);
            
            LogisticsTrack track = new LogisticsTrack();
            track.setExpressNo(expressNo);
            track.setTrackContent(trace.getString("AcceptStation"));
            track.setTrackLocation(""); // 快递鸟不返回地点
            track.setTrackTime(LocalDateTime.parse(
                    trace.getString("AcceptTime"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ));
            track.setTrackStatus(mapKuaiDiNiaoStatus(trace.getString("AcceptStation")));
            
            tracks.add(track);
        }

        // 按时间倒序
        tracks.sort((a, b) -> b.getTrackTime().compareTo(a.getTrackTime()));
        return tracks;
    }

    /**
     * 解析快递100轨迹数据
     */
    private List<LogisticsTrack> parseKuaiDi100Tracks(com.alibaba.fastjson.JSONArray data, String expressNo) {
        List<LogisticsTrack> tracks = new ArrayList<>();
        
        if (data == null || data.isEmpty()) {
            return tracks;
        }

        for (int i = 0; i < data.size(); i++) {
            JSONObject trace = data.getJSONObject(i);
            
            LogisticsTrack track = new LogisticsTrack();
            track.setExpressNo(expressNo);
            track.setTrackContent(trace.getString("context"));
            track.setTrackLocation(trace.getString("location"));
            track.setTrackTime(LocalDateTime.parse(
                    trace.getString("time"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ));
            track.setTrackStatus(mapKuaiDi100Status(trace.getInteger("status")));
            
            tracks.add(track);
        }

        // 按时间倒序
        tracks.sort((a, b) -> b.getTrackTime().compareTo(a.getTrackTime()));
        return tracks;
    }

    /**
     * 获取快递鸟快递公司编码
     */
    private String getShipperCode(String expressCompany) {
        // 常见快递公司编码映射
        Map<String, String> codeMap = new HashMap<>();
        codeMap.put("顺丰速运", "SF");
        codeMap.put("圆通速递", "YTO");
        codeMap.put("申通快递", "STO");
        codeMap.put("中通快递", "ZTO");
        codeMap.put("韵达快递", "YD");
        codeMap.put("EMS", "EMS");
        codeMap.put("京东物流", "JD");
        
        return codeMap.getOrDefault(expressCompany, expressCompany);
    }

    /**
     * 获取快递100快递公司编码
     */
    private String getKuaidi100ComCode(String expressCompany) {
        Map<String, String> codeMap = new HashMap<>();
        codeMap.put("顺丰速运", "shunfeng");
        codeMap.put("圆通速递", "yuantong");
        codeMap.put("申通快递", "shentong");
        codeMap.put("中通快递", "zhongtong");
        codeMap.put("韵达快递", "yunda");
        codeMap.put("EMS", "ems");
        codeMap.put("京东物流", "jd");
        
        return codeMap.getOrDefault(expressCompany, expressCompany.toLowerCase());
    }

    /**
     * 映射快递鸟状态
     */
    private Integer mapKuaiDiNiaoStatus(String acceptStation) {
        if (acceptStation == null) {
            return TrackStatusEnum.IN_TRANSIT.getCode();
        }
        
        if (acceptStation.contains("签收") || acceptStation.contains("已签收")) {
            return TrackStatusEnum.SIGNED.getCode();
        } else if (acceptStation.contains("派件")) {
            return TrackStatusEnum.DELIVERING.getCode();
        } else if (acceptStation.contains("揽件") || acceptStation.contains("取件")) {
            return TrackStatusEnum.PICKED_UP.getCode();
        } else {
            return TrackStatusEnum.IN_TRANSIT.getCode();
        }
    }

    /**
     * 映射快递100状态
     */
    private Integer mapKuaiDi100Status(Integer status) {
        if (status == null) {
            return TrackStatusEnum.IN_TRANSIT.getCode();
        }
        
        // 快递100状态码?=在途，1=揽件?=疑难?=签收?=退签，5=派件?=退?
        switch (status) {
            case 1:
                return TrackStatusEnum.PICKED_UP.getCode();
            case 3:
                return TrackStatusEnum.SIGNED.getCode();
            case 5:
                return TrackStatusEnum.DELIVERING.getCode();
            default:
                return TrackStatusEnum.IN_TRANSIT.getCode();
        }
    }

    /**
     * 快递鸟数据签名（MD5加密?
     */
    private String encrypt(String content, String key, String charset) throws Exception {
        String signStr = content + key;
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
        byte[] bytes = md.digest(signStr.getBytes(charset));
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        // Base64编码
        return Base64.getEncoder().encodeToString(hexString.toString().getBytes(charset));
    }

    /**
     * 快递100签名生成
     */
    private String generateSign(String expressCompany, String expressNo, String key) {
        String param = String.format("{\"com\":\"%s\",\"num\":\"%s\"}", 
                getKuaidi100ComCode(expressCompany), expressNo);
        String signStr = param + key + expressApiConfig.getKuaidi100().getCustomerId();
        
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(signStr.getBytes("UTF-8"));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString().toUpperCase();
        } catch (Exception e) {
            log.error("【生成签名失败?, e);
            return "";
        }
    }

    /**
     * 构建表单参数
     */
    private String buildFormParams(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }
}
