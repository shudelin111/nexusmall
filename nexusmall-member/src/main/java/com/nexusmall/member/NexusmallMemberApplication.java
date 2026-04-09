package com.nexusmall.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * NexusMall 会员服务启动?
 * <p>
 * 职责?
 * - 会员档案管理（昵?头像/生日/性别?
 * - 收货地址管理
 * - 会员等级/积分/成长值管?
 * - 监听用户注册事件，自动创建会员档?
 * </p>
 *
 * @author shudl
 * @since 2026-04-06
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class NexusmallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusmallMemberApplication.class, args);
    }
}
