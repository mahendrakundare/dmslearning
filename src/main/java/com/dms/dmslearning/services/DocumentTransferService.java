package com.dms.dmslearning.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public interface DocumentTransferService {

    byte[] downloadFile(String fileName) throws JsonProcessingException;

    void uploadFile(MultipartFile fileName) throws IOException;
    String uploadFile(File fileName) throws IOException;
}
