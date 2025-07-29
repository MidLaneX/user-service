package com.midlane.project_management_tool_user_service.controller;

import com.midlane.project_management_tool_user_service.dto.UserRequestDTO;
import com.midlane.project_management_tool_user_service.dto.UserResponseDTO;
import com.midlane.project_management_tool_user_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "User CRUD operations and management")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Create a new user", description = "Create a new user with profile information")
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO userRequestDTO) {
        log.info("Creating user with email: {}", userRequestDTO.email());
        UserResponseDTO createdUser = userService.createUser(userRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @Operation(summary = "Get all users", description = "Retrieve all users in the system")
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        log.info("Fetching all users");
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get users with pagination", description = "Retrieve users with pagination support")
    @GetMapping("/paginated")
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(Pageable pageable) {
        log.info("Fetching users with pagination");
        Page<UserResponseDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get user by ID", description = "Retrieve a specific user by their unique ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(
            @Parameter(description = "User ID") @PathVariable String id) {
        log.info("Fetching user with ID: {}", id);
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Get user by email", description = "Retrieve a user by their email address")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> getUserByEmail(
            @Parameter(description = "User email") @PathVariable String email) {
        log.info("Fetching user with email: {}", email);
        UserResponseDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Get user by username", description = "Retrieve a user by their username")
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponseDTO> getUserByUsername(
            @Parameter(description = "Username") @PathVariable String username) {
        log.info("Fetching user with username: {}", username);
        UserResponseDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Update user", description = "Update an existing user's information")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @Parameter(description = "User ID") @PathVariable String id,
            @Valid @RequestBody UserRequestDTO userRequestDTO) {
        log.info("Updating user with ID: {}", id);
        UserResponseDTO updatedUser = userService.updateUser(id, userRequestDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Delete user", description = "Delete a user from the system")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID") @PathVariable String id) {
        log.info("Deleting user with ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Toggle user status", description = "Activate or deactivate a user")
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<UserResponseDTO> toggleUserStatus(
            @Parameter(description = "User ID") @PathVariable String id) {
        log.info("Toggling status for user with ID: {}", id);
        UserResponseDTO user = userService.toggleUserStatus(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Update last login", description = "Update the last login time for a user")
    @PatchMapping("/{id}/last-login")
    public ResponseEntity<Void> updateLastLogin(
            @Parameter(description = "User ID") @PathVariable String id) {
        log.info("Updating last login for user with ID: {}", id);
        userService.updateLastLogin(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Search users", description = "Search users by name or email")
    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDTO>> searchUsers(
            @Parameter(description = "Search term") @RequestParam String query) {
        log.info("Searching users with query: {}", query);
        List<UserResponseDTO> users = userService.searchUsers(query);
        return ResponseEntity.ok(users);
    }
}
