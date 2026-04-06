package com.nexusmall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nexusmall.member.dao.MemberReceiveAddressMapper;
import com.nexusmall.member.entity.MemberReceiveAddress;
import com.nexusmall.member.service.MemberReceiveAddressService;
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
public class MemberReceiveAddressServiceImpl extends ServiceImpl<MemberReceiveAddressMapper, MemberReceiveAddress> implements MemberReceiveAddressService {

    @Override
    public List<MemberReceiveAddress> listByMemberId(Long memberId) {
        return this.list(new LambdaQueryWrapper<MemberReceiveAddress>()
                .eq(MemberReceiveAddress::getMemberId, memberId)
                .orderByDesc(MemberReceiveAddress::getDefaultStatus)
                .orderByDesc(MemberReceiveAddress::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setDefaultAddress(Long addressId, Long memberId) {
        log.info("设置默认收货地址，addressId: {}, memberId: {}", addressId, memberId);
        
        // 1. 取消该会员所有地址的默认状态
        this.update(new LambdaUpdateWrapper<MemberReceiveAddress>()
                .eq(MemberReceiveAddress::getMemberId, memberId)
                .set(MemberReceiveAddress::getDefaultStatus, 0));
        
        // 2. 设置指定地址为默认
        MemberReceiveAddress address = this.getById(addressId);
        if (address == null || !address.getMemberId().equals(memberId)) {
            throw new RuntimeException("地址不存在或不属于该会员");
        }
        
        address.setDefaultStatus(1);
        return this.updateById(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAddress(Long addressId, Long memberId) {
        log.info("删除收货地址，addressId: {}, memberId: {}", addressId, memberId);
        
        MemberReceiveAddress address = this.getById(addressId);
        if (address == null || !address.getMemberId().equals(memberId)) {
            throw new RuntimeException("地址不存在或不属于该会员");
        }
        
        return this.removeById(addressId);
    }
}
