package guru.sfg.brewery.security;

import guru.sfg.brewery.domain.security.Authority;
import guru.sfg.brewery.domain.security.User;
import guru.sfg.brewery.repositories.security.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.debug("Getting User info via JpaUserDetailsService");
// Umbau vom eigenen User zum User, der UserDetails von Spring implementiert => mapping von Authorities und User wird obsolet
//        User user = userRepository.findByUsername(username).orElseThrow(() ->       // dies ist die eigene Implementierung des Users
//        {return new UsernameNotFoundException("User name: " + username + " not found.");
//        });
//        return new org.springframework.security.core.userdetails.User(              // hier wird ein SpringFramework User per ctor gebaut
//                user.getUsername(),user.getPassword(), user.isEnabled(), user.isAccountNonExpired(),user.isCredentialsNonExpired(),
//                user.isAccountNonLocked(), convertToSpringAuthorities(user.getAuthorities())
//        );
//    }
            return userRepository.findByUsername(username).orElseThrow(() ->
        {return new UsernameNotFoundException("User name: " + username + " not found.");
        });
    }


// Umbau vom eigenen User zum User, der UserDetails von Spring implementiert => mapping von Authorities und User wird obsolet
//    private Collection<? extends GrantedAuthority> convertToSpringAuthorities(Set<Authority> authorities) {
//        if(authorities != null && authorities.size() > 0){
//            return authorities.stream()
//                    .map(Authority::getPermission)
//                    .map(SimpleGrantedAuthority::new)
//                    .collect(Collectors.toSet());
//        } else
//        {
//            return new HashSet<>();
//        }
//    }


}
