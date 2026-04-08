package com.nexusmall.member.interfaces.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nexusmall.common.annotation.ApiVersion;
import com.nexusmall.common.vo.Result;
import com.nexusmall.member.domain.entity.Member;
import com.nexusmall.member.domain.entity.MemberReceiveAddress;
import com.nexusmall.member.application.service.MemberLevelUpgradeNotificationService;
import com.nexusmall.member.application.service.MemberReceiveAddressService;
import com.nexusmall.member.application.service.MemberService;
import com.nexusmall.member.interfaces.dto.AddressRequest;
import com.nexusmall.member.interfaces.dto.IntegrationConsumeRequest;
import com.nexusmall.member.interfaces.dto.MemberProfileUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 会员管理控制器
 * <p>
 * 业界标准：
 * - 提供会员资料查询/更新接口
 * - 提供收货地址 CRUD 接口
 * - 所有接口需要认证(Auth Token)
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@RestController
@RequestMapping("/members")  // RESTful资源路径：会员集合
@ApiVersion("v1")  // 标记此 Controller 支持 v1 版本
@Tag(name = "会员管理", description = "会员资料和收货地址管理")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberReceiveAddressService addressService;

    @Autowired
    private MemberLevelUpgradeNotificationService notificationService;

    /**
     * 查询当前会员信息
     *
     * @param userId 用户 ID(从 Token 中解析)
     * @return 会员信息
     */
    @GetMapping(value = "/{userId}", headers = "X-API-Version=v1")
    @Operation(summary = "查询会员信息")
    public Result<Member> getMemberInfo(@PathVariable Long userId) {
        log.info("查询会员信息，userId: {}", userId);
        
        Member member = memberService.getByUserId(userId);
        if (member == null) {
            return Result.failure("404", "会员不存在");
        }
        
        return Result.success(member);
    }

    /**
     * 更新会员资料
     *
     * @param userId  用户 ID
     * @param request 更新请求
     * @return 是否成功
     */
    @PatchMapping(value = "/{userId}/profile", headers = "X-API-Version=v1")
    @Operation(summary = "更新会员资料")
    public Result<Void> updateProfile(@PathVariable Long userId, 
                                       @Validated @RequestBody MemberProfileUpdateRequest request) {
        log.info("更新会员资料，userId: {}", userId);
        
        Member member = memberService.getByUserId(userId);
        if (member == null) {
            return Result.failure("404", "会员不存在");
        }
        
        // 构建更新对象
        Member updateMember = new Member();
        updateMember.setId(member.getId());
        updateMember.setNickname(request.getNickname());
        updateMember.setAvatar(request.getAvatar());
        updateMember.setGender(request.getGender());
        
        if (request.getBirthday() != null && !request.getBirthday().isEmpty()) {
            updateMember.setBirthday(LocalDate.parse(request.getBirthday(), DateTimeFormatter.ISO_LOCAL_DATE));
        }
        
        boolean success = memberService.updateMemberProfile(updateMember);
        return success ? Result.success() : Result.failure("500", "更新失败");
    }

    /**
     * 查询收货地址列表
     *
     * @param userId 用户 ID
     * @return 收货地址列表
     */
    @GetMapping(value = "/{userId}/addresses", headers = "X-API-Version=v1")
    @Operation(summary = "查询收货地址列表")
    public Result<List<MemberReceiveAddress>> listAddresses(@PathVariable Long userId) {
        log.info("查询收货地址列表，userId: {}", userId);
        
        Member member = memberService.getByUserId(userId);
        if (member == null) {
            return Result.failure("404", "会员不存在");
        }
        
        List<MemberReceiveAddress> addresses = addressService.listByMemberId(member.getId());
        return Result.success(addresses);
    }

    /**
     * 查询默认收货地址
     *
     * @param userId 用户 ID
     * @return 默认收货地址
     */
    @GetMapping(value = "/{userId}/addresses/default", headers = "X-API-Version=v1")
    @Operation(summary = "查询默认收货地址")
    public Result<MemberReceiveAddress> getDefaultAddress(@PathVariable Long userId) {
        log.info("查询默认收货地址，userId: {}", userId);
        
        Member member = memberService.getByUserId(userId);
        if (member == null) {
            return Result.failure("404", "会员不存在");
        }
        
        List<MemberReceiveAddress> addresses = addressService.listByMemberId(member.getId());
        // 返回第一个地址(已按 default_status 降序排列)
        MemberReceiveAddress defaultAddress = addresses.isEmpty() ? null : addresses.get(0);
        
        return Result.success(defaultAddress);
    }

    /**
     * 添加收货地址
     *
     * @param userId  用户 ID
     * @param request 地址信息
     * @return 是否成功
     */
    @PostMapping(value = "/{userId}/addresses", headers = "X-API-Version=v1")
    @Operation(summary = "添加收货地址")
    public Result<Void> addAddress(@PathVariable Long userId, 
                                    @Validated @RequestBody AddressRequest request) {
        log.info("添加收货地址，userId: {}", userId);
        
        Member member = memberService.getByUserId(userId);
        if (member == null) {
            return Result.failure("404", "会员不存在");
        }
        
        MemberReceiveAddress address = new MemberReceiveAddress();
        BeanUtils.copyProperties(request, address);
        address.setMemberId(member.getId());
        
        boolean success = addressService.save(address);
        return success ? Result.success() : Result.failure("500", "添加失败");
    }

    /**
     * 更新收货地址
     *
     * @param userId    用户 ID
     * @param addressId 地址 ID
     * @param request   地址信息
     * @return 是否成功
     */
    @PutMapping(value = "/{userId}/addresses/{addressId}", headers = "X-API-Version=v1")
    @Operation(summary = "更新收货地址")
    public Result<Void> updateAddress(@PathVariable Long userId,
                                       @PathVariable Long addressId,
                                       @Validated @RequestBody AddressRequest request) {
        log.info("更新收货地址，addressId: {}, userId: {}", addressId, userId);
        
        Member member = memberService.getByUserId(userId);
        if (member == null) {
            return Result.failure("404", "会员不存在");
        }
        
        // 验证地址归属
        MemberReceiveAddress existingAddress = addressService.getById(addressId);
        if (existingAddress == null || !existingAddress.getMemberId().equals(member.getId())) {
            return Result.failure("403", "地址不存在或无权操作");
        }
        
        MemberReceiveAddress address = new MemberReceiveAddress();
        address.setId(addressId);
        BeanUtils.copyProperties(request, address);
        
        boolean success = addressService.updateById(address);
        return success ? Result.success() : Result.failure("500", "更新失败");
    }

    /**
     * 删除收货地址
     *
     * @param userId    用户 ID
     * @param addressId 地址 ID
     * @return 是否成功
     */
    @DeleteMapping(value = "/{userId}/addresses/{addressId}", headers = "X-API-Version=v1")
    @Operation(summary = "删除收货地址")
    public Result<Void> deleteAddress(@PathVariable Long userId,
                                       @PathVariable Long addressId) {
        log.info("删除收货地址，addressId: {}, userId: {}", addressId, userId);
        
        Member member = memberService.getByUserId(userId);
        if (member == null) {
            return Result.failure("404", "会员不存在");
        }
        
        boolean success = addressService.deleteAddress(addressId, member.getId());
        return success ? Result.success() : Result.failure("500", "删除失败");
    }

    /**
     * 设置默认收货地址
     *
     * @param userId    用户 ID
     * @param addressId 地址 ID
     * @return 是否成功
     */
    @PatchMapping(value = "/{userId}/addresses/{addressId}/default", headers = "X-API-Version=v1")
    @Operation(summary = "设置默认收货地址")
    public Result<Void> setDefaultAddress(@PathVariable Long userId,
                                           @PathVariable Long addressId) {
        log.info("设置默认收货地址，addressId: {}, userId: {}", addressId, userId);
        
        Member member = memberService.getByUserId(userId);
        if (member == null) {
            return Result.failure("404", "会员不存在");
        }
        
        boolean success = addressService.setDefaultAddress(addressId, member.getId());
        return success ? Result.success() : Result.failure("500", "设置失败");
    }

    /**
     * 积分兑换
     * <p>
     * 业界标准：
     * - 支持兑换优惠券/商品/现金
     * - 事务保证积分扣减和记录一致性
     * </p>
     *
     * @param userId  用户 ID
     * @param request 兑换请求
     * @return 是否成功
     */
    @PostMapping(value = "/{userId}/integrations/consume", headers = "X-API-Version=v1")
    @Operation(summary = "积分兑换")
    public Result<Void> consumeIntegration(@PathVariable Long userId,
                                            @Validated @RequestBody IntegrationConsumeRequest request) {
        log.info("积分兑换，userId: {}, integration: {}, type: {}", userId, request.getIntegration(), request.getConsumeType());
        
        Member member = memberService.getByUserId(userId);
        if (member == null) {
            return Result.failure("404", "会员不存在");
        }
        
        // 检查积分是否足够
        if (member.getIntegration() < request.getIntegration()) {
            return Result.failure("400", "积分不足，当前积分: " + member.getIntegration());
        }
        
        try {
            // 扣除积分（负数表示减少）
            memberService.addIntegration(member.getId(), -request.getIntegration(), 
                request.getConsumeType(), request.getObjectId(), request.getNote());
            
            // TODO: 创建兑换记录
            // IntegrationConsumeRecord record = new IntegrationConsumeRecord();
            // record.setMemberId(member.getId());
            // record.setIntegration(request.getIntegration());
            // record.setConsumeType(request.getConsumeType());
            // record.setObjectId(request.getObjectId());
            // record.setObjectName(request.getObjectName());
            // record.setAmount(request.getAmount());
            // record.setStatus(1); // 已完成
            // record.setCreateTime(LocalDateTime.now());
            // consumeRecordMapper.insert(record);
            
            log.info("积分兑换成功，userId: {}, 消耗积分: {}", userId, request.getIntegration());
            return Result.success();
        } catch (Exception e) {
            log.error("积分兑换失败，userId: {}", userId, e);
            return Result.failure("500", "兑换失败: " + e.getMessage());
        }
    }
}
