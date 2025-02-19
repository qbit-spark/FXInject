package com.fxinject.Test;

import com.fxinject.annotations.Component;
import com.fxinject.annotations.Inject;

@Component
public class UserService {
    public String getUserName() {
        return "John Doe";
    }
}