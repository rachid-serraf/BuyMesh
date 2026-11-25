package com.buy01.user.application.command;

import com.buy01.user.domain.model.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateUserCommand {

    private final String name;
    private final String email;
    private final String password;
    private final Role role;
    private final String avatarUrl;
}
