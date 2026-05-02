package com.fm.foodmanagementsystem.core.services.imps;

import com.fm.foodmanagementsystem.core.exception.SystemException;
import com.fm.foodmanagementsystem.core.exception.enums.SystemErrorCode;
import com.fm.foodmanagementsystem.core.services.interfaces.IFileService;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService implements IFileService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    // Hàm Upload Ảnh
    @Override
    public String uploadFile(MultipartFile file) {
        try {
            // Tự động tạo Bucket nếu chưa có
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            // Đổi tên file để không bị trùng (Ví dụ: 123e4567-e89b..._pizza.jpg)
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) {
                originalName = "unnamed";
            }
            String fileName = UUID.randomUUID().toString() + "_" + originalName.replaceAll("\\s+", "_");

            String contentType = file.getContentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), 10485760)
                            .contentType(contentType)
                            .build()
            );

            log.info("Uploaded file: {}", fileName);
            return fileName; // Chỉ trả về tên file để lưu vào Database
        } catch (Exception e) {
            log.error("MinIO Upload Error: ", e);
            throw new SystemException(SystemErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // Hàm Lấy Ảnh (Trả về byte array để hiển thị)
    @Override
    public byte[] getFile(String fileName) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
        )) {
            return stream.readAllBytes();
        } catch (Exception e) {
            log.error("Không tìm thấy file MinIO: {}", fileName, e);
            throw new SystemException(SystemErrorCode.DATA_NOT_FOUND);
        }
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            // Kiểm tra xem tên file có null hoặc rỗng không
            if (fileName == null || fileName.trim().isEmpty()) {
                return;
            }

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("Đã xóa file cũ trên MinIO: {}", fileName);
        } catch (Exception e) {
            // Dùng log.error thay vì throw Exception để nếu lỗi xóa ảnh (ví dụ ảnh không tồn tại)
            // thì app không bị chết (crash) hay rollback luồng chính.
            log.error("Không thể xóa file: {} - Lỗi: {}", fileName, e.getMessage());
        }
    }
}