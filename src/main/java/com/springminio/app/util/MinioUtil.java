package com.springminio.app.util;

import com.springminio.app.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class MinioUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinioUtil.class);

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;


    @SneakyThrows
    public void putObject(String bucketName, MultipartFile multipartFile, String filename, String fileType) {
        LOGGER.info("MinioUtil | putObject is called");
        LOGGER.info("MinioUtil | putObject | filename : {}", filename);
        LOGGER.info("MinioUtil | putObject | fileType : {}", fileType);

        Optional.of(multipartFile)
                .map(file -> {
                    try {
                        return new ByteArrayInputStream(file.getBytes());
                    } catch (IOException e) {
                        LOGGER.error("Erro ao ler arquivo: ", e);
                        return null;
                    }
                })
                .ifPresent(inputStream -> {
                    try {
                        minioClient.putObject(
                                PutObjectArgs.builder()
                                        .bucket(bucketName)
                                        .object(filename)
                                        .stream(inputStream, -1, minioConfig.getFileSize())
                                        .contentType(fileType)
                                        .build()
                        );
                    } catch (Exception e) {
                        LOGGER.error("Erro ao fazer upload do arquivo: ", e);
                    }
                });
    }

    @SneakyThrows
    public boolean bucketExists(String bucketName) {
        LOGGER.info("MinioUtil | bucketExists is called");

        return Optional.of(bucketName)
                .map(bucket -> {
                    try {
                        boolean found = minioClient.bucketExists(
                                BucketExistsArgs.builder()
                                        .bucket(bucket)
                                        .build()
                        );
                        LOGGER.info("MinioUtil | bucketExists | found : {}", found);
                        LOGGER.info("MinioUtil | bucketExists | message : {} {}",
                                bucket, found ? "exists" : "does not exist");
                        return found;
                    } catch (Exception e) {
                        LOGGER.error("Erro ao verificar existência do bucket: ", e);
                        return false;
                    }
                })
                .orElse(false);
    }

    // Create bucket name
    @SneakyThrows
    public boolean makeBucket(String bucketName) {
        LOGGER.info("MinioUtil | makeBucket is called");

        return Optional.of(bucketName)
                .filter(bucket -> !bucketExists(bucket))
                .map(bucket -> {
                    try {
                        minioClient.makeBucket(
                                MakeBucketArgs.builder()
                                        .bucket(bucket)
                                        .build());
                        LOGGER.info("MinioUtil | makeBucket | Bucket criado com sucesso: {}", bucket);
                        return true;
                    } catch (Exception e) {
                        LOGGER.error("Erro ao criar bucket: ", e);
                        return false;
                    }
                })
                .orElseGet(() -> {
                    LOGGER.info("MinioUtil | makeBucket | Bucket já existe: {}", bucketName);
                    return false;
                });
    }


    // List all buckets
    @SneakyThrows
    public List<Bucket> listBuckets() {
        LOGGER.info("MinioUtil | listBuckets is called");

        return minioClient.listBuckets();
    }

    // List all bucket names
    @SneakyThrows
    public List<String> listBucketNames() {
        LOGGER.info("MinioUtil | listBucketNames is called");

        return Optional.ofNullable(listBuckets())
                .map(buckets -> {
                    List<String> bucketNames = buckets.stream()
                            .map(Bucket::name)
                            .collect(Collectors.toList());

                    LOGGER.info("MinioUtil | listBucketNames | Total de buckets encontrados: {}", bucketNames.size());
                    return bucketNames;
                })
                .orElseGet(() -> {
                    LOGGER.warn("MinioUtil | listBucketNames | Nenhum bucket encontrado");
                    return new ArrayList<>();
                });
    }


    // List all objects from the specified bucket
    @SneakyThrows
    public Optional<Iterable<Result<Item>>> listObjects(String bucketName) {
        LOGGER.info("MinioUtil | listObjects is called");

        return Optional.of(bucketName)
                .filter(this::bucketExists)
                .map(bucket -> {
                    LOGGER.info("MinioUtil | listObjects | bucket exists");
                    return minioClient.listObjects(
                            ListObjectsArgs.builder()
                                    .bucket(bucket)
                                    .build());
                });
    }

    // Delete Bucket by its name from the specified bucket
    @SneakyThrows
    public boolean removeBucket(String bucketName) {
        LOGGER.info("MinioUtil | removeBucket is called");
        return Optional.of(bucketName)
                .filter(this::bucketExists)
                .map(bucket -> {
                    try {
                        boolean isEmpty = StreamSupport.stream(listObjects(bucket).orElse(List.of()).spliterator(), false)
                                .map(result -> {
                                    try {
                                        return result.get();
                                    } catch (Exception e) {
                                        LOGGER.error("Erro ao obter item: ", e);
                                        return null;
                                    }
                                })
                                .filter(Objects::nonNull)
                                .allMatch(item -> item.size() == 0);

                        if (!isEmpty) {
                            LOGGER.info("MinioUtil | removeBucket | Bucket não está vazio");
                            return false;
                        }

                        minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucket).build());
                        return !bucketExists(bucket);
                    } catch (Exception e) {
                        LOGGER.error("Erro ao remover bucket: ", e);
                        return false;
                    }
                })
                .orElse(false);
    }

    // List all object names from the specified bucket
    @SneakyThrows
    public List<String> listObjectNames(String bucketName) {
        LOGGER.info("MinioUtil | listObjectNames is called");

        return Optional.of(bucketName)
                .filter(this::bucketExists)
                .map(bucket -> {
                    LOGGER.info("MinioUtil | listObjectNames | bucket exists");
                    return listObjects(bucket)
                            .map(objects -> StreamSupport.stream(objects.spliterator(), false)
                                    .map(result -> {
                                        try {
                                            return result.get().objectName();
                                        } catch (Exception e) {
                                            LOGGER.error("Erro ao obter nome do objeto: ", e);
                                            return null;
                                        }
                                    })
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList()))
                            .orElse(new ArrayList<>());
                })
                .orElseGet(() -> {
                    LOGGER.info("MinioUtil | listObjectNames | bucket does not exist");
                    return List.of("Bucket does not exist");
                });
    }


    // Delete object from the specified bucket
    @SneakyThrows
    public boolean removeObject(String bucketName, String objectName) {
        LOGGER.info("MinioUtil | removeObject is called");

        return Optional.of(bucketName)
                .filter(this::bucketExists)
                .map(bucket -> {
                    try {
                        minioClient.removeObject(
                                RemoveObjectArgs.builder()
                                        .bucket(bucket)
                                        .object(objectName)
                                        .build()
                        );
                        LOGGER.info("MinioUtil | removeObject | objeto removido com sucesso");
                        return true;
                    } catch (Exception e) {
                        LOGGER.error("Erro ao remover objeto: ", e);
                        return false;
                    }
                })
                .orElseGet(() -> {
                    LOGGER.info("MinioUtil | removeObject | bucket não existe");
                    return false;
                });
    }


    // Get file path from the specified bucket
    @SneakyThrows
    public String getObjectUrl(String bucketName, String objectName) {
        LOGGER.info("MinioUtil | getObjectUrl is called");

        return Optional.of(bucketName)
                .filter(this::bucketExists)
                .map(bucket -> {
                    try {
                        String url = minioClient.getPresignedObjectUrl(
                                GetPresignedObjectUrlArgs.builder()
                                        .method(Method.GET)
                                        .bucket(bucket)
                                        .object(objectName)
                                        .expiry(2, TimeUnit.MINUTES)
                                        .build());
                        LOGGER.info("MinioUtil | getObjectUrl | url : {}", url);
                        return url;
                    } catch (Exception e) {
                        LOGGER.error("Erro ao obter URL do objeto: ", e);
                        return "";
                    }
                })
                .orElseGet(() -> {
                    LOGGER.info("MinioUtil | getObjectUrl | bucket não existe");
                    return "";
                });
    }


    // Get metadata of the object from the specified bucket
    @SneakyThrows
    public Optional<StatObjectResponse> statObject(String bucketName, String objectName) {
        LOGGER.info("MinioUtil | statObject is called");

        return Optional.of(bucketName)
                .filter(this::bucketExists)
                .map(bucket -> {
                    try {
                        StatObjectResponse stat = minioClient.statObject(
                                StatObjectArgs.builder()
                                        .bucket(bucket)
                                        .object(objectName)
                                        .build());
                        LOGGER.info("MinioUtil | statObject | stat : {}", stat);
                        return stat;
                    } catch (Exception e) {
                        LOGGER.error("Erro ao obter estatísticas do objeto: ", e);
                        return null;
                    }
                })
                .map(Optional::ofNullable)
                .orElseGet(Optional::empty);
    }


    // Get a file object as a stream from the specified bucket
    @SneakyThrows
    public Optional<InputStream> getObject(String bucketName, String objectName) {
        LOGGER.info("MinioUtil | getObject is called");

        return Optional.of(bucketName)
                .filter(this::bucketExists)
                .flatMap(bucket -> statObject(bucket, objectName))
                .filter(stat -> stat.size() > 0)
                .map(stat -> {
                    try {
                        InputStream stream = minioClient.getObject(
                                GetObjectArgs.builder()
                                        .bucket(bucketName)
                                        .object(objectName)
                                        .build()
                        );
                        LOGGER.info("MinioUtil | getObject | stream obtido com sucesso");
                        return stream;
                    } catch (Exception e) {
                        LOGGER.error("Erro ao obter objeto: ", e);
                        return null;
                    }
                });
    }


    // Delete multiple file objects from the specified bucket
    @SneakyThrows
    public boolean removeObject(String bucketName, List<String> objectNames) {
        LOGGER.info("MinioUtil | removeObject is called");

        return Optional.of(bucketName)
                .filter(this::bucketExists)
                .map(bucket -> {
                    try {
                        List<DeleteObject> deleteObjects = objectNames.stream()
                                .map(DeleteObject::new)
                                .collect(Collectors.toList());

                        Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                                RemoveObjectsArgs.builder()
                                        .bucket(bucket)
                                        .objects(deleteObjects)
                                        .build()
                        );

                        boolean hasErrors = StreamSupport.stream(results.spliterator(), false)
                                .map(result -> {
                                    try {
                                        DeleteError error = result.get();
                                        LOGGER.info("MinioUtil | removeObject | erro ao remover objeto: {} - {}",
                                                error.objectName(), error.message());
                                        return true;
                                    } catch (Exception e) {
                                        LOGGER.error("Erro ao processar resultado da remoção: ", e);
                                        return true;
                                    }
                                })
                                .anyMatch(hasError -> hasError);

                        return !hasErrors;
                    } catch (Exception e) {
                        LOGGER.error("Erro ao remover objetos: ", e);
                        return false;
                    }
                })
                .orElseGet(() -> {
                    LOGGER.info("MinioUtil | removeObject | bucket não existe");
                    return false;
                });
    }

}
