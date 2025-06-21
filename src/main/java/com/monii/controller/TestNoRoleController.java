package com.monii.controller;

import com.monii.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/norole")
public class TestNoRoleController {

    @GetMapping
    public String testAuth(@AuthenticationPrincipal User user) {
        return "Hola, " + user.getFirstName() + "! Has accedido a un endpoint protegido sin ROLE.";
    }
}
