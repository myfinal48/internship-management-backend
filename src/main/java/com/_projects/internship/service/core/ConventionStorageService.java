package com._projects.internship.service.core;

import io.minio.*;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConventionStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    private static final String CONVENTIONS_PATH = "conventions/";
    private static final String SIGNED_CONVENTIONS_PATH = "signed-conventions/";

    private void ensureBucketExists() throws Exception {
        boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!bucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("Bucket '{}' créé avec succès", bucketName);
        }
    }

    public String storeFile(byte[] fileContent, String fileName, String contentType) {
        try {
            ensureBucketExists();
            String uniqueFileName = UUID.randomUUID() + "_" + fileName;
            String filePath = CONVENTIONS_PATH + uniqueFileName;

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

            log.info("Fichier '{}' stocké avec succès", filePath);
            return filePath;

        } catch (Exception e) {
            log.error("Erreur lors du stockage du fichier '{}'", fileName, e);
            throw new RuntimeException("Erreur lors du stockage du fichier", e);
        }
    }

    public byte[] getFile(String filePath) {
        try {
            GetObjectResponse response = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build()
            );
            return response.readAllBytes();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du fichier '{}'", filePath, e);
            throw new RuntimeException("Erreur lors de la récupération du fichier", e);
        }
    }

    public String saveSignedConvention(Long conventionId, MultipartFile file) {
        // Vérification du fichier
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier ne peut pas être vide");
        }
        
        if (file.getContentType() == null || !"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("Le fichier doit être au format PDF");
        }
        
        String path = SIGNED_CONVENTIONS_PATH + "convention_" + conventionId + ".pdf";

        try {
            ensureBucketExists();

            try (InputStream is = file.getInputStream()) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(path)
                                .stream(is, file.getSize(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            log.info("Convention signée ID: {} stockée avec succès sous '{}'", conventionId, path);
            return path;

        } catch (Exception e) {
            log.error("Erreur lors de l'upload du PDF signé pour la convention ID: {}", conventionId, e);
            throw new RuntimeException("Erreur lors de l'upload du PDF signé", e);
        }
    }

    public void deleteFile(String filePath) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build()
            );
            log.info("Fichier '{}' supprimé avec succès", filePath);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du fichier '{}'", filePath, e);
            throw new RuntimeException("Erreur lors de la suppression du fichier", e);
        }
    }
}
