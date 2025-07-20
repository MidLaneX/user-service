package com.midlane.project_management_tool_user_service.exception;

public class NonTransientException extends RuntimeException {
    public NonTransientException(String message) {
        super(message);
    }
}