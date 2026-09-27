package com.hadiid.erp.config;

import com.hadiid.erp.security.AppUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Backend authorization is mandatory (Rule 32): every mutating controller
 * method is also annotated with @PreAuthorize, and this config additionally
 * locks down whole URL trees by permission so a missing method annotation is
 * not the only line of defense. CSRF is on by default for all state-changing
 * requests (Spring Security default with server-rendered forms + Thymeleaf's
 * automatic hidden CSRF field).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(AppUserDetailsService uds, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, DaoAuthenticationProvider authProvider) throws Exception {
        http
            .authenticationProvider(authProvider)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/login", "/error/**").permitAll()
                .requestMatchers("/settings/users/**").hasAuthority("USER_VIEW")
                .requestMatchers("/settings/audit/**").hasAuthority("AUDIT_VIEW")
                .requestMatchers("/settings/**").hasAuthority("SETTINGS_VIEW")
                .requestMatchers("/planning/**").hasAuthority("PLAN_VIEW")
                .requestMatchers("/schedule/**").hasAuthority("SCHEDULE_VIEW")
                .requestMatchers("/dashboard/**", "/reports/**").hasAuthority("REPORT_VIEW")
                .requestMatchers("/jobs/**").hasAuthority("JOB_VIEW")
                .requestMatchers("/customers/**").hasAuthority("JOB_VIEW")
                .requestMatchers("/labor/**").hasAuthority("PAYMENT_VIEW")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/error/403")
            )
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
                .maximumSessions(3)
            )
            // CSRF stays enabled (default) for all state-changing requests.
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
            );
        return http.build();
    }
}
