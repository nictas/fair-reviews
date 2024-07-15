package com.nictas.reviews.controller.rest;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nictas.reviews.controller.rest.dto.UserInfo;

@RestController
@RequestMapping("/user-info")
public class UserInfoController {

    @GetMapping
    public UserInfo getUserInfo(@AuthenticationPrincipal OAuth2IntrospectionAuthenticatedPrincipal principal) {
        List<String> roles = getRoles(principal);
        return new UserInfo(principal.getName(), roles);
    }

    private List<String> getRoles(OAuth2IntrospectionAuthenticatedPrincipal principal) {
        return principal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

}
