package com._projects.internship.service.storage;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Primary
@Profile("test")
public class DummyApplicationStorageService implements ApplicationStorageService {
    @Override
    public String store(MultipartFile file, Long applicationId) {
        // No-op for tests
        return "dummy-path";
    }

    @Override
    public byte[] load(String storagePath) {
        // Return empty or dummy data for tests
        return new byte[0];
    }

    @Override
    public void delete(String storagePath) {
        // No-op for tests
    }

    // Implement other methods as needed with dummy logic
} 