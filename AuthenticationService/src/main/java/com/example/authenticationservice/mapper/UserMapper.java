package com.example.authenticationservice.mapper;

import com.example.authenticationservice.dto.UserDto;
import com.example.authenticationservice.entity.UserEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel="spring",uses = {UserMapper.class},unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserDto userEntityToUserDto(UserEntity user);
    List<UserDto> userEntityToUserDto(List<UserEntity> user);

    UserEntity  userDtoToUserEntity(UserDto user);
    List<UserEntity>  userDtoToUserEntity(List<UserDto> user);

}
