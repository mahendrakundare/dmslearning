## Content Management Service

This is a simple content management services that allows you to upload and download content from alfresco servers. It is built using 
  
- Springboot

### What you’ll need
+ A favorite text editor or IDE
+ JDK 21
+ Install Gradle
+ Docker - for setting up Alfresco Community Edition

The server starts at port 8090


### Setup Alfresco server
Deploy Community Edition, including the repository, Share, Postgres database, Search Services, etc. docker compose file is present inside docker folder (for first time it will take some to pull and setup all the required images):

More details on setup alfresco server can be found here [Install using Docker Compose](https://docs.alfresco.com/content-services/community/install/containers/docker-compose/)



```bash
cd docker

docker-compose -f community-docker-compose.yml up -d
```
#### Once all the services up and running you can check them individually for us share service is required so we can login here [Alfresco Share](http://127.0.0.1:8080/share/page/)

####  Create folder using below command and configure the same in to ```application.properties custom.folder-name: {foldername}```

```bash
curl -X 'POST' \
  'http://localhost:8080/alfresco/api/-default-/public/alfresco/versions/1/nodes/-root-/children?autoRename=true' \
  -H 'accept: application/json' \
  -H 'authorization: Basic YWRtaW46YWRtaW4=' \
  -H 'Content-Type: application/json' \
  -d '{
  "name":"{folderName}",
  "nodeType":"cm:folder"
}'
```

### Run

```bash
 ./gradlew :bootRun
```

## Curl Commands

### Upload Content
```bash
curl --location 'http://localhost:8090/api/uploadFile' \
--form 'file=@"{replace with file path}"'
```

### Download Content
```bash
curl  'http://localhost:8090/api/download/{filenamewithextention}' --output filenamewithextention
```

### Note This service performs operations using both way  
+ Through Rest APIs (find more here [Alfresco Content Services REST API](https://api-explorer.alfresco.com/api-explorer/))
+ Through SDK provided by alfresco (find more here [Alfresco Java SDK](https://github.com/Alfresco/alfresco-java-sdk))

In order to use any of the above approach simply toggle the flag inside ```appliation.yml custom.using-sdk=true```