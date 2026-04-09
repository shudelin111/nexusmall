package com.nexusmall.logistics.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nexusmall.logistics.domain.entity.LogisticsTrack;
import com.nexusmall.logistics.infrastructure.persistence.mapper.LogisticsTrackMapper;
import com.nexusmall.logistics.application.service.LogisticsTrackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物流轨迹服务实现类
 *
 * @author shudl
 * @since 2026-04-07
 */
@Slf4j
@Service
public class LogisticsTrackServiceImpl extends ServiceImpl<LogisticsTrackMapper, LogisticsTrack> implements LogisticsTrackService {

    @Override
    public List<LogisticsTrack> listByLogisticsOrderId(Long logisticsOrderId) {
        return this.baseMapper.selectByLogisticsOrderId(logisticsOrderId);
    }

    @Override
    public List<LogisticsTrack> listByExpressNo(String expressNo) {
        return this.baseMapper.selectByExpressNo(expressNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addTrack(Long logisticsOrderId, String expressNo, String trackContent,
                            String trackLocation, Integer trackStatus) {
        log.info("【添加物流轨迹】logisticsOrderId={}, expressNo={}, content={}", 
                logisticsOrderId, expressNo, trackContent);

        LogisticsTrack track = new LogisticsTrack();
        track.setLogisticsOrderId(logisticsOrderId);
        track.setExpressNo(expressNo);
        track.setTrackTime(LocalDateTime.now());
        track.setTrackContent(trackContent);
        track.setTrackLocation(trackLocation);
        track.setTrackStatus(trackStatus);

        boolean success = this.save(track);

        if (success) {
            log.info("【添加物流轨迹成功】trackId={}", track.getId());
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchAddTracks(Long logisticsOrderId, String expressNo, List<LogisticsTrack> tracks) {
        log.info("【批量添加物流轨迹】logisticsOrderId={}, expressNo={}, count={}", 
                logisticsOrderId, expressNo, tracks.size());

        if (tracks == null || tracks.isEmpty()) {
            log.warn("【批量添加物流轨迹】轨迹列表为空");
            return false;
        }

        // 设置公共字段
        for (LogisticsTrack track : tracks) {
            track.setLogisticsOrderId(logisticsOrderId);
            track.setExpressNo(expressNo);
            if (track.getTrackTime() == null) {
                track.setTrackTime(LocalDateTime.now());
            }
        }

        boolean success = this.saveBatch(tracks);

        if (success) {
            log.info("【批量添加物流轨迹成功】count={}", tracks.size());
        }

        return success;
    }
}
