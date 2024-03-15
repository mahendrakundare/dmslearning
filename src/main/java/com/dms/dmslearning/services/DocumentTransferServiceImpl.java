package com.dms.dmslearning.services;


import com.dms.dmslearning.model.TokenResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.core.handler.NodesApi;
import org.alfresco.core.model.Node;
import org.alfresco.core.model.NodeBodyCreate;
import org.alfresco.core.model.NodeEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Component
public class DocumentTransferServiceImpl implements DocumentTransferService {

    @Autowired
    private NodesApi nodesApi;

    @Value("${custom.folder-name}")
    private String folderName;

    @Override
    public byte[] downloadFile(String fileName, boolean usesdk) throws IOException {
        if (usesdk) {
            return getContentUsingSDK(fileName);
        } else {
            //get token
            String token = getTickets("admin", "admin");
            //get nodeId
            String nodeId = getNodeId(fileName, token);
            //download file
            return downloadContent(nodeId, token);
        }
    }

    @Override
    public String uploadMultipartFile(MultipartFile file) throws IOException {
//        String token = getTickets("admin", "admin");
//        String nodeId = getNodeId("mktest", token);
//        String response = uploadContent(file.getOriginalFilename(), file, nodeId, token);
//        String s = uploadContent(file, token);
        return uploadContentUsingSDK(file);
    }

    @Override
    public String uploadFile(File file) throws IOException {
        return uploadFileToAlfresco(file);
    }

    @Override
    public String createFolder() {
        Objects.requireNonNull(folderName);

        String rootPath = "-root-";       // /Company Home
        String folderType = "cm:folder";  // Standard out-of-the-box folder type

        List<String> folderAspects = new ArrayList<String>();
        folderAspects.add("cm:titled");
        Map<String, String> folderProps = new HashMap<>();
        folderProps.put("cm:title", folderName);
        folderProps.put("cm:description", "Folder to upload content");

        String nodeId = rootPath; // The id of a node. You can also use one of these well-known aliases: -my-, -shared-, -root-
        NodeBodyCreate nodeBodyCreate = new NodeBodyCreate();
        nodeBodyCreate.setName(folderName);
        nodeBodyCreate.setNodeType(folderType);
        nodeBodyCreate.setAspectNames(folderAspects);
        nodeBodyCreate.setProperties(folderProps);

        List<String> include = null;
        List<String> fields = null;
        Boolean autoRename = true;
        Boolean majorVersion = false;
        // Should versioning be enabled at all?
        Boolean versioningEnabled = false;
        Node folderNode = null;

        folderNode = nodesApi.createNode(nodeId, nodeBodyCreate, autoRename, majorVersion, versioningEnabled,
                    include, fields).getBody().getEntry();


        return "Created new folder:" + folderNode.getName();
    }

    private String getTickets(String username, String password) {
        String ticketUrl = "http://localhost:8080/alfresco/api/-default-/public/authentication/versions/1/tickets";
        RestTemplate restTemplate = new RestTemplate();

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
        String relativePath = String.format("/%s/%s", folderName, fileName);
        //to get list of items from folder
//        String nodeUrl = "http://localhost:8080/alfresco/api/-default-/public/alfresco/versions/1/nodes/-root-/children?skipCount=0&maxItems=100&relativePath=/mktest";
        //to get exact details of the items or node
        String nodeUrl = "http://localhost:8080/alfresco/api/-default-/public/alfresco/versions/1/nodes/-root-?relativePath={relativePath}";
        RestTemplate restTemplate = new RestTemplate();

        String encoded = new String(Base64.getEncoder().encode(token.getBytes()));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Authorization", "Basic " + encoded);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Set URL variables
        Map<String, String> urlVariables = new HashMap<>();
        urlVariables.put("relativePath", relativePath);

        ResponseEntity<String> response = restTemplate.exchange(
                nodeUrl,
                HttpMethod.GET,
                entity,
                String.class,
                urlVariables
        );

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        JsonNode nodeId = jsonNode.get("entry").get("id");

        return nodeId.textValue();
    }

    private byte[] downloadContent(String nodeId, String token) {
        String baseUrl = "http://localhost:8080";
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
        urlVariables.put("nodeId", nodeId);
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

    public byte[] getContentUsingSDK(String fileName) throws IOException {
        String nodeId = "";
        String relativePath = String.format("/%s/%s", folderName, fileName);


        ResponseEntity<NodeEntry> node = nodesApi.getNode("-root-", null, relativePath, null);

        if (node.getStatusCode().is2xxSuccessful()) {
            nodeId = node.getBody().getEntry().getId();
        }

        ResponseEntity<Resource> response = nodesApi.getNodeContent(nodeId, true, null, null);

            return response.getBody().getContentAsByteArray();
    }

    public String uploadContentUsingSDK(MultipartFile file) {
        String parentFolderId = "-root-";
        String fileName = file.getOriginalFilename();
        String title = "dockertypefile";
        String description = "this is docker file";
        String relativeFolderPath = String.format("/%s/%s", folderName);

        Node fileNode = createFileMetadata(parentFolderId, fileName, title, description, relativeFolderPath);

        // Get the file bytes
        byte[] fileData = null;
        try {
            fileData = file.getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Add the file node content
        Node updatedFileNode = nodesApi.updateNodeContent(fileNode.getId(),
                fileData, true, null, null, null, null).getBody().getEntry();


        return "success";
    }

    private Node createFileMetadata(String parentFolderId, String fileName, String title, String description,
                                    String relativeFolderPath) {
        List<String> fileAspects = new ArrayList<String>();
        fileAspects.add("cm:titled");
        Map<String, String> fileProps = new HashMap<>();
        fileProps.put("cm:title", title);
        fileProps.put("cm:description", description);

        NodeBodyCreate nodeBodyCreate = new NodeBodyCreate();
        nodeBodyCreate.setName(fileName);
        nodeBodyCreate.setNodeType("cm:content");
        nodeBodyCreate.setAspectNames(fileAspects);
        nodeBodyCreate.setProperties(fileProps);
        nodeBodyCreate.setRelativePath(relativeFolderPath);

        // Create the file node metadata
        Node fileNode = nodesApi.createNode(parentFolderId, nodeBodyCreate, true, true, true,
                null, null).getBody().getEntry();

        return fileNode;
    }

}
