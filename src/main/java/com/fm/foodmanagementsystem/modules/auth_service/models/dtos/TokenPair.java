package com.fm.foodmanagementsystem.modules.auth_service.models.dtos;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokenPair {
    String accessToken;
    String refreshToken;
}
