package com.nexusmall.auth.vo;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private Long expireTime;
    private String username;
}
