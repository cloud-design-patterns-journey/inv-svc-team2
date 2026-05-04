package com.ibm.inventory_management.config;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private static final String STOCK_READ = "STOCK_READ";
    private static final String STOCK_WRITE = "STOCK_WRITE";
    private static final String STOCK_DELETE = "STOCK_DELETE";
    private static final String STOCK_AUDIT = "STOCK_AUDIT";
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/", "/index.html", "/health", "/actuator/health", "/actuator/health/**",
                                "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**",
                                "/webjars/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/stock-items").hasAuthority(STOCK_READ)
                        .requestMatchers(HttpMethod.GET, "/stock-items/audit").hasAuthority(STOCK_AUDIT)
                        .requestMatchers(HttpMethod.POST, "/stock-item").hasAuthority(STOCK_WRITE)
                        .requestMatchers(HttpMethod.PUT, "/stock-item/**").hasAuthority(STOCK_WRITE)
                        .requestMatchers(HttpMethod.DELETE, "/stock-item/**").hasAuthority(STOCK_DELETE)
                        .anyRequest().authenticated())
                .httpBasic(withDefaults());
        return http.build();
    }
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername("supplier")
                        .password(passwordEncoder.encode("supplier-pass"))
                        .authorities(STOCK_READ)
                        .build(),
                User.withUsername("organizer")
                        .password(passwordEncoder.encode("organizer-pass"))
                        .authorities(STOCK_READ, STOCK_WRITE, STOCK_DELETE)
                        .build(),
                User.withUsername("partner")
                        .password(passwordEncoder.encode("partner-pass"))
                        .authorities(STOCK_READ)
                        .build(),
                User.withUsername("auditor")
                        .password(passwordEncoder.encode("auditor-pass"))
                        .authorities(STOCK_READ, STOCK_AUDIT)
                        .build(),
                User.withUsername("admin")
                        .password(passwordEncoder.encode("admin-pass"))
                        .authorities(STOCK_READ, STOCK_WRITE, STOCK_DELETE, STOCK_AUDIT)
                        .build());
    }
}
