package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.controller.handler.ResourceNotFoundException;
import com.midlane.project_management_tool_user_service.dto.UserRequestDTO;
import com.midlane.project_management_tool_user_service.dto.UserResponseDTO;
import com.midlane.project_management_tool_user_service.model.User;
import com.midlane.project_management_tool_user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private UserResponseDTO mapToResponseDTO(User user) {
        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    private User mapToEntity(UserRequestDTO dto) {
        return User.builder()
                .name(dto.name())
                .email(dto.email())
                .role(dto.role())
                .build();
    }

    @Override
    public UserResponseDTO createUser(UserRequestDTO dto) {
        log.info("Creating user with email: {}", dto.email());
        if (userRepository.existsByEmail(dto.email())) {
            log.warn("Attempted to create user with existing email: {}", dto.email());
            throw new IllegalArgumentException("Email already exists");
        }
        User savedUser = userRepository.save(mapToEntity(dto));
        log.info("User created with id: {}", savedUser.getId());
        return mapToResponseDTO(savedUser);
    }

    @Override
    public List<UserResponseDTO> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO getUserById(String id) {
        log.info("Fetching user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return mapToResponseDTO(user);
    }

    @Override
    public void deleteUser(String id) {
        log.info("Deleting user by id: {}", id);
        if (!userRepository.existsById(id)) {
            log.warn("Attempted to delete non-existing user with id: {}", id);
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
        log.info("User deleted with id: {}", id);
    }
}
