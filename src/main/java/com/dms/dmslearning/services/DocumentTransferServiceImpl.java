package com.dms.dmslearning.services;


import com.dms.dmslearning.model.TokenResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class DocumentTransferServiceImpl implements DocumentTransferService {

    @Override
    public byte[] downloadFile(String fileName) throws JsonProcessingException {
        //get token
        //
        String token = getTickets("admin", "admin");

        //get nodeId
        String nodeId = getNodeId(fileName, token);

        //download file
        return downloadContent(nodeId, token);
    }

    @Override
    public void uploadFile(MultipartFile file) throws IOException {
        String token = getTickets("admin", "admin");
        String nodeId = getNodeId("mktest", token);
//        String response = uploadContent(file.getOriginalFilename(), file, nodeId, token);
        String s = uploadContent(file, token);
    }

    private String getTickets(String username, String password) {
        String ticketUrl = "http://localhost:8080/alfresco/api/-default-/public/authentication/versions/1/tickets";
        RestTemplate restTemplate = new RestTemplate();
//        HashMap<String, String> entity = new HashMap<>();
//        entity.put("accept", "application/json");
//        entity.put("Content-Type", "application/json");

        HashMap<String, String> body = new HashMap<>();
        body.put("userId", username);
        body.put("password", password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);


        ResponseEntity<TokenResponse> response = restTemplate.exchange(ticketUrl, HttpMethod.POST, requestEntity, TokenResponse.class);
        return response.getBody().getEntry().getId();
    }

    private String getNodeId(String fileName, String token) throws JsonProcessingException {
        String nodeUrl = "http://localhost:8080/alfresco/api/-default-/public/alfresco/versions/1/nodes/-root-/children?skipCount=0&maxItems=100&relativePath=/mktest";
        RestTemplate restTemplate = new RestTemplate();

        String encoded = new String(Base64.getEncoder().encode(token.getBytes()));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Authorization", "Basic " + encoded);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                nodeUrl,
                HttpMethod.GET,
                entity,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        JsonNode nodeId = jsonNode.get("list").get("entries").get(0).get("entry").get("id");

        return nodeId.textValue();
    }

    private byte[] downloadContent(String nodeId, String token) {

//        String contentUrl = "http://localhost:8080/alfresco/api/-default-/public/alfresco/versions/1/nodes/9f03253c-f8ed-4b0d-945e-ff1ecaefe371/content?attachment=true";
        String baseUrl = "http://localhost:8080";
        String nId = "43467fa5-c4cc-41a7-ac67-4c81b5206946";
        String contentUrl = baseUrl + "/alfresco/api/-default-/public/alfresco/versions/1/nodes/{nodeId}/content?attachment={attachment}";

        RestTemplate restTemplate = new RestTemplate();

//        String encoded = new String(Base64.getEncoder().encode(token.getBytes()));
        String encoded = Base64.getEncoder().encodeToString(("admin" + ":" + "admin").getBytes());

        // Set custom headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/octet-stream");
        headers.set("Authorization", "Basic " + encoded);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Set URL variables
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("nodeId", nId);
        urlVariables.put("attachment", String.valueOf(true));

        ResponseEntity<byte[]> response = restTemplate.exchange(
                contentUrl,
                HttpMethod.GET,
                entity,
                byte[].class,
                urlVariables
        );

        return response.getBody();
    }

//    private String uploadContent(String originalFilename, MultipartFile fileContent, String nodeId, String token) throws IOException {
//        String uploadUrl = "http://localhost:8080/alfresco/api/-default-/public/alfresco/versions/1/nodes/c8355fd7-9d1a-4d18-9ce4-0140a2de275a/children";
//    http://localhost:8080/alfresco/api/-default-/public/alfresco/versions/1/nodes/c8355fd7-9d1a-4d18-9ce4-0140a2de275a/children
//        String encoded = new String(Base64.getEncoder().encode(token.getBytes()));
////        headers.set("authorization", "Basic " + encoded);
//        // RestTemplate instance (can be moved outside for code reuse)
//        RestTemplate restTemplate = new RestTemplate();
//
//        // Create MultiValueMap for multipart request
//        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//
//        // Create Resource from byte array
//        Resource fileResource = new ByteArrayResource(fileContent.getBytes());
//
//        // Add file data to body
//        body.add("filedata", fileResource);
//
//        // Optional: Add other form data if needed
//        // body.add("description", "This is a test file");
//
//        // Set headers for multipart request
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//        headers.set("authorization", "Basic " + encoded);
//
//        // Create HttpEntity with body and headers
//        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//
//        // Send POST request
//        ResponseEntity<Object> exchange = restTemplate.exchange(uploadUrl, HttpMethod.POST, requestEntity, Object.class);
//
//        return "";
//    }

    public String uploadContent(MultipartFile file, String token) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        String alfrescoUrl = "http://localhost:8080/alfresco/api/-default-/public/alfresco/versions/1/nodes/c8355fd7-9d1a-4d18-9ce4-0140a2de275a/children";
        String username = "admin";
        String password = "admin";
        String filePath = "/Users/mahendrakundare/Documents/alfresco/community-docker-compose.yml"; // Replace with your actual file path

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        String authString = Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        headers.set("Authorization", "Basic " + authString);

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        File file1 = getFile(file);
        parts.add("filedata", new FileSystemResource(new File(filePath)));


        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(alfrescoUrl, HttpMethod.POST, requestEntity, String.class);
            // Handle the response based on your needs (e.g., check status code)
            System.out.println(response.getBody());
        } catch (RestClientResponseException e) {
            // Handle potential exceptions
            System.err.println("Error uploading file: " + e.getMessage());
        }
        return "";
    }

    @Override
    public String uploadFile(File file) throws IOException {
        return uploadFileToAlfresco(file);
    }

    public String uploadFileToAlfresco(File file) throws IOException {
        String alfrescoUrl = "http://localhost:8080/alfresco/api/-default-/public/alfresco/versions/1/nodes/c8355fd7-9d1a-4d18-9ce4-0140a2de275a/children";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        String authString = Base64.getEncoder().encodeToString(("admin" + ":" + "admin").getBytes());
        headers.set("Authorization", "Basic " + authString);

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("filedata", new FileSystemResource(file));
        headers.add("X-Original-Filename", "mkfille");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, headers);

        ResponseEntity<String> response = restTemplate.exchange(alfrescoUrl, HttpMethod.POST, requestEntity, String.class);

        if (response.getStatusCode() == HttpStatus.CREATED) {
            return "File uploaded successfully!";
        } else {
            return "Error uploading file: " + response.getStatusCodeValue();
        }
    }

    private File getFile(MultipartFile multipartFile) throws IOException {
        File tempFile = File.createTempFile("upload", ".tmp"); // Adjust extension based on file type
        multipartFile.transferTo(tempFile);

        return tempFile;

    }

}
