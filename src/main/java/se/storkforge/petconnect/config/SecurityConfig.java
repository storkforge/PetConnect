package se.storkforge.petconnect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import se.storkforge.petconnect.repository.UserRepository;
import se.storkforge.petconnect.util.CustomOAuth2UserService;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 🔐 Password encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 👤 Load UserDetails from our database
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
                .map(user -> {
                    List<GrantedAuthority> authorities = user.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority( role.getName()))
                            .collect(Collectors.toList());

                    return new org.springframework.security.core.userdetails.User(
                            user.getUsername(),
                            user.getPassword(),
                            authorities
                    );
                })
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // 🔒 Main security filter chain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomOAuth2UserService customOAuth2UserService) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register", "/register/**", "/css/**", "/js/**", "/images/**", "/graphql", "/error").permitAll()
                        .requestMatchers("/admin", "/admin/**").hasRole("ADMIN") // <-- Din ändring
                        .requestMatchers("/api/posts/**").authenticated() // <-- Ändring från main
                        .requestMatchers(HttpMethod.POST, "/api/posts").hasAnyRole("USER", "PREMIUM") // <-- Ändring från main
                        .requestMatchers(HttpMethod.PUT, "/api/posts/**").hasAnyRole("USER", "PREMIUM") // <-- Ändring från main
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/**").hasAnyRole("USER", "PREMIUM") // <-- Ändring från main
                        .requestMatchers("/premium/**").hasRole("PREMIUM")
                        .requestMatchers("/user-profile/**", "/profile/**", "/settings/**").authenticated()
                        .anyRequest().authenticated() // Denna regel blir nu för alla *andra* autentiserade sidor
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key("uniqueAndSecret")
                        .tokenValiditySeconds(1209600) // 2 weeks
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }


    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) auth.getPrincipal()).getUsername();
        }
        return null;
    }
}