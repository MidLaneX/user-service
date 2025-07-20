package com.midlane.project_management_tool_user_service.repository;


import com.midlane.project_management_tool_user_service.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
    boolean existsByEmail(String email);

}