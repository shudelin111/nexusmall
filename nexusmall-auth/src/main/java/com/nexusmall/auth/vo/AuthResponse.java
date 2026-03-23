package com.nexusmall.auth.vo;

import lombok.Data;

import java.util.List;

@Data
public class AuthResponse {
    private String token;
    private Long expireTime;
    private String username;
    private List<String> roles;
    private List<String> permissions;
}
