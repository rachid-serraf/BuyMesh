package com.buy01.user.infrastructure.adapters.in.web.dto.request;

import org.springframework.web.multipart.MultipartFile;

import com.buy01.user.application.command.UpdateUserCommand;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

public class UpdateUserRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    private MultipartFile avatar;

    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String name, String email, MultipartFile avatar) {
        this.name = name;
        this.email = email;
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public MultipartFile getAvatar() {
        return avatar;
    }

    public void setAvatar(MultipartFile avatar) {
        this.avatar = avatar;
    }

    public String name() {
        return name;
    }

    public String email() {
        return email;
    }

    public MultipartFile avatar() {
        return avatar;
    }

    public UpdateUserCommand toCommand(String avatarUrl) {
        return new UpdateUserCommand(name, email, avatarUrl);
    }
}
