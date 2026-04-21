package ek.osnb.demo.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(CsrfConfigurer::spa)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                // Use the built-in UsernamePasswordAuthenticationFilter but make it SPA-friendly
                .formLogin(form -> form
                        .loginProcessingUrl("/api/login")
                        .successHandler((_, res, _) -> res.setStatus(HttpServletResponse.SC_NO_CONTENT)) // 204
                        .failureHandler((_, res, _) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED)) // 401
                )
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler((_, res, _) -> res.setStatus(HttpServletResponse.SC_NO_CONTENT)) // 204
                )
                // Return 401 instead of redirecting to /login
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint((_, res, _) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                );
        return http.build();
    }

    @Bean
    PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsManager(PasswordEncoder passwordEncoder) {
        var u1 = User.builder()
                .username("user")
                .password(passwordEncoder.encode("pw"))
                .roles("USER")
                .build();
        var u2 = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("pw"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(u1, u2);
    }
}
