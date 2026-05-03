package com.fm.foodmanagementsystem.modules.order_service.resources.responses;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder
@Schema(description = """
        Dữ liệu mã giảm giá. Trường expiresAt là “wall clock” trong DB (không kèm timezone).
        Việc coi mã đã hết hạn hay chưa do server so sánh với LocalDateTime.now() (múi giờ JVM / thường là giờ cửa hàng).
        """)
public record CouponResponse(
        String id,
        String code,
        String discountType,
        Double discountValue,
        Double minOrderValue,
        Double maxDiscount,
        @Schema(description = """
                Hạn sử dụng theo thời điểm lưu DB. Server đánh giá hết hạn bằng LocalDateTime.now() trên JVM
                (timezone máy chủ — BA nên quy ước coi là “giờ cửa hàng” khi server đặt đúng zone).

                Gợi ý hiển thị app: ghi rõ “Theo giờ cửa hàng” / “Theo giờ hệ thống”; hiển thị expiresAt hoặc đổi sang
                múi giờ người dùng kèm nhãn. Giảm tranh chấp khi danh sách vừa tải xong nhưng vài giây sau áp mã đã quá HSD,
                hoặc khi đồng hồ điện thoại lệch giờ so với server.
                """)
        LocalDateTime expiresAt,
        Integer usageLimit,
        Integer usedCount,
        Integer reservedCount,
        Boolean isActive
) {}