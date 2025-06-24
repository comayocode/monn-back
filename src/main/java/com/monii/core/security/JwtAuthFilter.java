package com.monii.core.security;

import java.io.IOException;

import com.monii.auth.service.JwtService;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import com.monii.user.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);
        String requestURI = request.getRequestURI();

        logger.debug("Procesando solicitud para: {}", requestURI);

        if (token != null) {
            try {
                if (jwtService.validateToken(token)) {
                    String email = jwtService.extractUsername(token);
                    logger.debug("Token válido para usuario: {} en: {}", email, requestURI);

                    // Verificar si ya hay autenticación en el contexto
                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        User user = (User) userDetailsService.loadUserByUsername(email);

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                user, null, user.getAuthorities());

                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        logger.debug("Autenticación establecida para usuario: {}", email);
                    } else {
                        logger.debug("Ya existe autenticación en el contexto");
                    }
                } else {
                    logger.warn("Token inválido para solicitud: {}", requestURI);
                }
            } catch (Exception e) {
                logger.error("Error al procesar token: {}", e.getMessage());
                // No establecer autenticación si hay error
            }
        } else {
            logger.debug("No se encontró token para solicitud: {}", requestURI);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
