package com.fm.foodmanagementsystem.modules.order_service.services.imps;

import com.fm.foodmanagementsystem.core.exception.SystemException;
import com.fm.foodmanagementsystem.core.exception.enums.SystemErrorCode;
import com.fm.foodmanagementsystem.modules.order_service.models.entities.Order;
import com.fm.foodmanagementsystem.modules.order_service.models.repositories.OrderRepository;
import com.fm.foodmanagementsystem.modules.order_service.resources.responses.DashboardStatisticResponse;
import com.fm.foodmanagementsystem.modules.order_service.services.interfaces.IStatisticService;
import com.fm.foodmanagementsystem.modules.product_service.models.repositories.FoodRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatisticService implements IStatisticService {

    OrderRepository orderRepository;
    FoodRepository foodRepository;

    // 1. API cho màn hình Dashboard Mobile
    @Override
    public DashboardStatisticResponse getDashboardOverview() {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);

        long todayOrders = orderRepository.countOrdersByDateRange(startOfDay, endOfDay);
        Double revenue = orderRepository.sumRevenueByDateRange(startOfDay, endOfDay);
        double todayRevenue = (revenue != null) ? revenue : 0.0;
        long totalFoods = foodRepository.countByIsAvailableTrue();

        return DashboardStatisticResponse.builder()
                .todayOrders(todayOrders)
                .todayRevenue(todayRevenue)
                .totalFoods(totalFoods)
                .build();
    }

    // 2. API Xuất báo cáo Excel
    @Override
    public byte[] exportDailyRevenueReport(LocalDate date) {
        LocalDateTime startOfDay = date.atTime(LocalTime.MIN);
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Order> completedOrders = orderRepository.findCompletedOrdersForReport(startOfDay, endOfDay);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Báo cáo doanh thu");

            // Tạo Header
            Row headerRow = sheet.createRow(0);
            String[] columns = {
                    "Mã Đơn Hàng",
                    "Thời Gian",
                    "Tiền Hàng",
                    "Phí Ship",
                    "Giảm Giá",
                    "Thành Tiền",
                    "Trạng Thái",
                    "Địa Chỉ Giao"
            };

            // Format Header in đậm
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Đổ dữ liệu
            int rowIdx = 1;
            double totalDayRevenue = 0;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

            for (Order order : completedOrders) {
                Row row = sheet.createRow(rowIdx++);
                double shipping = order.getShippingFee() != null ? order.getShippingFee() : 0.0;
                double discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : 0.0;
                double itemsAmount = order.getTotalAmount() - shipping + discount;

                row.createCell(0).setCellValue(order.getId());
                row.createCell(1).setCellValue(order.getOrderDate().format(formatter));
                row.createCell(2).setCellValue(itemsAmount);
                row.createCell(3).setCellValue(shipping);
                row.createCell(4).setCellValue(discount);
                row.createCell(5).setCellValue(order.getTotalAmount());
                row.createCell(6).setCellValue(order.getStatus().name());
                row.createCell(7).setCellValue(order.getDeliveryAddress());
                totalDayRevenue += order.getTotalAmount();
            }

            // Dòng tổng kết cuối cùng
            Row totalRow = sheet.createRow(rowIdx + 1);
            totalRow.createCell(1).setCellValue("TỔNG DOANH THU CẢ NGÀY (Thành Tiền):");
            totalRow.createCell(5).setCellValue(totalDayRevenue);

            // Auto-size các cột cho đẹp
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Lỗi khi xuất báo cáo Excel cho ngày {}: {}", date, e.getMessage(), e);
            throw new SystemException(SystemErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}