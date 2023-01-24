package guru.sfg.brewery.bootstrap;

import guru.sfg.brewery.domain.security.Authority;
import guru.sfg.brewery.domain.security.Role;
import guru.sfg.brewery.domain.security.User;
import guru.sfg.brewery.repositories.security.AuthorityRepository;
import guru.sfg.brewery.repositories.security.RoleRepository;
import guru.sfg.brewery.repositories.security.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserDataLoader implements CommandLineRunner {
//    Interface used to indicate that a bean should run when it is contained within a SpringApplication.
//    Multiple CommandLineRunner beans can be defined within the same application context and
//    can be ordered using the Ordered interface or @Order annotation.

    @Override
    public void run(String... args) throws Exception {      // run aus CommandLineRunner
        if (authorityRepository.count() == 0) {              // gibt es schon authorities? wenn nicht, dann gibt es auch keine User
            loadSecurityData();                             // loadSecurityData() setzt die Values, die dann in der Application genutzt werden
        }
    }

    private final AuthorityRepository authorityRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;


    private void loadSecurityData() {
//        create roles neu - mit Role und Autority(permission)
//    1.    create Authorities / Permissions mit CRUD-Operations
//        permission("beer.create") = permission("entity.permission")

//        beer authorities
        Authority createBeer = authorityRepository.save(Authority.builder().permission("beer.create").build());
        Authority readBeer = authorityRepository.save(Authority.builder().permission("beer.read").build());
        Authority updateBeer = authorityRepository.save(Authority.builder().permission("beer.update").build());
        Authority deleteBeer = authorityRepository.save(Authority.builder().permission("beer.delete").build());

//        brewery authorities
        Authority createBrewery = authorityRepository.save(Authority.builder().permission("brewery.create").build());
        Authority readBrewery = authorityRepository.save(Authority.builder().permission("brewery.read").build());
        Authority updateBrewery = authorityRepository.save(Authority.builder().permission("brewery.update").build());
        Authority deleteBrewery = authorityRepository.save(Authority.builder().permission("brewery.delete").build());

//        customer authorities
        Authority createCustomer = authorityRepository.save(Authority.builder().permission("customer.create").build());
        Authority readCustomer = authorityRepository.save(Authority.builder().permission("customer.read").build());
        Authority updateCustomer = authorityRepository.save(Authority.builder().permission("customer.update").build());
        Authority deleteCustomer = authorityRepository.save(Authority.builder().permission("customer.delete").build());

//        beerOrder authorities for admin role
        Authority createOrder = authorityRepository.save(Authority.builder().permission("order.create").build());
        Authority readOrder = authorityRepository.save(Authority.builder().permission("order.read").build());
        Authority updateOrder = authorityRepository.save(Authority.builder().permission("order.update").build());
        Authority deleteOrder = authorityRepository.save(Authority.builder().permission("order.delete").build());

//        beerOrder authorities for CUSTOMER role ===> check permission name (BEST PRACTICE)
        Authority createOrderCustomer = authorityRepository.save(Authority.builder().permission("customer.order.create").build());
        Authority readOrderCustomer = authorityRepository.save(Authority.builder().permission("customer.order.read").build());
        Authority updateOrderCustomer = authorityRepository.save(Authority.builder().permission("customer.order.update").build());
        Authority deleteOrderCustomer = authorityRepository.save(Authority.builder().permission("customer.order.delete").build());
//    2.    create roles

        Role adminRole = roleRepository.save(Role.builder().name("ADMIN").build());
        Role userRole = roleRepository.save(Role.builder().name("USER").build());
        Role customerRole = roleRepository.save(Role.builder().name("CUSTOMER").build());

//    3.    set specific Authorities to different Roles
//        ACHTUNG: Set.of() baut immutable Collections, aber hibernate/jpa benötigt mutable Collections  => Failed to load ApplicationContext
//          deswegen muss man die Sets so bauen: new HasSet()
//        userRole.setAuthorities(Set.of(readBeer)); SO NICHT, weil immutable

        userRole.setAuthorities(new HashSet<>(Set.of(readBeer)));

        customerRole.setAuthorities(new HashSet<>(Set.of(readBeer, readBrewery, readCustomer,
                createOrderCustomer, readOrderCustomer, updateOrderCustomer, deleteOrderCustomer
                )));

        adminRole.setAuthorities(new HashSet<>(Set.of(
                createBeer, readBeer, updateBeer, deleteBeer,
                createBrewery, readBrewery, updateBrewery, deleteBrewery,
                createCustomer, readCustomer, updateCustomer, deleteCustomer,
                createOrder, readOrder, updateOrder, deleteOrder
                )));


//    4.    save Roles
        roleRepository.saveAll(Arrays.asList(adminRole, userRole, customerRole));

//      create roles:  setup ohne roleRepository, sondern nur mit Authorities
//        Authority admin = authorityRepository.save(Authority.builder().role("ROLE_ADMIN").build());
//        Authority user = authorityRepository.save(Authority.builder().role("ROLE_USER").build());
//        Authority customer = authorityRepository.save(Authority.builder().role("ROLE_CUSTOMER").build());



        User user = userRepository.save(User.builder()
                .username("user")
                .password(passwordEncoder.encode("userpw")) // weil in H2-MEM-DB gespeichert wird -> password encoder
//                        .authority(userRole)                                       // dies ist möglich, weil der User seine Authorities mit @Singular (Lombok) annotiert ist
                .role(userRole) // nach Umbau mit Roles/Authority
                .build());

        userRepository.save(User.builder()
                .username("admin")
                .password(passwordEncoder.encode("adminpw")) // weil in H2-MEM-DB gespeichert wird -> password encoder
//                        .authority(adminRole)                                       // dies ist möglich, weil der User seine Authorities mit @Singular (Lombok) annotiert ist
                .role(adminRole) // nach Umbau mit Roles/Authority
                .build());

        userRepository.save(User.builder()
                .username("customer")
                .password(passwordEncoder.encode("customerpw")) // weil in H2-MEM-DB gespeichert wird -> password encoder
//                        .authority(customerRole)                                       // dies ist möglich, weil der User seine Authorities mit @Singular (Lombok) annotiert ist
                .role(customerRole) // nach Umbau mit Roles/Authority
                .build());

        userRepository.save(User.builder()
                .username("customer2")
                .password(passwordEncoder.encode("customer2pw")) // weil in H2-MEM-DB gespeichert wird -> password encoder
//                        .authority(customerRole)                                       // dies ist möglich, weil der User seine Authorities mit @Singular (Lombok) annotiert ist
                .role(customerRole) // nach Umbau mit Roles/Authority
                .build());


        log.debug("Users loaded: " + userRepository.count());

    }

}
