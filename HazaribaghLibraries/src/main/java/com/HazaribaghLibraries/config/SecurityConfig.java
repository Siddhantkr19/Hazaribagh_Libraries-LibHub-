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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**", "/oauth2/**").permitAll()
                        .requestMatchers("/login/oauth2/code/**").permitAll()

                        // PUBLIC
                        .requestMatchers(HttpMethod.GET, "/api/libraries/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()


                        // ADMIN: Libraries (Using hasRole)
                        .requestMatchers(HttpMethod.POST, "/api/public/upload").hasRole("Admin")
                        .requestMatchers(HttpMethod.POST, "/api/libraries/**").hasRole("Admin")
                        .requestMatchers(HttpMethod.PUT, "/api/libraries/**").hasRole("Admin")
                        .requestMatchers(HttpMethod.DELETE, "/api/libraries/**").hasRole("Admin")

                        // Help and Support
                        .requestMatchers(HttpMethod.POST,"api/help/submit").hasRole("Student")
                        .requestMatchers(HttpMethod.GET,"api/help/**").hasRole("Admin")

                                // REVIEWS & FEEDBACK
                                 // Publicly visible reviews
                                .requestMatchers(HttpMethod.GET, "/api/reviews/library/**").permitAll()

                       // Student specific actions (ADD THE LEADING SLASH)
                                .requestMatchers("/api/reviews/check-eligibility").hasRole("Student")
                                .requestMatchers("/api/reviews/submit").hasRole("Student")

                             // Admin specific moderation (ADD THE LEADING SLASH)
                                .requestMatchers("/api/reviews/admin/**").hasRole("Admin")





                        // ADMIN: Dashboard & Booking (Using hasRole)
                        .requestMatchers("/api/admin/**").hasRole("Admin")

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
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}