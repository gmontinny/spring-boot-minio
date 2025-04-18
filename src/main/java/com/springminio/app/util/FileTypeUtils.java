package com.springminio.app.util;

import cn.hutool.core.io.FileTypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class FileTypeUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileTypeUtils.class);

    private static final String IMAGE_TYPE = "image/";
    private static final String AUDIO_TYPE = "audio/";
    private static final String VIDEO_TYPE = "video/";
    private static final String APPLICATION_TYPE = "application/";
    private static final String TXT_TYPE = "text/";

    private static final Map<Predicate<String>, String> TYPE_MAPPINGS = Map.of(
            type -> type.equalsIgnoreCase("JPG") ||
                    type.equalsIgnoreCase("JPEG") ||
                    type.equalsIgnoreCase("GIF") ||
                    type.equalsIgnoreCase("PNG") ||
                    type.equalsIgnoreCase("BMP") ||
                    type.equalsIgnoreCase("PCX") ||
                    type.equalsIgnoreCase("TGA") ||
                    type.equalsIgnoreCase("PSD") ||
                    type.equalsIgnoreCase("TIFF"), IMAGE_TYPE,

            type -> type.equalsIgnoreCase("mp3") ||
                    type.equalsIgnoreCase("OGG") ||
                    type.equalsIgnoreCase("WAV") ||
                    type.equalsIgnoreCase("REAL") ||
                    type.equalsIgnoreCase("APE") ||
                    type.equalsIgnoreCase("MODULE") ||
                    type.equalsIgnoreCase("MIDI") ||
                    type.equalsIgnoreCase("VQF") ||
                    type.equalsIgnoreCase("CD"), AUDIO_TYPE,

            type -> type.equalsIgnoreCase("mp4") ||
                    type.equalsIgnoreCase("avi") ||
                    type.equalsIgnoreCase("MPEG-1") ||
                    type.equalsIgnoreCase("RM") ||
                    type.equalsIgnoreCase("ASF") ||
                    type.equalsIgnoreCase("WMV") ||
                    type.equalsIgnoreCase("qlv") ||
                    type.equalsIgnoreCase("MPEG-2") ||
                    type.equalsIgnoreCase("MPEG4") ||
                    type.equalsIgnoreCase("mov") ||
                    type.equalsIgnoreCase("3gp"), VIDEO_TYPE,

            type -> type.equalsIgnoreCase("doc") ||
                    type.equalsIgnoreCase("docx") ||
                    type.equalsIgnoreCase("ppt") ||
                    type.equalsIgnoreCase("pptx") ||
                    type.equalsIgnoreCase("xls") ||
                    type.equalsIgnoreCase("xlsx") ||
                    type.equalsIgnoreCase("zip") ||
                    type.equalsIgnoreCase("jar"), APPLICATION_TYPE,

            type -> type.equalsIgnoreCase("txt"), TXT_TYPE
    );

    public static String getFileType(MultipartFile multipartFile) {
        return Optional.ofNullable(multipartFile)
                .map(file -> {
                    try (var inputStream = file.getInputStream()) {
                        String type = FileTypeUtil.getType(inputStream);
                        LOGGER.info("FileTypeUtils | getFileType | type : {}", type);

                        return TYPE_MAPPINGS.entrySet().stream()
                                .filter(entry -> entry.getKey().test(type))
                                .map(entry -> {
                                    String result = entry.getValue() + type;
                                    LOGGER.info("FileTypeUtils | getFileType | detected type : {}", result);
                                    return result;
                                })
                                .findFirst()
                                .orElse(null);

                    } catch (IOException e) {
                        LOGGER.error("FileTypeUtils | getFileType | IOException : {}", e.getMessage());
                        return null;
                    }
                })
                .orElse(null);
    }
}