package com.midlane.project_management_tool_user_service.controller;

import com.midlane.project_management_tool_user_service.dto.UserDTO;
import com.midlane.project_management_tool_user_service.dto.PasswordResetRequest;
import com.midlane.project_management_tool_user_service.dto.UpdateUserProfileRequest;
import com.midlane.project_management_tool_user_service.exception.ErrorResponse;
import com.midlane.project_management_tool_user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/auth/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getUserProfile(@PathVariable Long userId) {
        try {
            UserDTO userProfile = userService.getUserProfile(userId);
            return ResponseEntity.ok(userProfile);
        } catch (RuntimeException ex) {
            ErrorResponse error = new ErrorResponse("GET_PROFILE_ERROR", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PutMapping("/profile/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        try {
            UserDTO updatedUser = userService.updateUserProfile(userId, request);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException ex) {
            ErrorResponse error = new ErrorResponse("UPDATE_PROFILE_ERROR", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PutMapping("/reset-password/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> resetPassword(
            @PathVariable Long userId,
            @Valid @RequestBody PasswordResetRequest request) {
        try {
            userService.resetPassword(userId, request.getNewPassword());
            return ResponseEntity.ok("Password reset successfully");
        } catch (RuntimeException ex) {
            ErrorResponse error = new ErrorResponse("RESET_PASSWORD_ERROR", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}
