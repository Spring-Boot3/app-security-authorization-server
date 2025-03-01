package com.romlab.app_security.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping(path = "/welcome")
public class WelcomController {

    @GetMapping
    public Map<String, String> welcome() {
        return Collections.singletonMap("msj", "Welcome to my application");
    }

}
