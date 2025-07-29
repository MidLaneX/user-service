package com.midlane.project_management_tool_user_service.repository;

import com.midlane.project_management_tool_user_service.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findByIsActiveTrue();

    List<User> findByStatus(User.UserStatus status);


    @Query("{'$or': [" +
           "{'firstName': {'$regex': ?0, '$options': 'i'}}, " +
           "{'lastName': {'$regex': ?0, '$options': 'i'}}, " +
           "{'email': {'$regex': ?0, '$options': 'i'}}, " +
           "{'username': {'$regex': ?0, '$options': 'i'}}" +
           "]}")
    List<User> searchByNameOrEmail(String searchTerm);
}