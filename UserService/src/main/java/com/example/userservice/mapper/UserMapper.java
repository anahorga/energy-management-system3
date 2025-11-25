package com.example.userservice.mapper;

import com.example.userservice.dto.UserDto;
import com.example.userservice.entity.UserEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel="spring",uses = {UserMapper.class})
public interface UserMapper {

    UserDto userEntityToUserDto(UserEntity user);
    List<UserDto> userEntityToUserDto(List<UserEntity> user);

    UserEntity  userDtoToUserEntity(UserDto user);
    List<UserEntity>  userDtoToUserEntity(List<UserDto> user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(UserDto dto, @MappingTarget UserEntity entity);
}
