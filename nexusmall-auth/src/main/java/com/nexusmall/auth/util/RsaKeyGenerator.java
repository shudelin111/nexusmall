package com.nexusmall.auth.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

/**
 * RSA 密钥对生成工具
 * <p>
 * 业界标准：
 * - 使用 RSA-2048 位密钥 (安全性与性能平衡)
 * - 生成 PEM 格式密钥 (通用标准格式)
 * - 仅用于初始化,生产环境应从 Nacos 读取已生成的密钥
 * </p>
 *
 * @author shudl
 * @since 2026-04-05
 */
public class RsaKeyGenerator {

    private static final Logger log = LoggerFactory.getLogger(RsaKeyGenerator.class);

    /**
     * RSA 密钥长度
     * 业界标准: 2048 位 (最低安全要求)
     * 更高安全性可选: 4096 位 (性能略低)
     */
    private static final int KEY_SIZE = 2048;

    /**
     * 生成 RSA 密钥对
     *
     * @return KeyPair RSA 密钥对
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(KEY_SIZE);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            
            log.info("RSA 密钥对生成成功，密钥长度: {} 位", KEY_SIZE);
            return keyPair;
        } catch (NoSuchAlgorithmException e) {
            log.error("RSA 算法不可用", e);
            throw new RuntimeException("RSA 算法不可用", e);
        }
    }

    /**
     * 将私钥转换为 PEM 格式字符串
     *
     * @param privateKey RSA 私钥
     * @return PEM 格式的私钥字符串
     */
    public static String getPrivateKeyPem(RSAPrivateKey privateKey) {
        String privateKeyPEM = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        
        // 添加 PEM 头尾标识 (标准格式)
        StringBuilder sb = new StringBuilder();
        sb.append("-----BEGIN PRIVATE KEY-----\n");
        // 每 64 个字符换行 (PEM 标准格式)
        for (int i = 0; i < privateKeyPEM.length(); i += 64) {
            int end = Math.min(i + 64, privateKeyPEM.length());
            sb.append(privateKeyPEM, i, end).append("\n");
        }
        sb.append("-----END PRIVATE KEY-----");
        
        return sb.toString();
    }

    /**
     * 将公钥转换为 PEM 格式字符串
     *
     * @param publicKey RSA 公钥
     * @return PEM 格式的公钥字符串
     */
    public static String getPublicKeyPem(RSAPublicKey publicKey) {
        String publicKeyPEM = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        
        // 添加 PEM 头尾标识 (标准格式)
        StringBuilder sb = new StringBuilder();
        sb.append("-----BEGIN PUBLIC KEY-----\n");
        // 每 64 个字符换行 (PEM 标准格式)
        for (int i = 0; i < publicKeyPEM.length(); i += 64) {
            int end = Math.min(i + 64, publicKeyPEM.length());
            sb.append(publicKeyPEM, i, end).append("\n");
        }
        sb.append("-----END PUBLIC KEY-----");
        
        return sb.toString();
    }

    /**
     * 生成并打印密钥对 (用于初始化)
     * <p>
     * 使用方法：
     * 1. 运行 main 方法
     * 2. 复制输出的公钥和私钥
     * 3. 存入 Nacos 配置中心
     * </p>
     */
    public static void main(String[] args) {
        log.info("开始生成 RSA 密钥对...");
        
        KeyPair keyPair = generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        
        String publicKeyPem = getPublicKeyPem(publicKey);
        String privateKeyPem = getPrivateKeyPem(privateKey);
        
        System.out.println("\n========== RSA 公钥 (可公开) ==========");
        System.out.println(publicKeyPem);
        System.out.println("\n========== RSA 私钥 (严格保密) ==========");
        System.out.println(privateKeyPem);
        System.out.println("\n========================================\n");
        
        log.info("密钥对生成完成，请妥善保管私钥！");
    }
}
