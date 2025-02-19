package com.fxinject.Test;

import com.fxinject.Test.UserService;
import com.fxinject.annotations.Component;
import com.fxinject.annotations.Inject;

@Component
public class UserController {
    private final UserService userService;

    @Inject
    public UserController(UserService userService) {
        this.userService = userService;
    }

    public void displayUserName() {
        System.out.println("User: " + userService.getUserName());
    }
}