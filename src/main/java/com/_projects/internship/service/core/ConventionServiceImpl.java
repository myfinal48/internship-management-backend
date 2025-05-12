package com._projects.internship.service.core;

import com._projects.internship.dto.core.ConventionRequestDTO;
import com._projects.internship.dto.core.ConventionResponseDTO;
import com._projects.internship.mapper.core.ConventionMapper;
import com._projects.internship.model.core.ConventionEntity;
import com._projects.internship.model.core.ConventionStatus;
import com._projects.internship.repository.core.ConventionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ConventionServiceImpl implements ConventionService {

    private final ConventionRepository repository;
    private final ConventionMapper mapper;

    @Override
    public ConventionRequestDTO createConvention(ConventionRequestDTO dto) {
        // Implémentation à compléter
        return dto;
    }

    @Override
    public ConventionRequestDTO updateConvention(ConventionRequestDTO dto) {
        // Implémentation à compléter
        return dto;
    }

    @Override
    public ConventionResponseDTO createFromApplication(Long applicationId) {
        ConventionEntity entity = new ConventionEntity();
        entity.setStudentId(1L);
        entity.setCompanyId(1L);
        entity.setCreationDate(LocalDate.now());
        entity.setStatus(ConventionStatus.PENDING);

        entity = repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Override
    public ConventionResponseDTO validateByTeacher(Long id) {
        ConventionEntity entity = repository.findById(id).orElseThrow();
        entity.setStatus(ConventionStatus.VALIDATED_BY_TEACHER);
        entity.setRejectionReason(null);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public ConventionResponseDTO rejectByTeacher(Long id, String reason) {
        ConventionEntity entity = repository.findById(id).orElseThrow();
        entity.setStatus(ConventionStatus.REJECTED_BY_TEACHER);
        entity.setRejectionReason(reason);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public ConventionResponseDTO approveByAdmin(Long id) {
        ConventionEntity entity = repository.findById(id).orElseThrow();
        entity.setStatus(ConventionStatus.APPROVED_BY_ADMIN);
        entity.setRejectionReason(null);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public ConventionResponseDTO rejectByAdmin(Long id, String reason) {
        ConventionEntity entity = repository.findById(id).orElseThrow();
        entity.setStatus(ConventionStatus.REJECTED_BY_ADMIN);
        entity.setRejectionReason(reason);
        return mapper.toResponse(repository.save(entity));
    }
}
