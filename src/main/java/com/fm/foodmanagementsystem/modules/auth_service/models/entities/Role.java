package com.fm.foodmanagementsystem.modules.auth_service.models.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role {
    @Id
    @ToString.Include
    String name;
    String description;

    @ManyToMany
    Set<Permission> permissions;
}
