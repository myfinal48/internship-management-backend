package com._projects.internship.service.storage;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com._projects.internship.exceptions.core.StorageException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Profile("!test")
public class MinioApplicationStorageService implements ApplicationStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @PostConstruct
    public void init() {
        try {
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (MinioException e) {
            throw new StorageException("Impossible d'accéder ou de créer le bucket MinIO: " + bucketName, e);
        } catch (Exception e) {
            throw new StorageException("Erreur lors de l'initialisation de MinIO", e);
        }
    }

    @Override
    public String store(MultipartFile file, Long applicationId) {
        String original = StringUtils.cleanPath(file.getOriginalFilename());
        String objectName = "applications/" + applicationId + "/"
            + UUID.randomUUID() + "_" + original;
        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(in, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
            return objectName;
        } catch (Exception e) {
            throw new StorageException("Échec du stockage de '" + objectName + "' sur MinIO", e);
        }
    }

    @Override
    public byte[] load(String storagePath) {
        try (InputStream in = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(storagePath)
                    .build())) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new StorageException("Échec du chargement de '" + storagePath + "' depuis MinIO", e);
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(storagePath)
                    .build()
            );
        } catch (Exception e) {
            throw new StorageException("Échec de la suppression de '" + storagePath + "' sur MinIO", e);
        }
    }
}
