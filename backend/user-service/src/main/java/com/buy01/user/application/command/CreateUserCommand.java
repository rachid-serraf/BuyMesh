package com.buy01.user.application.command;

import com.buy01.user.domain.model.Role;

public class CreateUserCommand {

    private final String name;
    private final String email;
    private final String password;
    private final Role role;
    private final String avatarUrl;

    public CreateUserCommand(String name, String email, String password, Role role, String avatarUrl) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.avatarUrl = avatarUrl;
    }

    public String name() {
        return name;
    }

    public String email() {
        return email;
    }

    public String password() {
        return password;
    }

    public Role role() {
        return role;
    }

    public String avatarUrl() {
        return avatarUrl;
    }
}
