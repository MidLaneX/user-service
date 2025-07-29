package com.midlane.project_management_tool_user_service.service;

import com.midlane.project_management_tool_user_service.dto.UserRequestDTO;
import com.midlane.project_management_tool_user_service.dto.UserResponseDTO;
import com.midlane.project_management_tool_user_service.exception.UserNotFoundException;
import com.midlane.project_management_tool_user_service.exception.DuplicateResourceException;
import com.midlane.project_management_tool_user_service.model.User;
import com.midlane.project_management_tool_user_service.repository.UserRepository;
import com.midlane.project_management_tool_user_service.util.ModelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        log.info("Creating user with email: {}", userRequestDTO.email());

        // Check for duplicate email
        if (userRepository.existsByEmail(userRequestDTO.email())) {
            throw new DuplicateResourceException("User with email already exists: " + userRequestDTO.email());
        }

        // Check for duplicate username
        if (userRepository.existsByUsername(userRequestDTO.username())) {
            throw new DuplicateResourceException("User with username already exists: " + userRequestDTO.username());
        }

        User user = modelMapper.mapToUser(userRequestDTO);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        if (user.getStatus() == null) {
            user.setStatus(User.UserStatus.ACTIVE);
        }

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return modelMapper.mapToUserResponseDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll()
                .stream()
                .map(modelMapper::mapToUserResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        log.info("Fetching users with pagination: {}", pageable);
        return userRepository.findAll(pageable)
                .map(modelMapper::mapToUserResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(String id) {
        log.info("Fetching user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        return modelMapper.mapToUserResponseDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return modelMapper.mapToUserResponseDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUserByUsername(String username) {
        log.info("Fetching user with username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        return modelMapper.mapToUserResponseDTO(user);
    }

    @Override
    public UserResponseDTO updateUser(String id, UserRequestDTO userRequestDTO) {
        log.info("Updating user with ID: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        // Check for duplicate email (if email is being changed)
        if (!existingUser.getEmail().equals(userRequestDTO.email()) &&
            userRepository.existsByEmail(userRequestDTO.email())) {
            throw new DuplicateResourceException("User with email already exists: " + userRequestDTO.email());
        }

        // Check for duplicate username (if username is being changed)
        if (!existingUser.getUsername().equals(userRequestDTO.username()) &&
            userRepository.existsByUsername(userRequestDTO.username())) {
            throw new DuplicateResourceException("User with username already exists: " + userRequestDTO.username());
        }

        modelMapper.updateUserFromDTO(existingUser, userRequestDTO);
        existingUser.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(existingUser);
        log.info("User updated successfully with ID: {}", updatedUser.getId());

        return modelMapper.mapToUserResponseDTO(updatedUser);
    }

    @Override
    public void deleteUser(String id) {
        log.info("Deleting user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with ID: " + id);
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully with ID: {}", id);
    }

    @Override
    public UserResponseDTO toggleUserStatus(String id) {
        log.info("Toggling status for user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        user.setActive(!user.isActive());
        user.setStatus(user.isActive() ? User.UserStatus.ACTIVE : User.UserStatus.INACTIVE);
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        log.info("User status toggled successfully. ID: {}, Active: {}", id, updatedUser.isActive());

        return modelMapper.mapToUserResponseDTO(updatedUser);
    }

    @Override
    public void updateLastLogin(String id) {
        log.info("Updating last login for user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        log.info("Last login updated successfully for user ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> searchUsers(String searchTerm) {
        log.info("Searching users with term: {}", searchTerm);
        return userRepository.searchByNameOrEmail(searchTerm)
                .stream()
                .map(modelMapper::mapToUserResponseDTO)
                .collect(Collectors.toList());
    }
}
