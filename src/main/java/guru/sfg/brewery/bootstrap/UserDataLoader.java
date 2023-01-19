package guru.sfg.brewery.bootstrap;

import guru.sfg.brewery.domain.security.Authority;
import guru.sfg.brewery.domain.security.User;
import guru.sfg.brewery.repositories.security.AuthorityRepository;
import guru.sfg.brewery.repositories.security.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserDataLoader implements CommandLineRunner {
//    Interface used to indicate that a bean should run when it is contained within a SpringApplication.
//    Multiple CommandLineRunner beans can be defined within the same application context and
//    can be ordered using the Ordered interface or @Order annotation.

    @Override
    public void run(String... args) throws Exception {      // run aus CommandLineRunner
        if(authorityRepository.count() == 0) {              // gibt es schon authorities? wenn nicht, dann gibt es auch keine User
            loadSecurityData();                             // loadSecurityData() setzt die Values, die dann in der Application genutzt werden
        }
    }

    private final AuthorityRepository authorityRepository;
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


    private void loadSecurityData() {
//        create roles
        Authority admin = authorityRepository.save(Authority.builder().role("ADMIN").build());
        Authority user = authorityRepository.save(Authority.builder().role("USER").build());
        Authority customer = authorityRepository.save(Authority.builder().role("CUSTOMER").build());

//        create users
        userRepository.save(User.builder()
                        .username("user")
                        .password(passwordEncoder.encode("userpw")) // weil in H2-MEM-DB gespeichert wird -> password encoder
                        .authority(user)                                       // dies ist möglich, weil der User seine Authorities mit @Singular (Lombok) annotiert ist
                        .build());

        userRepository.save(User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("adminpw")) // weil in H2-MEM-DB gespeichert wird -> password encoder
                        .authority(admin)                                       // dies ist möglich, weil der User seine Authorities mit @Singular (Lombok) annotiert ist
                        .build());

        userRepository.save(User.builder()
                        .username("customer")
                        .password(passwordEncoder.encode("customerpw")) // weil in H2-MEM-DB gespeichert wird -> password encoder
                        .authority(customer)                                       // dies ist möglich, weil der User seine Authorities mit @Singular (Lombok) annotiert ist
                        .build());
        log.debug("Users loaded: " + userRepository.count());
    }

}
