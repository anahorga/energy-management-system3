package com.example.authenticationservice.security;


import com.example.authenticationservice.entity.UserRole;
import com.example.authenticationservice.security.exceptions.JwtAuthenticationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class JwtTokenService {

    private static final long EXPIRATION_TIME = Duration.ofDays(1).toMillis();
    private static final String HEADER_STRING = "app-auth";
    private static final String CLAIM_USER = "username";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_ID = "id";

    @Value("${application.secret}")
    private String secret;

    /**
     * Extracts authentication details from the JWT in the request header.
     *
     * @param request HTTP request containing the JWT
     * @return Spring Security Authentication if token is valid
     */
    public Authentication getAuthentication(final HttpServletRequest request) {
        String token = extractTokenFromRequest(request);

        if (token == null) {
            return null;
        }

        try {
            Claims claims = parseToken(token);
            String username = extractUsername(claims);
            String role = extractSingleRole(claims);

            GrantedAuthority authority = new SimpleGrantedAuthority(role);
            return new UsernamePasswordAuthenticationToken(username, null, List.of(authority));

        } catch (JwtException | IllegalArgumentException ex) {
            log.error("JWT parsing error: {}", ex.getMessage());
            throw new JwtAuthenticationException(ex);
        } catch (AuthenticationCredentialsNotFoundException ex) {
            log.error("Missing credentials in JWT: {}", ex.getMessage());
            throw new JwtAuthenticationException(ex);
        } catch (Exception ex) {
            log.error("Unexpected error while parsing JWT: {}", ex.getMessage());
            throw new JwtAuthenticationException(ex);
        }
    }

    /**
     * Creates a JWT token with user ID and roles.
     *
     * @param username the username of the user
     * @param role  the user role
     * @return a signed JWT token
     */
    public String createJwtToken(final String username, final UserRole role, final Long id) {
        return Jwts.builder()
                .claim(CLAIM_USER, username)
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_ID, id)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Returns the HMAC signing key.
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Extracts the JWT token from the HTTP request.
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING);
        return (token != null && !token.isBlank()) ? token : null;
    }

    /**
     * Parses the JWT token and returns its claims.
     */
    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extracts the username claim from the token.
     */
    private String extractUsername(Claims claims) {
        return Optional.ofNullable(claims.get(CLAIM_USER))
                .map(Object::toString)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("No username found in JWT"));
    }

    /**
     * Extracts a single role from the token (assumes only one).
     */
    private String extractSingleRole(Claims claims) {
        return Optional.ofNullable(claims.get(CLAIM_ROLE))
                .map(Object::toString)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("No roles found in JWT"));
    }

    private Long extractId(Claims claims) {
        String value = Optional.ofNullable(claims.get(CLAIM_ID))
                .map(Object::toString)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("No id found in JWT"));
        return Long.valueOf(value);
    }

    public Authentication getAuthenticationFromToken(String token) {
        if (token == null) {
            return null;
        }

        try {
            Claims claims = parseToken(token);
            String username = extractUsername(claims);
            String role = extractSingleRole(claims);

            GrantedAuthority authority = new SimpleGrantedAuthority(role);
            return new UsernamePasswordAuthenticationToken(username, null, List.of(authority));

        } catch (JwtException | IllegalArgumentException ex) {
            log.error("JWT parsing error: {}", ex.getMessage());
            throw new JwtAuthenticationException(ex);
        } catch (AuthenticationCredentialsNotFoundException ex) {
            log.error("Missing credentials in JWT: {}", ex.getMessage());
            throw new JwtAuthenticationException(ex);
        } catch (Exception ex) {
            log.error("Unexpected error while parsing JWT: {}", ex.getMessage());
            throw new JwtAuthenticationException(ex);
        }
    }

}
