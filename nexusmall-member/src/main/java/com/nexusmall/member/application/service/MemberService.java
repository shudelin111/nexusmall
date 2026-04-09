package com.nexusmall.member.application.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nexusmall.member.domain.entity.Member;

/**
 * 会员服务接口
 *
 * @author shudl
 * @since 2026-04-06
 */
public interface MemberService extends IService<Member> {

    /**
     * 根据用户 ID 查询会员信息
     *
     * @param userId 用户 ID
     * @return 会员信息
     */
    Member getByUserId(Long userId);

    /**
     * 更新会员资料
     *
     * @param member 会员信息
     * @return 是否成功
     */
    boolean updateMemberProfile(Member member);

    /**
     * 增加成长值
     *
     * @param memberId   会员 ID
     * @param growthPoint 成长值数量
     * @param sourceType  来源类型(ORDER/REVIEW/SIGN_IN)
     * @param sourceId    来源 ID
     * @param note        备注
     */
    void addGrowthPoint(Long memberId, Integer growthPoint, String sourceType, Long sourceId, String note);

    /**
     * 增加积分
     *
     * @param memberId     会员 ID
     * @param integration  积分数量
     * @param sourceType   来源类型(ORDER/COUPON/REFUND)
     * @param sourceId     来源 ID
     * @param note         备注
     */
    void addIntegration(Long memberId, Integer integration, String sourceType, Long sourceId, String note);
}
