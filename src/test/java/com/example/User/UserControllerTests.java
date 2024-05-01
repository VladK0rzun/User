package com.example.User;

import com.example.User.Services.UserServices;
import com.example.User.controller.RestResponseEntityExceptionHandler;
import com.example.User.controller.UserController;
import com.example.User.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;

@WebMvcTest(UserController.class)
public class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserController userController;

    @MockBean
    private UserServices userServices;

    @Test
    public void testCreateUserUnderAge() throws Exception {
        String userJson = "{\"email\":\"test@mail.com\",\"firstName\":\"Tom\",\"lastName\":\"Young\",\"birthDate\":\"2020-01-01\"}";
        String errorMessage = "User must be at least 18 years old to register.";

        when(userServices.createUser(any(User.class)))
                .thenThrow(new IllegalArgumentException(errorMessage));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }

    @Test
    public void testUpdateUserSuccessfully() throws Exception {
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail("update@test.com");
        updatedUser.setFirstName("UpdatedName");
        updatedUser.setLastName("UpdatedLastName");
        updatedUser.setBirthDate(LocalDate.of(1990, 1, 1));

        given(userServices.updateUser(eq(1L), any(User.class))).willReturn(updatedUser);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"update@test.com\",\"firstName\":\"UpdatedName\",\"lastName\":\"UpdatedLastName\",\"birthDate\":\"1990-01-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("UpdatedName"))
                .andExpect(jsonPath("$.lastName").value("UpdatedLastName"));
    }

    @Test
    public void testCreateUserWithInvalidEmail() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalid\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"birthDate\":\"2000-01-01\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteUserSuccessfully() throws Exception {
        doNothing().when(userServices).deleteUser(anyLong());

        mockMvc.perform(delete("/users/2"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testSearchUsersByDateRange() throws Exception {
        mockMvc.perform(get("/users/search")
                        .param("from", "2000-01-01")
                        .param("to", "2000-12-31"))
                .andExpect(status().isOk());

        verify(userServices).searchUserByBirthDate(any(LocalDate.class), any(LocalDate.class));
    }


    @Test
    public void testDeleteNonExistingUser() throws Exception {
        doThrow(new IllegalArgumentException("User not found.")).when(userServices).deleteUser(eq(999L));

        mockMvc.perform(delete("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found."));
    }

    @Test
    public void testSearchUsersByInvalidDateRange() throws Exception {
        mockMvc.perform(get("/users/search")
                        .param("from", "2001-01-01")
                        .param("to", "2000-12-31"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalArgumentException))
                .andExpect(result -> assertEquals("From date must be earlier than To date.", result.getResolvedException().getMessage()));
    }
}
