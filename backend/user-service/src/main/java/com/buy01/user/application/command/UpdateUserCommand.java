package com.buy01.user.application.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateUserCommand {

    private final String name;
    private final String email;
    private final String avatarUrl;
}
