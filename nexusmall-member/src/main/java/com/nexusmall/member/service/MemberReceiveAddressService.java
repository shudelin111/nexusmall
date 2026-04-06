package com.nexusmall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nexusmall.member.entity.MemberReceiveAddress;

import java.util.List;

/**
 * 会员收货地址服务接口
 *
 * @author shudl
 * @since 2026-04-06
 */
public interface MemberReceiveAddressService extends IService<MemberReceiveAddress> {

    /**
     * 查询会员的所有收货地址
     *
     * @param memberId 会员 ID
     * @return 收货地址列表
     */
    List<MemberReceiveAddress> listByMemberId(Long memberId);

    /**
     * 设置默认收货地址
     *
     * @param addressId 地址 ID
     * @param memberId  会员 ID
     * @return 是否成功
     */
    boolean setDefaultAddress(Long addressId, Long memberId);

    /**
     * 删除收货地址
     *
     * @param addressId 地址 ID
     * @param memberId  会员 ID
     * @return 是否成功
     */
    boolean deleteAddress(Long addressId, Long memberId);
}
