package com.example.deviceservice.mapper;

import com.example.deviceservice.dto.UserDto;
import com.example.deviceservice.entity.UserEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel="spring",uses = {UserMapper.class,DeviceMapper.class})
public interface UserMapper {

    UserDto userEntityToUserDto(UserEntity user);
    List<UserDto> userEntityToUserDto(List<UserEntity> user);

    UserEntity userDtoToUserEntity(UserDto user);
    List<UserEntity>  userDtoToUserEntity(List<UserDto> user);

}
