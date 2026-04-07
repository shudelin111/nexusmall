package com.nexusmall.member.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nexusmall.member.application.service.MemberReceiveAddressService;
import com.nexusmall.member.domain.entity.MemberReceiveAddress;
import com.nexusmall.member.infrastructure.persistence.dao.MemberReceiveAddressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 会员收货地址服务实现类
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberReceiveAddressServiceImpl extends ServiceImpl<MemberReceiveAddressMapper, MemberReceiveAddress> implements MemberReceiveAddressService {

    @Override
    public List<MemberReceiveAddress> listByMemberId(Long memberId) {
        log.info("查询会员收货地址: memberId={}", memberId);
        
        LambdaQueryWrapper<MemberReceiveAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberReceiveAddress::getMemberId, memberId)
               .orderByDesc(MemberReceiveAddress::getDefaultStatus)
               .orderByDesc(MemberReceiveAddress::getCreateTime);
        
        return this.list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultAddress(Long addressId, Long memberId) {
        log.info("设置默认收货地址: addressId={}, memberId={}", addressId, memberId);
        
        // 1. 取消该会员所有地址的默认状态
        LambdaUpdateWrapper<MemberReceiveAddress> cancelWrapper = new LambdaUpdateWrapper<>();
        cancelWrapper.eq(MemberReceiveAddress::getMemberId, memberId)
                     .set(MemberReceiveAddress::getDefaultStatus, 0);
        this.update(cancelWrapper);
        
        // 2. 设置指定地址为默认
        LambdaUpdateWrapper<MemberReceiveAddress> setWrapper = new LambdaUpdateWrapper<>();
        setWrapper.eq(MemberReceiveAddress::getId, addressId)
                  .eq(MemberReceiveAddress::getMemberId, memberId)
                  .set(MemberReceiveAddress::getDefaultStatus, 1);
        
        boolean success = this.update(setWrapper);
        log.info("设置默认收货地址{}", success ? "成功" : "失败");
        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAddress(Long addressId, Long memberId) {
        log.info("删除收货地址: addressId={}, memberId={}", addressId, memberId);
        
        LambdaQueryWrapper<MemberReceiveAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberReceiveAddress::getId, addressId)
               .eq(MemberReceiveAddress::getMemberId, memberId);
        
        boolean success = this.remove(wrapper);
        log.info("删除收货地址{}", success ? "成功" : "失败");
        return success;
    }
}
