package com.nictas.reviews.controller.rest;

import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class ControllerTestData {

    static final Set<GrantedAuthority> AUTHORITIES_USER = Set.of(new SimpleGrantedAuthority("ROLE_USER"));
    static final Set<GrantedAuthority> AUTHORITIES_ADMIN = Set.of(new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("ROLE_ADMIN"));

}
