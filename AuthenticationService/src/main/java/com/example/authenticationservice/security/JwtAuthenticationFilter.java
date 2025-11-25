package com.example.authenticationservice.security;

import com.example.authenticationservice.security.exceptions.JwtAuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest servletRequest, HttpServletResponse servletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication auth = jwtTokenService.getAuthentication(servletRequest);
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (final JwtAuthenticationException ex) {
            servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error parsing JWT Token");
        }
    }
}
