package com.kcj.SubWeb.config;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Date;

@Getter
public class CustomUserDetails extends User {
    private final int id;
    public CustomUserDetails(int id, String username, String password,
                             Collection<? extends GrantedAuthority> authorities)
    {
        super(username,password,authorities);
        this.id = id;
    }
}
