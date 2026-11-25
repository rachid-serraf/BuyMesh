package com.buy01.user.infrastructure.adapters.in.web.dto.request;

import org.springframework.web.multipart.MultipartFile;

import com.buy01.user.application.command.UpdateUserCommand;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;

    private MultipartFile avatar;

    public UpdateUserCommand toCommand(String avatarUrl) {
        return new UpdateUserCommand(name, email, avatarUrl);
    }
}
