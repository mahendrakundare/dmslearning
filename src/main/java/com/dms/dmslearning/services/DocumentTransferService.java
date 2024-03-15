package com.dms.dmslearning.services;


import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public interface DocumentTransferService {

    byte[] downloadFile(String fileName, boolean usesdk) throws IOException;

    String uploadMultipartFile(MultipartFile multipartFile) throws IOException;

    String uploadFile(File fileName) throws IOException;
}
