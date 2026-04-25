package com.fm.foodmanagementsystem.modules.media_service;

import com.fm.foodmanagementsystem.core.services.interfaces.IFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {

    private final IFileService fileService;

    // API Test Upload Ảnh (Sau này sẽ gọi ngầm trong lúc tạo Food/Category)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(fileService.uploadFile(file));
    }

    // API Phục vụ ảnh (Mobile sẽ dùng thẻ <Image src="https://ngrok-domain/api/v1/media/hinh-pizza.jpg" />)
    // Lưu ý: api này phải được cấu hình permitAll() trong SecurityConfig nhé
    @GetMapping("/{fileName}")
    public ResponseEntity<byte[]> getImage(@PathVariable String fileName) {
        byte[] imageBytes = fileService.getFile(fileName);

        // Detect content type từ file extension
        MediaType contentType = MediaType.APPLICATION_OCTET_STREAM; // default
        String lowerName = fileName.toLowerCase();
        if (lowerName.endsWith(".png")) {
            contentType = MediaType.IMAGE_PNG;
        } else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) {
            contentType = MediaType.IMAGE_JPEG;
        } else if (lowerName.endsWith(".gif")) {
            contentType = MediaType.IMAGE_GIF;
        } else if (lowerName.endsWith(".webp")) {
            contentType = MediaType.parseMediaType("image/webp");
        } else if (lowerName.endsWith(".svg")) {
            contentType = MediaType.parseMediaType("image/svg+xml");
        }

        return ResponseEntity.ok()
                .contentType(contentType)
                .body(imageBytes);
    }
}