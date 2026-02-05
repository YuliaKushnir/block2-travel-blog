package org.example.block2travelblog.controller;

import lombok.RequiredArgsConstructor;
import org.example.block2travelblog.dto.OAuthUserRequest;
import org.example.block2travelblog.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/user")
@RequiredArgsConstructor
public class OAuth2UserController {

    private final UserService userService;

    @PostMapping("/oauth")
    public Long getOrCreate(@RequestBody OAuthUserRequest req) {
        return userService
                .getOrCreateOAuthUser(req.email(), req.name())
                .getId();
    }

}
