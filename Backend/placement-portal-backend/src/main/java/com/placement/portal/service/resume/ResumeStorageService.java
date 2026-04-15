package com.placement.portal.service.resume;

import com.placement.portal.domain.Resume;
import com.placement.portal.domain.StudentProfile;
import com.placement.portal.dto.response.ResumeDto;
import com.placement.portal.exception.EntityNotFoundException;
import com.placement.portal.mapper.EntityMapper;
import com.placement.portal.repository.ResumeRepository;
import com.placement.portal.repository.StudentProfileRepository;
import com.placement.portal.util.AppConstants;
import com.placement.portal.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeStorageService {

    private final ResumeRepository resumeRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final EntityMapper entityMapper;
    private final SecurityUtils securityUtils;

    @Value("${app.upload.base-path:uploads/resumes}")
    private String uploadBasePath;

    /**
     * Validates, stores, and persists a résumé file.
     *
     * @param file             the uploaded file
     * @param studentProfileId the owning student profile UUID
     * @return the persisted {@link ResumeDto}
     * @throws IllegalArgumentException if the MIME type is not allowed or the file exceeds the size limit
     */
    @Transactional
    public ResumeDto uploadResume(MultipartFile file, String studentProfileId) {
        validateFile(file);

        StudentProfile student = studentProfileRepository.findById(studentProfileId)
                .orElseThrow(() -> new EntityNotFoundException("StudentProfile", studentProfileId));

        String originalName = sanitize(file.getOriginalFilename());
        String storedName   = UUID.randomUUID() + "-" + originalName;
        Path   dir          = Paths.get(uploadBasePath, studentProfileId);
        Path   target       = dir.resolve(storedName);

        try {
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), target);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store résumé file", e);
        }

        Resume resume = Resume.builder()
                .studentProfile(student)
                .fileName(originalName)
                .storagePath(target.toString())
                .fileSizeBytes(file.getSize())
                .contentType(file.getContentType())
                .isPrimary(false)
                .build();

        return entityMapper.toResumeDto(resumeRepository.save(resume));
    }

    /**
     * Returns all résumés belonging to the currently authenticated student.
     */
    @Transactional(readOnly = true)
    public List<ResumeDto> getMyResumes() {
        String userId = securityUtils.getCurrentUserId();
        StudentProfile student = studentProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "StudentProfile for user " + userId + " not found"));

        return resumeRepository.findByStudentProfileId(student.getId())
                .stream()
                .map(entityMapper::toResumeDto)
                .collect(Collectors.toList());
    }

    /**
     * Marks the given résumé as primary and clears the primary flag on all others.
     */
    @Transactional
    public void setPrimaryResume(String resumeId) {
        Resume target = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new EntityNotFoundException("Resume", resumeId));

        String studentProfileId = target.getStudentProfile().getId();

        // Clear existing primary flag
        resumeRepository.findByStudentProfileId(studentProfileId)
                .forEach(r -> {
                    if (r.isPrimary()) {
                        r.setPrimary(false);
                        resumeRepository.save(r);
                    }
                });

        target.setPrimary(true);
        resumeRepository.save(target);
        log.info("Resume {} set as primary for student {}", resumeId, studentProfileId);
    }

    /**
     * Deletes the physical file and the database record for a résumé.
     */
    @Transactional
    public void deleteResume(String resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new EntityNotFoundException("Resume", resumeId));

        Path filePath = Paths.get(resume.getStoragePath());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete résumé file at " + filePath, e);
        }

        resumeRepository.delete(resume);
        log.info("Resume {} deleted", resumeId);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        if (file.getSize() > AppConstants.MAX_RESUME_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File size exceeds the maximum allowed size of "
                            + (AppConstants.MAX_RESUME_FILE_SIZE / (1024 * 1024)) + " MB");
        }
        String contentType = file.getContentType();
        boolean allowed = contentType != null
                && Arrays.asList(AppConstants.ALLOWED_RESUME_TYPES).contains(contentType);
        if (!allowed) {
            throw new IllegalArgumentException(
                    "Unsupported file type '" + contentType
                            + "'. Allowed types: PDF, DOC, DOCX");
        }
    }

    private String sanitize(String fileName) {
        if (fileName == null || fileName.isBlank()) return "resume";
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
