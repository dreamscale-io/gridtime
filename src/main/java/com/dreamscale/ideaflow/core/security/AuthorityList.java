package com.dreamscale.ideaflow.core.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;

class AuthorityList extends ArrayList<GrantedAuthority> {

    void addRole(StandardRole role) {
        add(new SimpleGrantedAuthority(role.getName()));
    }

}
