package io.cockroachdb.pest.web.api.cluster;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.cockroachdb.pest.model.ApplicationProperties;

@RestController
@RequestMapping("/api/cluster/certs")
public class CertificateController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final List<String> allowedExtensions = List.of("crt", "key");

    private static final List<String> allowedContentTypes = List.of(
            "application/x-x509-ca-cert",
            "application/octet-stream");

    @Autowired
    private ApplicationProperties applicationProperties;

    @PostMapping
    public ResponseEntity<String> uploadCerts(@RequestParam("files") MultipartFile[] files)
            throws UncheckedIOException {
        for (MultipartFile file : files) {
            validateFile(file);
            uploadFile(file);
        }

        return ResponseEntity.ok("Files received successfully");
    }

    private void validateFile(MultipartFile file) {
        logger.debug("Upload file name: %s, size: %s, type: %s"
                .formatted(file.getOriginalFilename(), file.getSize(), file.getContentType()));

        String fileName = Objects.requireNonNull(file.getOriginalFilename());

        int idx = fileName.lastIndexOf('.');
        if (idx > 0) {
            String ext = fileName.substring(idx + 1);
            if (!allowedExtensions.contains(ext)) {
                throw new UploadException("Illegal file extension: " + ext);
            }
        }

        String contentType = file.getContentType();
        if (!allowedContentTypes.contains(contentType)) {
            throw new UploadException("Illegal content type: " + contentType);
        }
    }

    private boolean uploadFile(MultipartFile file) throws UncheckedIOException {
        Path targetPath = applicationProperties.getDirectories().getCertsDirPath()
                .resolve(Objects.requireNonNull(file.getOriginalFilename()));
        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            Files.setPosixFilePermissions(targetPath, PosixFilePermissions.fromString("rwx------"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return true;
    }
}
