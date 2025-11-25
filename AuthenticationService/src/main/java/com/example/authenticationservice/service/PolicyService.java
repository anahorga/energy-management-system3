package com.example.authenticationservice.service;

import com.example.authenticationservice.entity.Rule;
import com.example.authenticationservice.entity.UserRole;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Set;

@Component
public class PolicyService {

    private final AntPathMatcher ant = new AntPathMatcher();
    private final List<Rule> rules = List.of(
            // --- ADMIN-only: colecția /api/devices (findAll)
            new Rule("GET",    "/api/devices",          Set.of(UserRole.ADMIN)),
            new Rule("GET",    "/api/devices/",         Set.of(UserRole.ADMIN)),

            // --- ADMIN-only: create device
            new Rule("POST",   "/api/devices",          Set.of(UserRole.ADMIN)),
            new Rule("POST",   "/api/devices/",         Set.of(UserRole.ADMIN)),

            // --- ADMIN-only: user management subpaths (/user, /user/{id})
            new Rule("POST",   "/api/devices/user",     Set.of(UserRole.ADMIN)),
            new Rule("POST",   "/api/devices/user/",    Set.of(UserRole.ADMIN)),
            new Rule("DELETE", "/api/devices/user/*",   Set.of(UserRole.ADMIN)),
            // (defensiv, chiar dacă nu ai GET pe /user)
            new Rule("GET",    "/api/devices/user",     Set.of(UserRole.ADMIN)),
            new Rule("GET",    "/api/devices/user/*",   Set.of(UserRole.ADMIN)),

            // --- ADMIN-only: update/delete device by id
            new Rule("PUT",    "/api/devices/*",        Set.of(UserRole.ADMIN)),
            new Rule("DELETE", "/api/devices/*",        Set.of(UserRole.ADMIN)),

            // --- USER + ADMIN: doar GET /api/devices/{id} (getDevicesByUserId)
            // (vinea DUPĂ regulile admin-only de mai sus ca să nu „scape” /user/*)
            new Rule("GET",    "/api/devices/*",        Set.of(UserRole.USER, UserRole.ADMIN)),

            // --- CATCH-ALL sub /api/devices/** -> ADMIN-only (orice endpoint viitor)
            new Rule("*",      "/api/devices/**",       Set.of(UserRole.ADMIN)),
            new Rule("GET",    "/api/users",    Set.of(UserRole.ADMIN)),
            new Rule("GET",    "/api/users/",   Set.of(UserRole.ADMIN)),

            new Rule("POST",   "/api/users",    Set.of(UserRole.ADMIN)),
            new Rule("POST",   "/api/users/",   Set.of(UserRole.ADMIN)),

            new Rule("DELETE", "/api/users/*",  Set.of(UserRole.ADMIN)),
            new Rule("PUT",    "/api/users/*",  Set.of(UserRole.ADMIN)),


            new Rule("POST",   "/api/monitoring/device/",   Set.of(UserRole.ADMIN)),
            new Rule("POST",   "/api/monitoring/device",   Set.of(UserRole.ADMIN)),

            new Rule("DELETE", "/api/monitoring/device/*",  Set.of(UserRole.ADMIN)),

            new Rule("GET",    "/api/monitoring",    Set.of(UserRole.ADMIN,UserRole.USER)),
            new Rule("GET",    "/api/monitoring/",   Set.of(UserRole.ADMIN,UserRole.USER)),

// --- CATCH-ALL: orice alt endpoint sub /api/users/** e tot ADMIN-only
            new Rule("*",      "/api/users/**", Set.of(UserRole.ADMIN)),

            new Rule("*",    "/api/monitoring/**",  Set.of(UserRole.ADMIN))
    );

    public boolean isAllowed(String method, String uri, Set<UserRole> userRoles) {
        for (Rule r : rules) {
            boolean methodOk = r.getHttpMethod().equals("*") || r.getHttpMethod().equalsIgnoreCase(method);
            boolean pathOk   = ant.match(r.getPathPattern(), uri);
            if (methodOk && pathOk) {
                return userRoles.stream().anyMatch(r.getAllowed()::contains);
            }
        }
        return false;
    }
}
