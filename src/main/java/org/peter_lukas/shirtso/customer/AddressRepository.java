package org.peter_lukas.shirtso.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {

    @Query("SELECT a FROM Address a WHERE a.userId = :userId ORDER BY a.isDefault DESC , a.createdAt DESC")
    List<Address> findByUserUserIdOrderByIsDefaultDescCreatedAtDesc(UUID userId);

    @Query("SELECT a FROM Address a WHERE a.addressId = :addressId AND a.userId = :userId")
    Optional<Address> findByAddressIdAndUserUserId(Integer addressId, UUID userId);

    @Query("SELECT a FROM Address a WHERE a.user.userId = :userId AND a.isDefault = true")
    Optional<Address> findDefaultAddress(UUID userId);
}
