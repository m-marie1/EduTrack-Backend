// //package com.college.attendance.config;
// //
// //import org.springframework.context.annotation.Bean;
// //import org.springframework.context.annotation.Configuration;
// //import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// //import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// //import org.springframework.security.config.http.SessionCreationPolicy;
// //import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// //import org.springframework.security.crypto.password.PasswordEncoder;
// //import org.springframework.security.web.SecurityFilterChain;
// //
// //@Configuration
// //@EnableWebSecurity
// //public class SecurityConfig {
// //
// //    @Bean
// //    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
// //        // This is a basic configuration that will need to be updated
// //        // when integrating with your teammate's authentication system
// //        http
// //            .csrf(csrf -> csrf.disable())
// //            .authorizeHttpRequests(auth -> auth
// //                .requestMatchers("/h2-console/**").permitAll()
// //                .requestMatchers("/api/auth/**").permitAll()
// //                .anyRequest().authenticated()
// //            )
// //            .sessionManagement(session -> session
// //                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
// //            )
// //            .headers(headers -> headers.frameOptions().disable());
// //
// //        return http.build();
// //    }
// //
// //    @Bean
// //    public PasswordEncoder passwordEncoder() {
// //        return new BCryptPasswordEncoder();
// //    }
// //}







// package com.college.attendance.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.context.annotation.Profile;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.SecurityFilterChain;

// @Configuration
// @EnableWebSecurity
// public class SecurityConfig {

//     // This bean will be used only in dev profile
//     @Bean
//     @Profile("dev")
//     public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
//         // Disable security for development
//         http
//                 .csrf(csrf -> csrf.disable())
//                 .authorizeHttpRequests(auth -> auth
//                         .requestMatchers("/**").permitAll()
//                 )
//                 .sessionManagement(session -> session
//                         .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                 )
//                 .headers(headers -> headers.frameOptions().disable());

//         return http.build();
//     }

//     // This bean will be used in production
//     @Bean
//     @Profile("!dev")
//     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//         http
//                 .csrf(csrf -> csrf.disable())
//                 .authorizeHttpRequests(auth -> auth
//                         .requestMatchers("/h2-console/**").permitAll()
//                         .requestMatchers("/api/auth/**").permitAll()
//                         .anyRequest().authenticated()
//                 )
//                 .sessionManagement(session -> session
//                         .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                 )
//                 .headers(headers -> headers.frameOptions().disable());

//         return http.build();
//     }

//     @Bean
//     public PasswordEncoder passwordEncoder() {
//         return new BCryptPasswordEncoder();
//     }
// }




package com.college.attendance.config;

import com.college.attendance.security.JwtRequestFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final JwtRequestFilter jwtRequestFilter;
    
    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .headers(headers -> headers.frameOptions().disable());
            
        // Add JWT token filter
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}