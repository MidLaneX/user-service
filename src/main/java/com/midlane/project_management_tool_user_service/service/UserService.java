package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.UserDTO;


import java.util.List;

public interface UserService {
    UserDTO createUser(UserDTO userDto);
    List<UserDTO> getAllUsers();
    UserDTO getUserById(String id);
    void deleteUser(String id);
}