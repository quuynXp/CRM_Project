package com.crm.auth.repository.jpa;

import com.crm.auth.entity.UserAuthAccount;
import com.crm.auth.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAuthAccountRepository extends JpaRepository<UserAuthAccount, UUID> {
    Optional<UserAuthAccount> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
    Optional<UserAuthAccount> findByUser_UserIdAndProvider(UUID userId, AuthProvider provider);
    List<UserAuthAccount> findByUser_UserIdAndVerifiedTrue(UUID userId);

    Optional<UserAuthAccount> findByUser_UserIdAndIsPrimaryTrue(UUID userId);
    Optional<UserAuthAccount> findFirstByUser_UserIdOrderByLinkedAtAsc(UUID userId);
    Optional<UserAuthAccount> findFirstByUser_UserId(UUID userId);
    Optional<UserAuthAccount> findByUser_UserIdAndProviderAndProviderUserId(UUID userId, AuthProvider provider, String providerUserId);
    int countByUser_UserId(UUID userId);
}
