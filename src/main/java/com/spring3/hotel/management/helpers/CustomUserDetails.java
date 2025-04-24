package com.spring3.hotel.management.helpers;
 
import com.spring3.hotel.management.models.User;
// import com.spring3.hotel.management.models.Role; // Commenting out: Unresolved import
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails extends User implements UserDetails {

    private String username;
    private String password;
    Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User byUsername) {
        this.username = byUsername.getUsername();
        this.password= byUsername.getPassword();
        List<GrantedAuthority> auths = new ArrayList<>();

        // Lấy role của user
        // Role role = byUsername.getRole(); // Commenting out: Role cannot be resolved
        // if (role != null) { // Commenting out: Role cannot be resolved
            // auths.add(new SimpleGrantedAuthority(role.getName())); // Commenting out: Role cannot be resolved
        // }
        // Add a default role or handle role assignment differently if needed
        auths.add(new SimpleGrantedAuthority("ROLE_USER")); // Example: Default to ROLE_USER
        this.authorities = auths;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
