package com.example.deviceservice.service;

import com.example.deviceservice.dto.DeviceDto;
import com.example.deviceservice.dto.UserDto;
import com.example.deviceservice.entity.DeviceEntity;
import com.example.deviceservice.entity.UserEntity;
import com.example.deviceservice.exceptions.DeviceNotFoundException;
import com.example.deviceservice.exceptions.InvalidDeviceException;
import com.example.deviceservice.exceptions.UserNotFoundException;
import com.example.deviceservice.mapper.DeviceMapper;
import com.example.deviceservice.mapper.UserMapper;
import com.example.deviceservice.rabbit.RabbitMQConfig;
import com.example.deviceservice.repository.DeviceRepository;
import com.example.deviceservice.repository.UserRepository;
import com.example.deviceservice.validator.DeviceValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {

        private  final DeviceMapper deviceMapper;
        private  final DeviceRepository deviceRepository;
        private  final UserRepository userRepository;
        private  final UserMapper userMapper;
        private  final DeviceValidator deviceValidator;
        private final RabbitTemplate rabbitTemplate;


        public List<DeviceDto> findAll()
        {
            return deviceMapper.deviceEntityToDeviceDto(deviceRepository.findAll());
        }

        public DeviceDto saveDevice(DeviceDto deviceDto)
        {
                DeviceEntity deviceEntity = deviceMapper.deviceDtoToDeviceEntity(deviceDto);

                String errs = deviceValidator.validate(deviceEntity);
                if (!errs.isEmpty()) {
                        throw new InvalidDeviceException(errs);
                }
                if (!userRepository.existsById(deviceEntity.getUser().getId())) {
                        throw new UserNotFoundException("User with id " + deviceEntity.getUser().getId() + " not found");
                }

                DeviceEntity savedDevice=deviceRepository.save(deviceEntity);

                DeviceDto event=DeviceDto.builder()
                        .id(savedDevice.getId())
                        .build();

                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "device.insert", event);

                return deviceMapper.deviceEntityToDeviceDto(savedDevice);
        }

        public void deleteUser(Long id) {
                if (!userRepository.existsById(id)) {
                        throw new UserNotFoundException("User with id " + id + " not found");
                }
                List<DeviceEntity> devicesForUser=deviceRepository.findAllByUser_Id(id);
                for(DeviceEntity device:devicesForUser)
                        deleteDevice(device.getId());
                userRepository.deleteById(id);
        }


        @Transactional
        public UserDto saveUser(UserDto userDto) {
                UserEntity userEntity = userMapper.userDtoToUserEntity(userDto);

                if (userEntity.getId() == null) {
                        throw new IllegalArgumentException("User id must be provided (no auto-generation).");
                }

                if (userRepository.existsById(userEntity.getId())) {
                        throw new DuplicateKeyException("User with id " + userEntity.getId() + " already exists");
                }

                UserEntity saved = userRepository.save(userEntity);
                return userMapper.userEntityToUserDto(saved);
        }


        @Transactional
        public DeviceDto updateDevice(DeviceDto dto, Long id) {
                DeviceEntity device = deviceRepository.findById(id)
                        .orElseThrow(() -> new DeviceNotFoundException("Device with id " + id + " not found"));

                deviceMapper.updateEntityFromDto(dto, device);

                if (dto.getUser() != null && dto.getUser().getId() != null) {
                        Long userId = dto.getUser().getId();
                        if (!userRepository.existsById(userId)) {
                                throw new UserNotFoundException("User with id " + userId + " not found");
                        }
                        device.setUser(userRepository.getReferenceById(userId)); // proxy managed (fără transient)
                }

                String errs = deviceValidator.validate(device);
                if (!errs.isEmpty()) {
                        throw new InvalidDeviceException(errs);
                }

                DeviceEntity saved = deviceRepository.save(device);
                return deviceMapper.deviceEntityToDeviceDto(saved);
        }

        public void deleteDevice(Long id) {
                if (!deviceRepository.existsById(id)) {
                        throw new DeviceNotFoundException("Device with id " + id + " not found");
                }
                DeviceDto event=DeviceDto.builder()
                        .id(id)
                        .build();

                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "device.delete", event);

                deviceRepository.deleteById(id);
        }


        public List<DeviceDto> findByUserId(Long id) {
                if (!userRepository.existsById(id)) {
                        throw new UserNotFoundException("User with id " + id + " not found");
                }

                return deviceMapper.deviceEntityToDeviceDto(deviceRepository.findAllByUser_Id(id));

        }
}
