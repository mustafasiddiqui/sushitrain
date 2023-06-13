package com.example.sushitrain.controller;

import com.example.sushitrain.scanner.TrayScannerImpl;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class FileUploadController {

    @PostMapping("/uploadFile")
    public int handleFileUpload(@RequestParam MultipartFile file) {
        try {
            String uploadedFilePath = convertMultipartFileToFile(file);
            TrayScannerImpl scanner = new TrayScannerImpl();
            int retVal = scanner.scanTrays(uploadedFilePath);
            deleteFile(uploadedFilePath);
            return retVal;
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error when uploading file");
        }
    }

    private void deleteFile(String uploadedFilePath) throws IOException {
        Files.delete(Paths.get(uploadedFilePath));
    }

    private String convertMultipartFileToFile(MultipartFile file) throws IOException {
        String defaultBaseDir = System.getProperty("java.io.tmpdir");
        Path path = Paths.get(defaultBaseDir, file.getOriginalFilename());
        File convertedFile = path.toFile();

        FileOutputStream fos = new FileOutputStream(convertedFile);
        fos.write(file.getBytes());
        fos.close();

        return convertedFile.getAbsolutePath();
    }

}
