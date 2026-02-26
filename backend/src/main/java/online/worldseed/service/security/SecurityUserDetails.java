package online.worldseed.service.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import online.worldseed.model.entity.SecurityUserEntity;

public class SecurityUserDetails implements UserDetails {

    private final UUID id;
    private final String login;
    private final String password;
    private final List<GrantedAuthority> authorities;

    public SecurityUserDetails(SecurityUserEntity entity) {
        this.id = entity.getId();
        this.login = entity.getLogin();
        this.password = entity.getPassword();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + entity.getRole().name()));
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
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
