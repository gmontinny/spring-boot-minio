package com.springminio.app.controller;

import com.springminio.app.exception.FileResponseException;
import com.springminio.app.payload.FileResponse;
import com.springminio.app.service.MinioService;
import com.springminio.app.util.FileTypeUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/minio")
@Tag(name = "Minio Controller", description = "API para gerenciamento de arquivos no MinIO")
public class MinioController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinioController.class);
    private final MinioService minioService;
    private static final String DELETE_SUCCESS = "Operação de deleção realizada com sucesso";
    private static final String DELETE_FAILED = "Falha na operação de deleção";


    @Value("${server.port}")
    private int portNumber;

    @PostMapping("/upload")
    @Operation(summary = "Upload de arquivo", description = "Realiza o upload de um arquivo para um bucket específico")
    public FileResponse uploadFile(
            @Parameter(description = "Arquivo a ser enviado") @RequestParam("file") MultipartFile file,
            @Parameter(description = "Nome do bucket") @RequestParam String bucketName) {

        LOGGER.info("MinioController | uploadFile | bucketName : {}", bucketName);

        if (file.isEmpty()) {
            throw new FileResponseException("Arquivo não pode estar vazio");
        }

        String fileType = FileTypeUtils.getFileType(file);
        if (fileType == null) {
            throw new FileResponseException("Tipo de arquivo não suportado");
        }

        return minioService.putObject(file, bucketName, fileType);
    }


    @PostMapping("/addBucket/{bucketName}")
    public String addBucket(@PathVariable String bucketName) {

        LOGGER.info("MinioController | addBucket is called");

        LOGGER.info("MinioController | addBucket | bucketName : " + bucketName);

        minioService.makeBucket(bucketName);
        return "Bucket name "+ bucketName +" created";
    }

    @GetMapping("/showURL/{bucketName}/{objectName}")
    @Operation(summary = "Obter URL do objeto", description = "Retorna a URL de acesso para um objeto específico no bucket")
    public String showObjectUrl(
            @Parameter(description = "Nome do bucket") @PathVariable String bucketName,
            @Parameter(description = "Nome do objeto") @PathVariable String objectName) {

        LOGGER.info("MinioController | showObjectUrl is called");
        return minioService.getObjectUrl(bucketName, objectName);
    }


    @GetMapping("/show/{bucketName}")
    public List<String> show(@PathVariable String bucketName) {

        LOGGER.info("MinioController | show is called");

        LOGGER.info("MinioController | show | bucketName : " + bucketName);

        return minioService.listObjectNames(bucketName);
    }

    @GetMapping("/showBucketName")
    public List<String> showBucketName() {

        LOGGER.info("MinioController | showBucketName is called");

        return minioService.listBucketName();
    }

    @DeleteMapping("/removeBucket/{bucketName}")
    @Operation(summary = "Remover bucket", description = "Remove um bucket específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bucket removido com sucesso"),
            @ApiResponse(responseCode = "400", description = "Falha ao remover bucket")
    })
    public String delBucketName(@Parameter(description = "Nome do bucket") @PathVariable String bucketName) {
        LOGGER.info("MinioController | delBucketName | bucketName : {}", bucketName);
        return minioService.removeBucket(bucketName) ? DELETE_SUCCESS : DELETE_FAILED;
    }


    @DeleteMapping("/removeObject/{bucketName}/{objectName}")
    public String delObject(@PathVariable("bucketName") String bucketName, @PathVariable("objectName") String objectName) {

        LOGGER.info("MinioController | delObject is called");

        LOGGER.info("MinioController | delObject | bucketName : " + bucketName);
        LOGGER.info("MinioController | delObject | objectName : " + objectName);

        boolean state =  minioService.removeObject(bucketName, objectName);

        LOGGER.info("MinioController | delBucketName | state : " + state);

        if(state){
            return " Delete Object successfully ";
        }else {
            return " Delete failed ";
        }
    }

    @DeleteMapping("/removeListObject/{bucketName}")
    public String delListObject(@PathVariable("bucketName") String bucketName, @RequestBody List<String> objectNameList) {

        LOGGER.info("MinioController | delListObject is called");

        LOGGER.info("MinioController | delListObject | bucketName : " + bucketName);
        LOGGER.info("MinioController | delListObject | objectNameList size : " + objectNameList.size());

        boolean state =  minioService.removeListObject(bucketName, objectNameList) ;

        LOGGER.info("MinioController | delBucketName | state : " + state);

        if(state){
            return " Delete List Object successfully ";
        }else {
            return " Delete failed ";
        }
    }

    @GetMapping("/showListObjectNameAndDownloadUrl/{bucketName}")
    public Map<String, String> showListObjectNameAndDownloadUrl(@PathVariable String bucketName) {

        LOGGER.info("MinioController | showListObjectNameAndDownloadUrl is called");

        LOGGER.info("MinioController | showListObjectNameAndDownloadUrl | bucketName : " + bucketName);

        Map<String, String> map = new HashMap<>();
        List<String> listObjectNames = minioService.listObjectNames(bucketName);

        LOGGER.info("MinioController | showListObjectNameAndDownloadUrl | listObjectNames size : " + listObjectNames.size());

        String url = "localhost:" + portNumber + "/minio/download/" + bucketName + "/";
        LOGGER.info("MinioController | showListObjectNameAndDownloadUrl | url : " + url);

        for (int i = 0; i <listObjectNames.size() ; i++) {
            map.put(listObjectNames.get(i),url+listObjectNames.get(i));
        }

        LOGGER.info("MinioController | showListObjectNameAndDownloadUrl | map : " + map.toString());

        return map;
    }

    @GetMapping("/download/{bucketName}/{objectName}")
    @Operation(summary = "Download de objeto", description = "Realiza o download de um objeto específico do bucket")
    public void download(
            HttpServletResponse response,
            @Parameter(description = "Nome do bucket") @PathVariable String bucketName,
            @Parameter(description = "Nome do objeto") @PathVariable String objectName) {

        LOGGER.info("MinioController | download is called");
        LOGGER.info("MinioController | download | bucketName : {}", bucketName);
        LOGGER.info("MinioController | download | objectName : {}", objectName);

        try (InputStream in = minioService.downloadObject(bucketName, objectName)) {
            response.setHeader("Content-Disposition", "attachment;filename="
                    + URLEncoder.encode(objectName, "UTF-8"));
            response.setCharacterEncoding("UTF-8");
            IOUtils.copy(in, response.getOutputStream());
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Erro de codificação ao fazer download do arquivo: {}", e.getMessage(), e);
            throw new FileResponseException("Erro ao processar nome do arquivo");
        } catch (IOException e) {
            LOGGER.error("Erro de I/O ao fazer download do arquivo: {}", e.getMessage(), e);
            throw new FileResponseException("Erro ao fazer download do arquivo");
        }
    }
}
