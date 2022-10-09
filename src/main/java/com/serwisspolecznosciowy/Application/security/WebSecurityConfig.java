package com.serwisspolecznosciowy.Application.security;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@SecurityScheme(scheme = "basic", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(new BCryptPasswordEncoder(12))
                .usersByUsernameQuery("select username, password, enabled from users where username =?")
                .authoritiesByUsernameQuery("select username, role from users where username =?");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.httpBasic()
                .and()
                .authorizeRequests()
                .antMatchers(
                        "/user/add/dto",
                        "/user/login",
                        "/user/logout",
                        "/v3/api-docs/**",
                        "/swagger-ui/index.html**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                ).permitAll()
                .antMatchers(
                        "/user/password/{userId}",
                        "/user/delete/{}",
                        "/user/picture/{userId}",
                        "/user/all/username/dto",
                        "/post/all/dto",
                        "/post/edit/dto/{postId}",
                        "/post/add/dto",
                        "/post/dto/{id}",
                        "/post/delete/**",
                        "/post/body/dto",
                        "/post/like/dto/{postId}",
                        "/post/dislike/dto/{postId}",
                        "/post/likes/dto/{postId}",
                        "/post/dislikes/dto/{postId}",
                        "/comment/add/{postId}",
                        "/comment/all/dto",
                        "/comment/dto/{id}",
                        "/comment/delete/{id}",
                        "/comment/body/dto",
                        "/comment/like/{commentId}",
                        "/comment/dislike/{commentId}",
                        "/comment/edit/dto/{commentId}",
                        "/comment/all/dto/{postId}"
                ).hasAnyRole("USER", "ADMIN")
                .antMatchers(
                        "/user/all",
                        "/user/{id}",
                        "/user/enable/{userId}",
                        "/user/post/{postId}",
                        "/post/{id}",
                        "/post/all",
                        "/comment/all",
                        "/comment/{id}",
                        "/comment/body"
                ).hasRole("ADMIN")
                .anyRequest().authenticated()
                .and()
                .formLogin().permitAll()
                .and()
                .logout().permitAll()
                .and()
                .csrf().disable();
    }

}
