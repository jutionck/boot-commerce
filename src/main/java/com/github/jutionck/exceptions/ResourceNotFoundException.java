package com.github.jutionck.exceptions;

public class ResourceNotFoundException extends RuntimeException{
    // implemented: "student not found with id 1"
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
