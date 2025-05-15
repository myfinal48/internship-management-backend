package com._projects.internship.exceptions.core;

/**
 * Exception générique levée lorsqu'une erreur survient
 * lors du stockage ou de la récupération d'un fichier.
 */
public class StorageException extends RuntimeException {

    /**
     * Crée une StorageException avec un message.
     * 
     * @param message description de l'erreur
     */
    public StorageException(String message) {
        super(message);
    }

    /**
     * Crée une StorageException avec un message et une cause.
     * 
     * @param message description de l'erreur
     * @param cause   exception sous-jacente
     */
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
