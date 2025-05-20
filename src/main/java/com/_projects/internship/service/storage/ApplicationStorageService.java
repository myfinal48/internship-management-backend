package com._projects.internship.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ApplicationStorageService {
    String store(MultipartFile file, Long applicationId);
    byte[] load(String storagePath);
    void delete(String storagePath);
}
