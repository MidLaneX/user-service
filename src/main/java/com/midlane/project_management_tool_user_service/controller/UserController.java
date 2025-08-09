package com.midlane.project_management_tool_user_service.controller;

import com.midlane.project_management_tool_user_service.dto.CreateUserRequest;
import com.midlane.project_management_tool_user_service.dto.UpdateUserProfileRequest;
import com.midlane.project_management_tool_user_service.dto.UserResponse;
import com.midlane.project_management_tool_user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        UserResponse user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/auth-service-id/{authServiceUserId}")
    public ResponseEntity<UserResponse> getUserByAuthServiceUserId(@PathVariable Long authServiceUserId) {
        UserResponse user = userService.getUserByAuthServiceUserId(authServiceUserId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile/{email}")
    public ResponseEntity<UserResponse> updateUserProfile(
            @PathVariable String email,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        UserResponse response = userService.updateUserProfile(email, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile-complete/{email}")
    public ResponseEntity<Boolean> isProfileComplete(@PathVariable String email) {
        boolean isComplete = userService.isProfileComplete(email);
        return ResponseEntity.ok(isComplete);
    }
}
