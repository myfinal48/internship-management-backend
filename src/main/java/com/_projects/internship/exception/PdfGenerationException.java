package com._projects.internship.exception;

/**
 * Exception spécifique pour les erreurs liées à la génération de documents PDF
 */
public class PdfGenerationException extends RuntimeException {

    public PdfGenerationException(String message) {
        super(message);
    }

    public PdfGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}