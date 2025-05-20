package com._projects.internship.service.core;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

/**
 * Service pour le stockage de fichiers de conventions avec MinIO/S3
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConventionStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    /**
     * Stocke un fichier de convention dans MinIO/S3
     * @param fileContent Le contenu du fichier
     * @param fileName Le nom du fichier
     * @param contentType Le type MIME du fichier
     * @return Le chemin d'accès au fichier stocké
     */
    public String storeFile(byte[] fileContent, String fileName, String contentType) {
        try {
            // Vérifier si le bucket existe, sinon le créer
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Bucket '{}' créé avec succès", bucketName);
            }

            // Générer un nom de fichier unique
            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
            String filePath = "conventions/" + uniqueFileName;

            // Télécharger le fichier
            try (InputStream inputStream = new ByteArrayInputStream(fileContent)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(filePath)
                                .stream(inputStream, fileContent.length, -1)
                                .contentType(contentType)
                                .build()
                );
            }

            log.info("Fichier de convention '{}' téléchargé avec succès", filePath);
            return filePath;
        } catch (Exception e) {
            log.error("Erreur lors du stockage du fichier de convention", e);
            throw new RuntimeException("Erreur lors du stockage du fichier de convention", e);
        }
    }

    /**
     * Récupère un fichier de convention depuis MinIO/S3
     * @param filePath Le chemin d'accès au fichier
     * @return Le contenu du fichier
     */
    public byte[] getFile(String filePath) {
        try {
            GetObjectResponse response = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build()
            );

            // Lire le contenu du fichier
            return response.readAllBytes();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du fichier de convention '{}'", filePath, e);
            throw new RuntimeException("Erreur lors de la récupération du fichier de convention", e);
        }
    }

    /**
     * Sauvegarde une convention signée (PDF) dans MinIO/S3
     * @param conventionId L'ID de la convention
     * @param file Le fichier PDF signé
     * @return Le chemin d'accès au fichier stocké
     */
    public String saveSignedConvention(Long conventionId, MultipartFile file) {
        String path = "signed-conventions/convention_" + conventionId + ".pdf";
        try {
            // Vérifier si le bucket existe, sinon le créer
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Bucket '{}' créé avec succès", bucketName);
            }
            
            try (InputStream is = file.getInputStream()) {
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(path)
                        .stream(is, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
                );
                log.info("Convention signée ID: {} téléchargée avec succès", conventionId);
            }
            return path;
        } catch (Exception e) {
            log.error("Erreur lors de l'upload du PDF signé pour la convention ID: {}", conventionId, e);
            throw new RuntimeException("Erreur lors de l'upload du PDF signé", e);
        }
    }
// ...existing code...

    /**
     * Supprime un fichier de convention de MinIO/S3
     * @param filePath Le chemin d'accès au fichier
     */
    public void deleteFile(String filePath) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build()
            );
            log.info("Fichier de convention '{}' supprimé avec succès", filePath);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du fichier de convention '{}'", filePath, e);
            throw new RuntimeException("Erreur lors de la suppression du fichier de convention", e);
        }
    }
}