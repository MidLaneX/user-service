package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.UserRequestDTO;
import com.midlane.project_management_tool_user_service.dto.UserResponseDTO;

import java.util.List;

public interface UserService {
    UserResponseDTO createUser(UserRequestDTO userDto);
    List<UserResponseDTO> getAllUsers();
    UserResponseDTO getUserById(String id);
    void deleteUser(String id);
}
