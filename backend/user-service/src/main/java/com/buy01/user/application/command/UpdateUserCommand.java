package com.buy01.user.application.command;

public class UpdateUserCommand {

    private final String name;
    private final String email;
    private final String avatarUrl;

    public UpdateUserCommand(String name, String email, String avatarUrl) {
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
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
}
