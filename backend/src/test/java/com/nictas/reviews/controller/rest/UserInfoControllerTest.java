package com.nictas.reviews.controller.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nictas.reviews.configuration.GitHubOpaqueTokenIntrospector;
import com.nictas.reviews.configuration.SecurityConfiguration;
import com.nictas.reviews.configuration.UserRoles;
import com.nictas.reviews.controller.rest.dto.UserInfo;

@WebMvcTest(UserInfoController.class)
@Import(SecurityConfiguration.class)
class UserInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GitHubOpaqueTokenIntrospector introspector;

    @Test
    void testGetUserInfoWithUser() throws Exception {
        String login = "foo";
        mockMvc.perform(MockMvcRequestBuilders.get("/user-info")
                .with(SecurityMockMvcRequestPostProcessors.opaqueToken()
                        .attributes(attributes -> attributes.put("sub", login))
                        .authorities(ControllerTestData.AUTHORITIES_USER)))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.login")
                        .value(login))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles")
                        .isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles.length()")
                        .value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roles[0]")
                        .value(UserRoles.ROLE_USER));
    }

    @Test
    void testGetUserWithAdmin() throws Exception {
        String login = "foo";
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/user-info")
                .with(SecurityMockMvcRequestPostProcessors.opaqueToken()
                        .attributes(attributes -> attributes.put("sub", login))
                        .authorities(ControllerTestData.AUTHORITIES_ADMIN)))
                .andExpect(MockMvcResultMatchers.status()
                        .isOk())
                .andReturn();

        UserInfo userInfo = getResponseBody(mvcResult);
        assertEquals(login, userInfo.getLogin());
        assertEquals(2, userInfo.getRoles()
                .size());
        assertTrue(userInfo.getRoles()
                .containsAll(Set.of(UserRoles.ROLE_USER, UserRoles.ROLE_ADMIN)));
    }

    private UserInfo getResponseBody(MvcResult mvcResult) throws IOException {
        return objectMapper.readValue(mvcResult.getResponse()
                .getContentAsByteArray(), UserInfo.class);
    }

}
