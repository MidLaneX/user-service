package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.UserRequestDTO;
import com.midlane.project_management_tool_user_service.dto.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    /**
     * Create a new user
     */
    UserResponseDTO createUser(UserRequestDTO userRequestDTO);

    /**
     * Get all users
     */
    List<UserResponseDTO> getAllUsers();

    /**
     * Get users with pagination
     */
    Page<UserResponseDTO> getAllUsers(Pageable pageable);

    /**
     * Get user by ID
     */
    UserResponseDTO getUserById(String id);

    /**
     * Get user by email
     */
    UserResponseDTO getUserByEmail(String email);

    /**
     * Get user by username
     */
    UserResponseDTO getUserByUsername(String username);

    /**
     * Update user
     */
    UserResponseDTO updateUser(String id, UserRequestDTO userRequestDTO);

    /**
     * Delete user
     */
    void deleteUser(String id);

    /**
     * Activate/Deactivate user
     */
    UserResponseDTO toggleUserStatus(String id);

    /**
     * Update last login time
     */
    void updateLastLogin(String id);

    /**
     * Search users by name or email
     */
    List<UserResponseDTO> searchUsers(String searchTerm);
}
