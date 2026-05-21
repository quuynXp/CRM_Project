package com.crm.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.crm.identity.entity.base.BaseEntity;
import com.fasterxml.jackson.databind.JsonNode;

@Entity
@Table(name = "company")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company extends BaseEntity {
    @Id
    @Column(name = "company_id")
    private UUID companyId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    private UUID userId;

    @Column(name = "tax_code", unique = true, length = 50)
    private String taxCode;

    private UUID storeId;

    @Column(name = "city", length = 255)
    private String city;

    @Column(name = "district", length = 255)
    private String district;

    @Column(name = "address")
    private String address;

    @Column(name = "description")
    private String description;
}
