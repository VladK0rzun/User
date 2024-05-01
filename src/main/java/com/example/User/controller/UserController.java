package com.example.User.controller;

import com.example.User.Services.UserServices;
import com.example.User.model.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserServices userServices;

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody @Valid User user) {
        try {
            User createdUser = userServices.createUser(user);
            return ResponseEntity.ok(createdUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user){
        try {
            User updatedUser = userServices.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userServices.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            // Include the exception message in the response body and set the correct status
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUser(@RequestParam("from") LocalDate from, @RequestParam("to") LocalDate to){
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("From date must be earlier than To date.");
        }
        List<User> users = userServices.searchUserByBirthDate(from, to);
        return ResponseEntity.ok(users);
    }
}
