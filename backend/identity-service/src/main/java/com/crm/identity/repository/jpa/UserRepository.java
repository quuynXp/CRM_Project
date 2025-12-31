package com.crm.auth.repository.jpa;

import com.crm.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUserIdAndIsDeletedFalse(UUID userId);
    
    Optional<User> findByEmailAndIsDeletedFalse(String email);
    
    Optional<User> findByPhoneAndIsDeletedFalse(String phone);
    
    Optional<User> findByCccdAndIsDeletedFalse(String cccd);

    boolean existsByEmailAndIsDeletedFalse(String email);
    boolean existsByPhoneAndIsDeletedFalse(String phone);
    boolean existsByCccdAndIsDeletedFalse(String cccd);

    @Query("SELECT u FROM User u WHERE (u.email = :identifier OR u.phone = :identifier) AND u.isDeleted = false")
    Optional<User> findByIdentifier(@Param("identifier") String identifier);

    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(u.fullname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "u.phone LIKE CONCAT('%', :keyword, '%') OR " +
           "u.cccd LIKE CONCAT('%', :keyword, '%')) " +
           "AND u.isDeleted = false")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);
}