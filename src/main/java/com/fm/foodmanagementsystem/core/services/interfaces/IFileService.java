package com.fm.foodmanagementsystem.core.services.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface IFileService {
    String uploadFile(MultipartFile file);
    byte[] getFile(String fileName);
    void deleteFile(String fileName);
}
