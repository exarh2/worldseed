package online.worldseed.service.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import online.worldseed.model.entity.SecurityUserEntity;
import online.worldseed.repository.SecurityUserRepository;

@Service
public class SecurityUserDetailsService implements UserDetailsService {

    private final SecurityUserRepository repository;

    public SecurityUserDetailsService(SecurityUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SecurityUserEntity entity = repository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new SecurityUserDetails(entity);
    }
}
