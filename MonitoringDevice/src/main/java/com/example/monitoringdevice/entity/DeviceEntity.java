package com.example.monitoringdevice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sd-device")
public class DeviceEntity {

    @Id
    private Long id;

    @OneToMany(
            mappedBy = "device",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<MonitoringEntity> consumptionValues;
}
