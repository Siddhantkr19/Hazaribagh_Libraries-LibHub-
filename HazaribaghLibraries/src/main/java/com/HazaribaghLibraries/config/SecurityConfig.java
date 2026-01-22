package com.HazaribaghLibraries.config;

import com.HazaribaghLibraries.security.jwt.AuthTokenFilter;
import com.HazaribaghLibraries.security.oauth2.OAuth2LoginSuccessHandler;
import com.HazaribaghLibraries.security.services.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) {
        this.userDetailsService = userDetailsService;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
    }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Allow pre-flight checks
                        .requestMatchers("/api/auth/**", "/oauth2/**", "/login/oauth2/code/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/libraries/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reviews/library/**").permitAll()
                        .requestMatchers( "/api/health/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**" , "/docs", "/docs/** ").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/libraries").permitAll()    // <--- ADD THIS (Exact match)
                       // <--- Catches /libraries/1
                        // Admin Routes
                        .requestMatchers(HttpMethod.POST, "/api/public/upload").hasRole("Admin")
                        .requestMatchers(HttpMethod.POST, "/api/libraries/**").hasRole("Admin")
                        .requestMatchers(HttpMethod.PUT, "/api/libraries/**").hasRole("Admin")
                        .requestMatchers(HttpMethod.DELETE, "/api/libraries/**").hasRole("Admin")
                        .requestMatchers("/api/admin/**").hasRole("Admin")
                        .requestMatchers("/api/reviews/admin/**").hasRole("Admin")
                        .requestMatchers(HttpMethod.GET, "/api/help/**").hasRole("Admin")

                        // Student Routes
                        .requestMatchers(HttpMethod.POST, "/api/help/submit").hasRole("Student")
                        .requestMatchers("/api/reviews/check-eligibility").hasRole("Student")
                        .requestMatchers("/api/reviews/submit").hasRole("Student")

                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                .oauth2Login(oauth2 -> oauth2.successHandler(oAuth2LoginSuccessHandler));

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",                 // Localhost
               // "https://libhub-kappa.vercel.app",
                "https://libhub.live",            // <--- Add domain
                "https://www.libhub.live",// Production Frontend
                "https://libhub-backend.onrender.com",
                "http://localhost:8080"// Production Backend (Self)
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        configuration.setAllowCredentials(true); // CRITICAL for Cookies
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}