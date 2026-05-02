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

    /** Dashboard quản trị chi nhánh — chỉ ADMIN (không dùng cho app khách). */
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DashboardStatisticResponse> getAdminDashboard() {
        return ApiResponse.<DashboardStatisticResponse>builder()
                .result(statisticService.getDashboardOverview())
                .build();
    }

    /**
     * @deprecated Dùng {@link #getAdminDashboard()} — {@code GET /api/v1/statistics/admin/dashboard}.
     */
    @Deprecated
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DashboardStatisticResponse> getDashboardLegacy() {
        return getAdminDashboard();
    }

    @GetMapping("/admin/export-revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportAdminRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return buildExportRevenueResponse(date);
    }

    /**
     * @deprecated Dùng {@link #exportAdminRevenue(LocalDate)} — {@code GET /api/v1/statistics/admin/export-revenue}.
     */
    @Deprecated
    @GetMapping("/export-revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportRevenueLegacy(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return buildExportRevenueResponse(date);
    }

    private ResponseEntity<byte[]> buildExportRevenueResponse(LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        byte[] excelContent = statisticService.exportDailyRevenueReport(targetDate);
        String fileName = "Doanh_Thu_" + targetDate + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelContent);
    }
}