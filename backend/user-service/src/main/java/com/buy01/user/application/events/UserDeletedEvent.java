package com.buy01.user.application.events;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDeletedEvent {

    private final UUID userId;
}
