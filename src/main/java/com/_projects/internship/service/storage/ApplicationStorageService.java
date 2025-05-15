package com._projects.internship.service.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Gère le stockage des CV et lettres de motivation.
 */
public interface ApplicationStorageService {
    /**
     * Stocke un fichier (CV ou lettre) et renvoie le chemin ou l'URL.
     */
    String store(MultipartFile file, Long applicationId);
    
    /**
     * Charge un fichier sous forme de bytes pour téléchargement.
     */
    byte[] load(String storagePath);
    
    /**
     * Supprime le fichier stocké.
     */
    void delete(String storagePath);
}
