package com.multi.loyaltybackend.service;

import com.multi.loyaltybackend.exception.FileStorageException;
import com.multi.loyaltybackend.exception.InvalidFilePathException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class ImageStorageService {

    private final Path storageDir = Paths.get("images");

    public ImageStorageService() {
        try {
            Files.createDirectories(storageDir);
        } catch (IOException e) {
            throw new FileStorageException("Failed to create upload directory", e);
        }
    }

    public String storeFile(MultipartFile file) {
        String extension = getFileExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + extension;

        try {
            Path target = storageDir.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new FileStorageException("ფაილის შენახვისას მოხდა შეცდომა", e);
        }
    }

    public String getFilePath(String fileName) {
        if (fileName == null) {
            return null;
        }

        Path path = storageDir.resolve(fileName).normalize();
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();

        try {
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                return baseUrl + "/api/images/" + fileName;
            } else {
                throw new FileStorageException("ფაილი ვერ მოძებნა");
            }
        } catch (MalformedURLException e) {
            throw new InvalidFilePathException("არავალიდური გაილის მისამართი: " + e);
        }
    }

    public void deleteFile(String fileName) {
        try {
            Path path = storageDir.resolve(fileName).normalize();
            List<String> defaultsFileNames = List.of("default-company.png", "default-event.png", "default-profile.png");

            if (!defaultsFileNames.contains(fileName)) {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            throw new FileStorageException("ფაილის წაშლისას მოხდა შეცდომა", e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return (dot >= 0) ? filename.substring(dot) : "";
    }
}
