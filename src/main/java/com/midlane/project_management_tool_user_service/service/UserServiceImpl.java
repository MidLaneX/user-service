package com.midlane.project_management_tool_user_service.service;



import com.midlane.project_management_tool_user_service.dto.UserDTO;
import com.midlane.project_management_tool_user_service.exception.ResourceNotFoundException;
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

    private UserDTO mapToDTO(User user) {
        return new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    private User mapToEntity(UserDTO dto) {
        return User.builder()
                .id(dto.id())
                .name(dto.name())
                .email(dto.email())
                .role(dto.role())
                .build();
    }

    @Override
    public UserDTO createUser(UserDTO dto) {
        log.info("Creating user with email: {}", dto.email());
        if (userRepository.existsByEmail(dto.email())) {
            log.warn("Attempted to create user with existing email: {}", dto.email());
            throw new IllegalArgumentException("Email already exists");
        }
        User savedUser = userRepository.save(mapToEntity(dto));
        log.info("User created with id: {}", savedUser.getId());
        return mapToDTO(savedUser);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(String id) {
        log.info("Fetching user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return mapToDTO(user);
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
