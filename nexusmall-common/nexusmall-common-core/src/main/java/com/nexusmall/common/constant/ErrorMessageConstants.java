package com.nexusmall.common.constant;

/**
 * 错误消息常量
 * <p>
 * 统一管理所有业务异常的错误消息，避免硬编码
 * </p>
 *
 * @author shudl
 * @since 2026-03-26
 */
public class ErrorMessageConstants {

    private ErrorMessageConstants() {
        // 防止实例化
    }

    /**
     * 认证相关错误消息
     */
    public static class Auth {
        
        public static final String ROLE_NOT_FOUND_WITH_ID = "角色不存在：";
        public static final String PERMISSION_NOT_FOUND_WITH_ID = "权限不存在：";
        public static final String USER_NOT_FOUND_WITH_USERNAME = "用户不存在：";
        public static final String USER_DISABLED_WITH_USERNAME = "用户已被禁用：";

        private Auth() {
            // 防止实例化
        }
    }

    /**
     * 第三方服务相关错误消息
     */
    public static class ThirdParty {
        
        public static final String ALIYUN_SMS_API_CALL_FAILED = "调用阿里云短信接口失败：";
        public static final String OSS_FILE_READ_FAILED = "读取上传文件失败";

        private ThirdParty() {
            // 防止实例化
        }
    }

    /**
     * 订单相关错误消息
     */
    public static class Order {
        
        public static final String MQ_SEND_MESSAGE_FAILED = "发送普通消息失败";

        private Order() {
            // 防止实例化
        }
    }

    /**
     * 系统异常相关错误消息
     */
    public static class System {
        
        /**
         * 默认系统内部错误提示（用于开发环境兜底）
         */
        public static final String INTERNAL_ERROR = "系统内部错误";

        private System() {
            // 防止实例化
        }
    }

    /**
     * 环境配置相关常量
     */
    public static class Environment {
        
        /**
         * 开发环境标识
         */
        public static final String DEV = "dev";
        
        /**
         * 测试环境标识
         */
        public static final String TEST = "test";
        
        /**
         * 生产环境标识
         */
        public static final String PROD = "prod";

        private Environment() {
            // 防止实例化
        }
    }
}
