package tuchin_emelianov.blps_lab_1.security;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import tuchin_emelianov.blps_lab_1.service.UserService;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final UserService userService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .userDetailsService(userService)
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(HttpMethod.POST, "/order", "/order/payment", "/order/receiving", "/delivery/payment", "/delivery/receiving").hasAuthority("Клиент")
                        .requestMatchers(HttpMethod.PUT, "/pickup", "/order/payment").hasAuthority("Клиент")
                        .requestMatchers(HttpMethod.POST, "/processing", "/pickup").hasAuthority("Работник")
                        .requestMatchers(HttpMethod.PUT, "/processing").hasAuthority("Работник")
                        .requestMatchers(HttpMethod.POST, "/delivery").hasAuthority("Курьер")
                        .requestMatchers(HttpMethod.PUT, "/delivery").hasAuthority("Курьер")
                        .requestMatchers("/order/**", "/pickup/**").hasAnyAuthority("Работник", "Клиент")
                        .requestMatchers("/delivery/**").hasAuthority("Курьер")
                        .anyRequest().hasAuthority("Клиент")
                ).httpBasic();
        return http.build();
    }
}
