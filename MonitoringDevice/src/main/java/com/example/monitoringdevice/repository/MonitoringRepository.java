package com.example.monitoringdevice.repository;

import com.example.monitoringdevice.entity.MonitoringEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MonitoringRepository  extends JpaRepository<MonitoringEntity,Long> {
     List<MonitoringEntity> findAllByDevice_Id(Long deviceId);
     MonitoringEntity findByDeviceIdAndTimestamp(Long deviceId, LocalDateTime timestamp);
     List<MonitoringEntity> findByDeviceIdAndTimestampBetweenOrderByTimestampAsc(Long deviceId,LocalDateTime start, LocalDateTime end);

}
