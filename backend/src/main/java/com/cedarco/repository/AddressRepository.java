package com.cedarco.repository;

import com.cedarco.entity.Address;
import com.cedarco.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);
    void deleteByIdAndUser(Long id, User user);
}
