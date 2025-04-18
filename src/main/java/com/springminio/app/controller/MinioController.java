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
import java.util.Optional;

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

        return Optional.ofNullable(file)
                .filter(f -> !f.isEmpty())
                .map(f -> {
                    String fileType = Optional.ofNullable(FileTypeUtils.getFileType(f))
                            .orElseThrow(() -> new FileResponseException("Tipo de arquivo não suportado"));
                    return minioService.putObject(f, bucketName, fileType);
                })
                .orElseThrow(() -> new FileResponseException("Arquivo não pode estar vazio"));
    }


    @PostMapping("/addBucket/{bucketName}")
    @Operation(summary = "Criar novo bucket", description = "Cria um novo bucket no MinIO com o nome especificado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bucket criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Falha ao criar o bucket")
    })
    public String addBucket(
            @Parameter(description = "Nome do bucket a ser criado") @PathVariable String bucketName) {

        LOGGER.info("MinioController | addBucket is called");
        LOGGER.info("MinioController | addBucket | bucketName : " + bucketName);

        minioService.makeBucket(bucketName);
        return "Bucket name " + bucketName + " created";
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
    @Operation(summary = "Listar objetos do bucket", description = "Lista todos os objetos contidos em um bucket específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de objetos retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Bucket não encontrado")
    })
    public List<String> show(
            @Parameter(description = "Nome do bucket para listar os objetos") @PathVariable String bucketName) {
        LOGGER.info("MinioController | show is called");
        LOGGER.info("MinioController | show | bucketName : " + bucketName);
        return minioService.listObjectNames(bucketName);
    }

    @GetMapping("/showBucketName")
    @Operation(summary = "Listar buckets", description = "Lista todos os buckets disponíveis no MinIO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de buckets retornada com sucesso")
    })
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
    @Operation(summary = "Remover objeto", description = "Remove um objeto específico de um bucket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Objeto removido com sucesso"),
            @ApiResponse(responseCode = "400", description = "Falha ao remover objeto")
    })
    public String delObject(
            @Parameter(description = "Nome do bucket") @PathVariable("bucketName") String bucketName,
            @Parameter(description = "Nome do objeto") @PathVariable("objectName") String objectName) {

        LOGGER.info("MinioController | delObject | bucketName: {}, objectName: {}", bucketName, objectName);

        return Optional.of(minioService.removeObject(bucketName, objectName))
                .map(success -> success ? DELETE_SUCCESS : DELETE_FAILED)
                .orElse(DELETE_FAILED);
    }


    @DeleteMapping("/removeListObject/{bucketName}")
    @Operation(summary = "Remover lista de objetos", description = "Remove múltiplos objetos de um bucket específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Objetos removidos com sucesso"),
            @ApiResponse(responseCode = "400", description = "Falha ao remover objetos")
    })
    public String delListObject(
            @Parameter(description = "Nome do bucket") @PathVariable("bucketName") String bucketName,
            @Parameter(description = "Lista de nomes dos objetos a serem removidos") @RequestBody List<String> objectNameList) {

        LOGGER.info("MinioController | delListObject | bucketName: {}, quantidade de objetos: {}",
                bucketName, objectNameList.size());

        return Optional.of(objectNameList)
                .filter(list -> !list.isEmpty())
                .map(list -> minioService.removeListObject(bucketName, list))
                .map(success -> success ? DELETE_SUCCESS : DELETE_FAILED)
                .orElse(DELETE_FAILED);
    }


    @GetMapping("/showListObjectNameAndDownloadUrl/{bucketName}")
    @Operation(summary = "Listar objetos com URLs de download",
            description = "Retorna um mapa com os nomes dos objetos e suas respectivas URLs de download")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Mapa de objetos e URLs retornado com sucesso"),
            @ApiResponse(responseCode = "404",
                    description = "Bucket não encontrado"),
            @ApiResponse(responseCode = "500",
                    description = "Erro interno ao processar a requisição")
    })
    public Map<String, String> showListObjectNameAndDownloadUrl(
            @Parameter(description = "Nome do bucket para listar os objetos")
            @PathVariable String bucketName) {

        LOGGER.info("MinioController | showListObjectNameAndDownloadUrl | bucketName : {}", bucketName);

        String baseUrl = "localhost:" + portNumber + "/minio/download/" + bucketName + "/";
        LOGGER.info("MinioController | showListObjectNameAndDownloadUrl | baseUrl : {}", baseUrl);

        return minioService.listObjectNames(bucketName).stream()
                .collect(HashMap::new,
                        (map, objectName) -> map.put(objectName, baseUrl + objectName),
                        HashMap::putAll);
    }


    @GetMapping("/download/{bucketName}/{objectName}")
    @Operation(summary = "Download de objeto", description = "Realiza o download de um objeto específico do bucket")
    public void download(
            HttpServletResponse response,
            @Parameter(description = "Nome do bucket") @PathVariable String bucketName,
            @Parameter(description = "Nome do objeto") @PathVariable String objectName) {

        LOGGER.info("MinioController | download is called");
        LOGGER.info("MinioController | download | bucketName : {}", bucketName);


        InputStream inputStream = minioService.downloadObject(bucketName, objectName)
                .orElseThrow(() -> new FileResponseException("Arquivo não encontrado"));

        try (InputStream in = inputStream) {
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

