package com.nexusmall.member.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nexusmall.member.application.service.MemberService;
import com.nexusmall.member.domain.entity.GrowthChangeHistory;
import com.nexusmall.member.domain.entity.IntegrationChangeHistory;
import com.nexusmall.member.domain.entity.Member;
import com.nexusmall.member.infrastructure.persistence.dao.GrowthChangeHistoryMapper;
import com.nexusmall.member.infrastructure.persistence.dao.IntegrationChangeHistoryMapper;
import com.nexusmall.member.infrastructure.persistence.dao.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 会员服务实现类
 *
 * @author shudl
 * @since 2026-04-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

    private final GrowthChangeHistoryMapper growthChangeHistoryMapper;
    private final IntegrationChangeHistoryMapper integrationChangeHistoryMapper;

    @Override
    public Member getByUserId(Long userId) {
        log.info("查询会员信息: userId={}", userId);
        
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getUserId, userId);
        
        return this.getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMemberProfile(Member member) {
        log.info("更新会员资料: memberId={}", member.getId());
        
        // 只允许更新部分字段
        Member updateMember = new Member();
        updateMember.setId(member.getId());
        updateMember.setNickname(member.getNickname());
        updateMember.setAvatar(member.getAvatar());
        updateMember.setGender(member.getGender());
        updateMember.setBirthday(member.getBirthday());
        updateMember.setUpdateTime(LocalDateTime.now());
        
        return this.updateById(updateMember);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addGrowthPoint(Long memberId, Integer growthPoint, String sourceType, Long sourceId, String note) {
        log.info("增加成长值: memberId={}, growthPoint={}, sourceType={}", memberId, growthPoint, sourceType);
        
        // 1. 查询会员当前成长值
        Member member = this.getById(memberId);
        if (member == null) {
            log.error("会员不存在: memberId={}", memberId);
            throw new RuntimeException("会员不存在");
        }
        
        // 2. 更新会员成长值
        int oldGrowth = member.getGrowthPoint() != null ? member.getGrowthPoint() : 0;
        int newGrowth = oldGrowth + growthPoint;
        member.setGrowthPoint(newGrowth);
        member.setUpdateTime(LocalDateTime.now());
        this.updateById(member);
        
        // 3. 记录成长值变更历史
        GrowthChangeHistory history = new GrowthChangeHistory();
        history.setMemberId(memberId);
        history.setCreateTime(LocalDateTime.now());
        history.setChangeType(0); // 0-增加
        history.setChangeCount(growthPoint);
        history.setNote(note);
        history.setSourceType(sourceType);
        history.setSourceId(sourceId);
        
        growthChangeHistoryMapper.insert(history);
        
        log.info("成长值增加成功: memberId={}, oldGrowth={}, newGrowth={}", memberId, oldGrowth, newGrowth);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addIntegration(Long memberId, Integer integration, String sourceType, Long sourceId, String note) {
        log.info("增加积分: memberId={}, integration={}, sourceType={}", memberId, integration, sourceType);
        
        // 1. 查询会员当前积分
        Member member = this.getById(memberId);
        if (member == null) {
            log.error("会员不存在: memberId={}", memberId);
            throw new RuntimeException("会员不存在");
        }
        
        // 2. 更新会员积分
        int oldIntegration = member.getIntegration() != null ? member.getIntegration() : 0;
        int newIntegration = oldIntegration + integration;
        member.setIntegration(newIntegration);
        member.setUpdateTime(LocalDateTime.now());
        this.updateById(member);
        
        // 3. 记录积分变更历史
        IntegrationChangeHistory history = new IntegrationChangeHistory();
        history.setMemberId(memberId);
        history.setCreateTime(LocalDateTime.now());
        history.setChangeType(0); // 0-增加
        history.setChangeCount(integration);
        history.setNote(note);
        history.setSourceType(sourceType);
        history.setSourceId(sourceId);
        
        integrationChangeHistoryMapper.insert(history);
        
        log.info("积分增加成功: memberId={}, oldIntegration={}, newIntegration={}", memberId, oldIntegration, newIntegration);
    }
}
