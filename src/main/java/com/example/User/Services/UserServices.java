package com.example.User.Services;

import com.example.User.model.User;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class UserServices {
    private List<User> users =new ArrayList<>();
    private AtomicLong counter = new AtomicLong();

    @Value("${user.minimum-age}")
    private int minimumAge;

    public User createUser(User user) {
        LocalDate today = LocalDate.now();
        LocalDate birthDate = user.getBirthDate();
        int age = Period.between(birthDate, today).getYears();

        if (age < minimumAge) {
            throw new IllegalArgumentException("User must be at least " + minimumAge + " years old to register.");
        }
        user.setId(counter.incrementAndGet());
        users.add(user);
        return user;
    }

    public User updateUser(Long id, User userUpdates){
        User user = users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElseThrow(()-> new RuntimeException("User not found"));
        return user;
    }

    public void deleteUser(Long id) {
        boolean removed = users.removeIf(u -> u.getId().equals(id));
        if (!removed) {
            throw new IllegalArgumentException("User not found.");
        }
    }

    public List<User> searchUserByBirthDate(LocalDate from,LocalDate to){
        if(from.isAfter(to)){
            throw new IllegalArgumentException("From date must be before To date");
        }
        return users.stream()
                .filter(user -> !user.getBirthDate().isBefore(from) && !user.getBirthDate().isAfter(to))
                .collect(Collectors.toList());
    }
}
