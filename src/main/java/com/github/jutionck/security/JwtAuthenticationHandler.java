package com.github.jutionck.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jutionck.dto.response.ErrorResponse;
import com.github.jutionck.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Component
public class JwtAuthenticationHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) ->
                writeErrorResponse(response,
                        Objects.requireNonNull(ResponseUtil.buildErrorResponse(
                                        HttpStatus.UNAUTHORIZED,
                                        "Unauthorized - Invalid or missing token",
                                        List.of("You must provide a valid access token"))
                                .getBody()));
    }

    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) ->
                writeErrorResponse(response,
                        Objects.requireNonNull(ResponseUtil.buildErrorResponse(
                                        HttpStatus.FORBIDDEN,
                                        "Forbidden - Access denied",
                                        List.of("You do not have permission to access this resource"))
                                .getBody()));
    }

    private void writeErrorResponse(HttpServletResponse response, ErrorResponse errorResponse) throws IOException {
        response.setStatus(errorResponse.getStatus().getCode());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
