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
    @GetMapping(value = "/{fileName}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getImage(@PathVariable String fileName) {
        byte[] imageBytes = fileService.getFile(fileName);
        return ResponseEntity.ok().body(imageBytes);
    }
}