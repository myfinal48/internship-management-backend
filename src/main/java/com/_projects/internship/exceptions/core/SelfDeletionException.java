package com._projects.internship.exceptions.core;

public class SelfDeletionException extends RuntimeException {
    public SelfDeletionException(String message) {
        super(message);
    }
}