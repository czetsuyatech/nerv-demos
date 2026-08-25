package com.czetsuyatech.nerv.audit.persistence.repository;

import com.czetsuyatech.nerv.audit.persistence.entity.AddressEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, Long> {

  List<AddressEntity> findByUserId(Long userId);
}
