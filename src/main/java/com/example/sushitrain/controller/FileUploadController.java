package com.example.sushitrain.controller;

import com.example.sushitrain.scanner.TrayScannerImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class FileUploadController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/uploadFile")
    public String listUploadedFiles(Model model) {
        return "uploadForm";
    }

    @PostMapping("/uploadFile")
    public String handleFileUpload(@RequestParam MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            String uploadedFilePath = convertMultipartFileToFile(file);
            TrayScannerImpl scanner = new TrayScannerImpl();
            int retVal = scanner.scanTrays(uploadedFilePath);
            deleteFile(uploadedFilePath);
            redirectAttributes
                    .addFlashAttribute("result", retVal)
                    .addFlashAttribute("successfulUpload", true);
            return "redirect:/";
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
