package com.kaijie.security;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 从请求头解析 JWT 并把认证信息放入 SecurityContext
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || header.isEmpty()) {
            log.warn("No Authorization header present for request: {} {}", request.getMethod(), request.getRequestURI());
        }

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                if (JwtUtils.isTokenValid(token)) {
                    Claims claims = JwtUtils.parseToken(token);
                    String username = claims.getSubject();
                    Integer roleType = null;
                    Object rt = claims.get("roleType");
                    if (rt != null) {
                        try {
                            roleType = Integer.valueOf(String.valueOf(rt));
                        } catch (Exception ignored) {}
                    }

                    List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                    if (roleType != null) {
                        switch (roleType) {
                            case 0:
                                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                                break;
                            case 1:
                                authorities.add(new SimpleGrantedAuthority("ROLE_DOCTOR"));
                                break;
                            case 2:
                                authorities.add(new SimpleGrantedAuthority("ROLE_PATIENT"));
                                break;
                            default:
                                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                        }
                    }

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception ex) {
                // token 无效或解析出错，记录日志并忽略让后续的安全机制处理（请求会最终被拒绝）
                log.warn("Failed to parse/validate token for request {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}

