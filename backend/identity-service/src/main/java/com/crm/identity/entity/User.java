package com.crm.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.crm.auth.entity.base.BaseEntity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private String email;

    @Column(name = "phone", unique = true)
    private String phone;

    @Column(unique = true)
    private String cccd;

    @Column(name = "password")
    private String password;

    @Column(name = "fullname")
    private String fullname;

    @Column(name = "day_of_birth")
    private LocalDate dayOfBirth;

    @Column(name = "avatar_url")
    private String avatarUrl;
   
}