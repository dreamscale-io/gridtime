package com.dreamscale.ideaflow.core.security;

import com.dreamscale.ideaflow.api.ResourcePaths;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public AuthorizationFilter authorizationFilter() {
        return new AuthorizationFilter();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // TODO: investigate enabling csrf
        http.csrf().disable()
                .addFilterBefore(authorizationFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers(ResourcePaths.ACCOUNT_PATH + ResourcePaths.ACTIVATE_PATH).permitAll()
                .antMatchers(ResourcePaths.ORGANIZATION_PATH + ResourcePaths.MEMBER_PATH + ResourcePaths.INVITATION_PATH).permitAll()
                .antMatchers(ResourcePaths.PROJECT_PATH + "/**").hasRole("USER")
                .antMatchers(ResourcePaths.JOURNAL_PATH + "/**").hasRole("USER")
                .antMatchers(ResourcePaths.FLOW_PATH + "/**").hasRole("USER")
                .antMatchers(ResourcePaths.ADMIN_PATH + "/**").permitAll()
                .antMatchers("/v2/api-docs").permitAll()
                .antMatchers("/swagger-ui.html").permitAll()
                .antMatchers("/swagger-resources/**").permitAll()
                .antMatchers("/webjars/springfox-swagger-ui/**").permitAll()
                .antMatchers("/**").authenticated();
    }

}