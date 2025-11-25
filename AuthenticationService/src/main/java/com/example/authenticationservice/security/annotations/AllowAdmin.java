package com.example.authenticationservice.security.annotations;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority(T(com.example.authenticationservice.entity.UserRole).ADMIN)")
@Target(ElementType.METHOD)
public @interface AllowAdmin {
}
