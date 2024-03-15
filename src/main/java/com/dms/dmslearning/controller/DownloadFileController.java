package com.dms.dmslearning.controller;

import com.dms.dmslearning.services.DocumentTransferService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/message")
    public String getMessage() {
        return "Hello World";
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity downloadFile(@PathVariable String filename) throws JsonProcessingException {
        byte[] file = documentTransferService.downloadFile(filename);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(file);
    }


//    @PostMapping("/uploadFile")
//    public ResponseEntity uploadFile(@RequestParam("filedata") MultipartFile file) throws IOException {
//        downloadService.uploadFile(file);
//        return ResponseEntity.ok().body("success");
//    }

    @PostMapping("/uploadFile")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        String originalFilename = multipartFile.getOriginalFilename();
        File tempFile = new File(System.getProperty("java.io.tmpdir") + File.separator + originalFilename);
        multipartFile.transferTo(tempFile);

        String message = documentTransferService.uploadFile(tempFile);

        if (!tempFile.delete()) {
            System.err.println("Failed to delete temporary file: " + tempFile.getAbsolutePath());
        }

        return ResponseEntity.ok(message);
    }
}
