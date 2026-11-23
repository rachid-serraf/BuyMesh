package com.buy01.user.infrastructure.adapters.in.web.dto.response;

import java.util.UUID;

import com.buy01.user.domain.model.Role;
import com.buy01.user.domain.model.User;

public class UserResponse {

    private final UUID id;
    private final String name;
    private final String email;
    private final String avatarUrl;
    private final Role role;

    public UserResponse(UUID id, String name, String email, String avatarUrl, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.role = role;
    }

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getRole()
        );
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public Role getRole() {
        return role;
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String email() {
        return email;
    }

    public String avatarUrl() {
        return avatarUrl;
    }

    public Role role() {
        return role;
    }
}
