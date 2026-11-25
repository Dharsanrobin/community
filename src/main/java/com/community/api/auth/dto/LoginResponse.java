
package com.community.api.auth.dto;

public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private UserInfo user;

    public LoginResponse() {
    }

    public LoginResponse(
            String accessToken,
            String refreshToken,
            long expiresIn,
            UserInfo user) {

        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public UserInfo getUser() {
        return user;
    }

    public static class UserInfo {

        private Long id;
        private String uuid;
        private String name;
        private String username;
        private String email;
        private String role;

        public UserInfo(
                Long id,
                String uuid,
                String name,
                String username,
                String email,
                String role) {

            this.id = id;
            this.uuid = uuid;
            this.name = name;
            this.username = username;
            this.email = email;
            this.role = role;
        }

        public Long getId() {
            return id;
        }

        public String getUuid() {
            return uuid;
        }

        public String getName() {
            return name;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getRole() {
            return role;
        }
    }
}
