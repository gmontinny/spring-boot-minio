package com.springminio.app.controller;

import com.springminio.app.exception.FileResponseException;
import com.springminio.app.payload.FileResponse;
import com.springminio.app.service.MinioService;
import com.springminio.app.util.FileTypeUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MinioController.class)
public class MinioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MinioService minioService;

    @MockitoBean
    private FileTypeUtils fileTypeUtils;


    @MockitoBean
    private Logger logger;

    private final String FILENAME = "test.txt";
    private final String CONTENT = "Test Content";
    private final String BUCKET_NAME = "test-bucket";
    private final String CONTENT_TYPE = "text/plain";


    @Test
    void uploadFile_SuccessfulUpload_ReturnsFileResponse() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                FILENAME,
                MediaType.TEXT_PLAIN_VALUE,
                CONTENT.getBytes()
        );

        FileResponse mockedResponse = FileResponse.builder()
                .filename(FILENAME)
                .contentType(CONTENT_TYPE)
                .fileSize((long) CONTENT.getBytes().length)
                .createdTime(LocalDateTime.now())
                .build();

        // Corrigindo o mock estático
        MockedStatic<FileTypeUtils> fileTypeUtilsMock = mockStatic(FileTypeUtils.class);
        fileTypeUtilsMock.when(() -> FileTypeUtils.getFileType(any())).thenReturn(CONTENT_TYPE);

        when(minioService.putObject(mockFile, BUCKET_NAME, CONTENT_TYPE))
                .thenReturn(mockedResponse);

        try {
            mockMvc.perform(MockMvcRequestBuilders.multipart("/minio/upload")
                            .file(mockFile)
                            .param("bucketName", BUCKET_NAME))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.filename", is(FILENAME)))
                    .andExpect(jsonPath("$.contentType", is(CONTENT_TYPE)))
                    .andExpect(jsonPath("$.fileSize", is((int) CONTENT.getBytes().length)));
        } finally {
            fileTypeUtilsMock.close();
        }
    }

    @Test
    void uploadFile_EmptyFile_ThrowsFileResponseException() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", MediaType.TEXT_PLAIN_VALUE, new byte[0]);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/minio/upload")
                        .file(emptyFile)
                        .param("bucketName", BUCKET_NAME))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadFile_UnsupportedFileType_ThrowsFileResponseException() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.unsupported",
                MediaType.TEXT_PLAIN_VALUE,
                CONTENT.getBytes()
        );

        // Criando o mock estático corretamente
        try (MockedStatic<FileTypeUtils> fileTypeUtilsMock = mockStatic(FileTypeUtils.class)) {
            fileTypeUtilsMock.when(() -> FileTypeUtils.getFileType(any()))
                    .thenThrow(new FileResponseException("Tipo de arquivo não suportado"));

            mockMvc.perform(MockMvcRequestBuilders.multipart("/minio/upload")
                            .file(mockFile)
                            .param("bucketName", BUCKET_NAME))
                    .andExpect(status().isBadRequest());
        }
    }

}