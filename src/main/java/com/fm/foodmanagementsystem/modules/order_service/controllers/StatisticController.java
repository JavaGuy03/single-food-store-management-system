package com.fm.foodmanagementsystem.modules.order_service.controllers;

import com.fm.foodmanagementsystem.core.response.ApiResponse;
import com.fm.foodmanagementsystem.modules.order_service.resources.responses.DashboardStatisticResponse;
import com.fm.foodmanagementsystem.modules.order_service.services.interfaces.IStatisticService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatisticController {

    IStatisticService statisticService;

    // API lấy dữ liệu 3 cục cho màn hình Mobile
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ApiResponse<DashboardStatisticResponse> getDashboard() {
        return ApiResponse.<DashboardStatisticResponse>builder()
                .result(statisticService.getDashboardOverview())
                .build();
    }

    // API Xuất file Excel
    @GetMapping("/export-revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // Nếu không truyền ngày, mặc định lấy ngày hôm nay
        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        byte[] excelContent = statisticService.exportDailyRevenueReport(targetDate);
        String fileName = "Doanh_Thu_" + targetDate.toString() + ".xlsx";

        return ResponseEntity.ok()
                // Header này ép trình duyệt/điện thoại phải hiện bảng tải file về
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelContent);
    }
}