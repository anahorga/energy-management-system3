package com.example.authenticationservice.controller;

import com.example.authenticationservice.entity.UserRole;
import com.example.authenticationservice.security.JwtTokenService;
import com.example.authenticationservice.service.PolicyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class ForwardAuthController {

    private final JwtTokenService jwt;
    private final PolicyService policy;

    @RequestMapping(path = "/validate", method = { RequestMethod.GET, RequestMethod.POST,
            RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.OPTIONS })
    public void forwardAuth(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            res.setStatus(HttpStatus.OK.value());
            return;
        }

        String token = req.getHeader("app-auth");
        if (!StringUtils.hasText(token)) {
            res.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing token");
            return;
        }

        var auth = jwt.getAuthenticationFromToken(token);

        Set<UserRole> roles = auth.getAuthorities().stream()
                .map(a -> UserRole.valueOf(a.getAuthority().replace("ROLE_", "")))
                .collect(java.util.stream.Collectors.toSet());

        String method = headerOrDefault(req, "X-Forwarded-Method", req.getMethod());
        String uri    = headerOrDefault(req, "X-Forwarded-Uri",    req.getRequestURI());
        String path = uri.split("\\?")[0];
        boolean allowed = policy.isAllowed(method, path, roles);
        if (!allowed) {
            res.sendError(HttpStatus.FORBIDDEN.value(), "Forbidden by policy");
            return;
        }

        res.setStatus(HttpStatus.OK.value());
    }

    private static String headerOrDefault(HttpServletRequest req, String name, String fallback) {
        String v = req.getHeader(name);
        return StringUtils.hasText(v) ? v : fallback;
    }
}
