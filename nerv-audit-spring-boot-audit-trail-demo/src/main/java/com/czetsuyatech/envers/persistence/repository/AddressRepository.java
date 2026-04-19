package com.czetsuyatech.envers.persistence.repository;

import com.czetsuyatech.envers.persistence.entity.AddressEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, Long> {

  List<AddressEntity> findByUserId(Long userId);
}
