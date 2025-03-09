package com.romlab.app_security.controllers;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping(path = "/account")
public class AccountsController {

    //Esta se ejecuta antes del controller
    //@PreAuthorize("hasAnyAuthority('VIEW_ACCOUNT', 'VIEW_CARDS')")
    //Esta se ejecuta despues del controller
    //@PostAuthorize()
    @GetMapping
    public Map<String, String> account() {
        return Collections.singletonMap("msj", "Account to my application");
    }

}
