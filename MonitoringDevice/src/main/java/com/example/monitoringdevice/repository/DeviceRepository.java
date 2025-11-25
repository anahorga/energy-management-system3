package com.example.monitoringdevice.repository;

import com.example.monitoringdevice.entity.DeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceEntity,Long> {
}
