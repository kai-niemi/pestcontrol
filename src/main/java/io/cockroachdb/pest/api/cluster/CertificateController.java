package io.cockroachdb.pest.api.cluster;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.cockroachdb.pest.model.ApplicationProperties;

@RestController
@RequestMapping("/api/cluster")
public class CertificateController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final List<String> allowedExtensions = List.of("crt", "key");

    private static final List<String> allowedContentTypes = List.of(MediaType.APPLICATION_OCTET_STREAM_VALUE);

    @Autowired
    private ApplicationProperties applicationProperties;

    @PostMapping("/certs")
    public ResponseEntity<String> uploadCerts(@RequestParam("files") MultipartFile[] files)
            throws UncheckedIOException {
        for (MultipartFile file : files) {
            validateFile(file);
            uploadFile(file);
        }
        return ResponseEntity.ok("Files received successfully!");
    }

    private void validateFile(MultipartFile file) {
        logger.debug("Upload file name: %s, size: %s, type: %s"
                .formatted(file.getOriginalFilename(), file.getSize(),  file.getContentType()));

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

    private void uploadFile(MultipartFile file) throws UncheckedIOException {
        Path targetPath = applicationProperties.getCertsDirectory().resolve(
                Objects.requireNonNull(file.getOriginalFilename()));
        if (!Files.exists(targetPath)) {
            try {
                Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            logger.info("File already exists (skip): %s".formatted(targetPath));
        }
    }
}
