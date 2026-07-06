package com.commerceops.erp.global.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/refresh", "/api/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/banners/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/admin/dashboard/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/orders/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/inventory/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/shipments/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/returns/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/inquiries/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/warehouses/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/warehouse-stocks/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/stock-transfers/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/products/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/categories/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/banners/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/notifications/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/ops-analytics/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/hr/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/staff").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/staff/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/permission-groups").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/permission-groups/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/permissions").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/users/me/permissions").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/users/*/permissions").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/menu-permissions").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/users/*/permission-groups").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/admin/permission-groups").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/admin/permission-groups/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/admin/permission-groups/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/permission-groups/*/permissions").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/menu-permissions").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/users/*/permission-groups").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        // ADMIN 전용 (고객 관리, 상품 등록/수정/삭제, 쿠폰, 회계)
                        .requestMatchers("/api/admin/users/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers("/api/admin/orders/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers("/api/admin/dashboard/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers("/api/admin/inventory/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers("/api/admin/shipments/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers("/api/admin/returns/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers("/api/admin/inquiries/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers("/api/admin/reviews/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers("/api/admin/audit-logs/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers("/api/admin/warehouses/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers("/api/admin/warehouse-stocks/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers("/api/admin/stock-transfers/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/products/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/admin/products/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/admin/products/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/products/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/admin/categories/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/admin/categories/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.POST, "/api/admin/banners/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/admin/banners/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/banners/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers("/api/admin/coupons/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers("/api/admin/accounting/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers("/api/admin/hr/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers("/api/admin/staff").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers("/api/admin/staff/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .requestMatchers("/api/admin/media/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        // MANAGER 이상 접근 가능
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "MANAGER")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"success\":false,\"statusCode\":401,\"message\":\"인증이 필요합니다.\"}");
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"success\":false,\"statusCode\":403,\"message\":\"접근 권한이 없습니다.\"}");
                        })
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
