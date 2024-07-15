package com.nictas.reviews.controller.rest.dto;

import java.util.List;

import lombok.Data;

@Data
public class UserInfo {

    private final String login;
    private final List<String> roles;

}
