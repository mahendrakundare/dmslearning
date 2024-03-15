package com.dms.dmslearning.controller;

import com.dms.dmslearning.services.DocumentTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class DownloadFileController {

    @Autowired
    DocumentTransferService documentTransferService;

    @Value("${custom.using-sdk}")
    private boolean usingSDK;

    @GetMapping("/message")
    public String getMessage() {
        return "Hello World";
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity downloadFile(@PathVariable String filename) throws IOException {
        byte[] file = documentTransferService.downloadFile(filename, usingSDK);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(file);
    }

    @PostMapping("/uploadFile")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        String message = "";

        if (usingSDK) {
            message = documentTransferService.uploadMultipartFile(multipartFile);

        } else {
        String originalFilename = multipartFile.getOriginalFilename();
        File tempFile = new File(System.getProperty("java.io.tmpdir") + File.separator + originalFilename);
        multipartFile.transferTo(tempFile);
        message = documentTransferService.uploadFile(tempFile);

        if (!tempFile.delete()) {
            System.err.println("Failed to delete temporary file: " + tempFile.getAbsolutePath());
        }
    }

        return ResponseEntity.ok(message);
    }

    @PostMapping("/folder")
    public ResponseEntity<String> createFolder() throws Exception {

        String response = documentTransferService.createFolder();

        return ResponseEntity.ok(response);
    }
}
