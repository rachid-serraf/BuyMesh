package com.buy01.user.application.events;

import java.util.UUID;

public class UserDeletedEvent {

    private final UUID userId;

    public UserDeletedEvent(UUID userId) {
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID userId() {
        return userId;
    }
}
