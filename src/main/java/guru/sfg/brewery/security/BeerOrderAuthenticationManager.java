package guru.sfg.brewery.security;

import guru.sfg.brewery.domain.security.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class BeerOrderAuthenticationManager {


    public boolean customerIdMatches (Authentication authentication, UUID customerId){

        // Wichtig:Der Cast zum User ist nötig, weil getPrincipal
        // nur ein Object zurückgibt
        User principal = (User) authentication.getPrincipal();
        //hier kann auch komplexere Logik & DB-Aufruf rein
        return principal.getCustomer().getId().equals(customerId);
    }
}
