package com.junit.starter.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class UserServiceTest {

    @Test
    void usersEmptyIfNoUserAdded() {
        var userService = new UserService();
        var users = userService.getAll();
        assertFalse(users.isEmpty(), () -> "User list should be empty");
        // input ->[box == func] -> actual output
    }
}
