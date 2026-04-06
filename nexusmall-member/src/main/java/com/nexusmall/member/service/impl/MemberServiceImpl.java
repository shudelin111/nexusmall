package com.nexusmall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nexusmall.member.dao.GrowthChangeHistoryMapper;
import com.nexusmall.member.dao.IntegrationChangeHistoryMapper;
import com.nexusmall.member.dao.MemberLevelMapper;
import com.nexusmall.member.dao.MemberMapper;
import com.nexusmall.member.entity.GrowthChangeHistory;
import com.nexusmall.member.entity.IntegrationChangeHistory;
import com.nexusmall.member.entity.Member;
import com.nexusmall.member.entity.MemberLevel;
import com.nexusmall.member.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

    @Autowired
    private MemberLevelMapper memberLevelMapper;

    @Autowired
    private GrowthChangeHistoryMapper growthChangeHistoryMapper;

    @Autowired
    private IntegrationChangeHistoryMapper integrationChangeHistoryMapper;

    @Override
    public Member getByUserId(Long userId) {
        return this.getOne(new LambdaQueryWrapper<Member>()
                .eq(Member::getUserId, userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMemberProfile(Member member) {
        log.info("更新会员资料，userId: {}", member.getUserId());
        
        // 只允许更新特定字段
        Member updateMember = new Member();
        updateMember.setId(member.getId());
        updateMember.setNickname(member.getNickname());
        updateMember.setAvatar(member.getAvatar());
        updateMember.setGender(member.getGender());
        updateMember.setBirthday(member.getBirthday());
        
        return this.updateById(updateMember);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addGrowthPoint(Long memberId, Integer growthPoint, String sourceType, Long sourceId, String note) {
        log.info("增加成长值，memberId: {}, growthPoint: {}, sourceType: {}", memberId, growthPoint, sourceType);
        
        // 1. 更新会员成长值
        Member member = this.getById(memberId);
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }
        
        int oldGrowthPoint = member.getGrowthPoint();
        member.setGrowthPoint(oldGrowthPoint + growthPoint);
        this.updateById(member);
        
        // 2. 记录成长值变化历史
        GrowthChangeHistory history = GrowthChangeHistory.builder()
            .memberId(memberId)
            .changeType(growthPoint > 0 ? 0 : 1) // 0=增加, 1=减少
            .changeCount(Math.abs(growthPoint))
            .sourceType(sourceType)
            .sourceId(sourceId)
            .note(note)
            .createTime(LocalDateTime.now())
            .build();
        growthChangeHistoryMapper.insert(history);
        
        // 3. 检查是否需要升级会员等级
        checkAndUpgradeMemberLevel(member);
        
        log.info("成长值增加成功，memberId: {}, 旧成长值: {}, 新成长值: {}", memberId, oldGrowthPoint, member.getGrowthPoint());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addIntegration(Long memberId, Integer integration, String sourceType, Long sourceId, String note) {
        log.info("增加积分，memberId: {}, integration: {}, sourceType: {}", memberId, integration, sourceType);
        
        // 1. 更新会员积分
        Member member = this.getById(memberId);
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }
        
        int oldIntegration = member.getIntegration();
        member.setIntegration(oldIntegration + integration);
        this.updateById(member);
        
        // 2. 记录积分变化历史
        IntegrationChangeHistory history = IntegrationChangeHistory.builder()
            .memberId(memberId)
            .changeType(integration > 0 ? 0 : 1) // 0=增加, 1=减少
            .changeCount(Math.abs(integration))
            .sourceType(sourceType)
            .sourceId(sourceId)
            .note(note)
            .createTime(LocalDateTime.now())
            .build();
        integrationChangeHistoryMapper.insert(history);
        
        log.info("积分增加成功，memberId: {}, 旧积分: {}, 新积分: {}", memberId, oldIntegration, member.getIntegration());
    }
    
    /**
     * 检查并升级会员等级
     * <p>
     * 业界标准：
     * - 根据成长值自动匹配等级
     * - 只升不降（降级需要人工审核）
     * - 记录等级变更日志
     * </p>
     *
     * @param member 会员信息
     */
    private void checkAndUpgradeMemberLevel(Member member) {
        // 1. 查询所有可用的会员等级，按成长值阈值升序排列
        java.util.List<MemberLevel> levels = memberLevelMapper.selectList(
            new LambdaQueryWrapper<MemberLevel>()
                .eq(MemberLevel::getStatus, 1)
                .orderByAsc(MemberLevel::getGrowthPointThreshold)
        );
        
        if (levels.isEmpty()) {
            log.warn("没有可用的会员等级");
            return;
        }
        
        // 2. 找到当前成长值对应的最高等级
        MemberLevel targetLevel = levels.get(0); // 默认最低等级
        for (MemberLevel level : levels) {
            if (member.getGrowthPoint() >= level.getGrowthPointThreshold()) {
                targetLevel = level;
            } else {
                break; // 已经超过当前成长值，停止查找
            }
        }
        
        // 3. 如果等级有变化，则升级
        if (!targetLevel.getId().equals(member.getMemberLevelId())) {
            Long oldLevelId = member.getMemberLevelId();
            member.setMemberLevelId(targetLevel.getId());
            this.updateById(member);
            
            log.info("会员等级升级成功，memberId: {}, 旧等级ID: {}, 新等级ID: {}, 等级名称: {}",
                member.getId(), oldLevelId, targetLevel.getId(), targetLevel.getLevelName());
            
            // TODO: 可以发送等级升级通知（短信/邮件/Push）
        }
    }
}
