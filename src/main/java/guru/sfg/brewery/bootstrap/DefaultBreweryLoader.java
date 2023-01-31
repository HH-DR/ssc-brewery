/*
 *  Copyright 2020 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package guru.sfg.brewery.bootstrap;

import guru.sfg.brewery.domain.*;
import guru.sfg.brewery.domain.security.Authority;
import guru.sfg.brewery.domain.security.Role;
import guru.sfg.brewery.domain.security.User;
import guru.sfg.brewery.repositories.*;
import guru.sfg.brewery.repositories.security.AuthorityRepository;
import guru.sfg.brewery.repositories.security.RoleRepository;
import guru.sfg.brewery.repositories.security.UserRepository;
import guru.sfg.brewery.web.model.BeerStyleEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;


/**
 * Created by jt on 2019-01-26.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class DefaultBreweryLoader implements CommandLineRunner {

    public static final String TASTING_ROOM = "Tasting Room";
    public static final String ST_PETE_DISTRIBUTING = "St Pete Distributing";
    public static final String DUNEDIN_DISTRIBUTING = "Dunedin Distributing";
    public static final String KEY_WEST_DISTRIBUTING = "Key West Distributing";

    public static final String ST_PETE_USER = "stpete";
    public static final String DUNEDIN_USER = "dunedin";
    public static final String KEY_WEST_USER = "keywest";

    public static final String BEER_1_UPC = "0631234200036";
    public static final String BEER_2_UPC = "0631234300019";
    public static final String BEER_3_UPC = "0083783375213";

    private final BreweryRepository breweryRepository;
    private final BeerRepository beerRepository;
    private final BeerInventoryRepository beerInventoryRepository;
    private final BeerOrderRepository beerOrderRepository;
    private final CustomerRepository customerRepository;

//    aus UserDataLoader
    private final AuthorityRepository authorityRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        loadSecurityData(); // vormals in UserDataLoader - jetzt ist sichergestellt, dass die Security Data zuerst geladen wird
        loadBreweryData();
        loadTastingRoomData();
        loadCustomerData(); // for distributors
    }

    private void loadCustomerData() {
//        BEST PRACTICE: Optional für JPA-find Methoden (siehe roleRepository) und dann an dieser Stelle kein Optional,
//        sondern eine Rolle und ein statement orElseThrow()
        Role customerRole = roleRepository.findByName("CUSTOMER").orElseThrow();

//        create customers
        Customer stPeteCustomer = customerRepository.save(Customer.builder()
                        .customerName(ST_PETE_DISTRIBUTING)
                        .apiKey(UUID.randomUUID())
                        .build());

        Customer dunedinCustomer = customerRepository.save(Customer.builder()
                        .customerName(DUNEDIN_DISTRIBUTING)
                        .apiKey(UUID.randomUUID())
                        .build());

        Customer keyWestCustomer = customerRepository.save(Customer.builder()
                        .customerName(KEY_WEST_DISTRIBUTING)
                        .apiKey(UUID.randomUUID())
                        .build());

//        create users
        User stPeteUser = userRepository.save(User.builder()
                        .username("stpete")
                        .password(passwordEncoder.encode("password"))
                        .customer(stPeteCustomer)
                        .role(customerRole)
                        .build());

        User dunedinUser = userRepository.save(User.builder()
                        .username("dunedin")
                        .password(passwordEncoder.encode("password"))
                        .customer(dunedinCustomer)
                        .role(customerRole)
                        .build());

        User keyWestUser = userRepository.save(User.builder()
                        .username("keywest")
                        .password(passwordEncoder.encode("password"))
                        .customer(keyWestCustomer)
                        .role(customerRole)
                        .build());

        createOrder(stPeteCustomer);
        createOrder(dunedinCustomer);
        createOrder(keyWestCustomer);

        log.debug("Orders loaded: " + beerOrderRepository.count());
    }

//    private BeerOrder createOrder(Customer customer) {
//
//        Set<BeerOrderLine> beerOrderLines = Set.of(BeerOrderLine.builder()
//                .beer(beerRepository.findByUpc(BEER_1_UPC))
//                .orderQuantity(2)
//                .build());
//
//        log.debug("BeerOrderLines of DefaultBreweryLoader: " + (beerOrderLines.stream()));
////        beerOrderLines.stream().forEach(beerOrderLine -> {beerOrderLineRepository.save(beerOrderLine);});
//
//        BeerOrder savedBeerOrder = beerOrderRepository.save(BeerOrder.builder()
//                .customer(customer)
//                .orderStatus(OrderStatusEnum.NEW)
////                .beerOrderLines(beerOrderLines)
//                  .beerOrderLines(beerOrderLineRepository.save())
//                .build());
//        return savedBeerOrder;
//    }

    private BeerOrder createOrder(Customer customer) {
        return  beerOrderRepository.save(BeerOrder.builder()
                .customer(customer)
                .orderStatus(OrderStatusEnum.NEW)
                .beerOrderLines(Set.of(BeerOrderLine.builder()
                        .beer(beerRepository.findByUpc(BEER_1_UPC))
                        .orderQuantity(1)
                        .quantityAllocated(111)
                        .build()))
                .build());
    }

    private void loadTastingRoomData() {
        Customer tastingRoom = Customer.builder()
                .customerName(TASTING_ROOM)
                .apiKey(UUID.randomUUID())
                .build();

        customerRepository.save(tastingRoom);

        beerRepository.findAll().forEach(beer -> {
            beerOrderRepository.save(BeerOrder.builder()
                    .customer(tastingRoom)
                    .orderStatus(OrderStatusEnum.NEW)
                    .beerOrderLines(Set.of(BeerOrderLine.builder()
                            .beer(beer)
                            .orderQuantity(3)
                            .quantityAllocated(333)
                            .build()))
                    .build());
        });
    }

    private void loadBreweryData() {
        if (breweryRepository.count() == 0) {
            breweryRepository.save(Brewery
                    .builder()
                    .breweryName("Cage Brewing")
                    .build());

            Beer mangoBobs = Beer.builder()
                    .beerName("Mango Bobs")
                    .beerStyle(BeerStyleEnum.IPA)
                    .minOnHand(12)
                    .quantityToBrew(200)
                    .upc(BEER_1_UPC)
                    .build();

            beerRepository.save(mangoBobs);
            beerInventoryRepository.save(BeerInventory.builder()
                    .beer(mangoBobs)
                    .quantityOnHand(500)
                    .build());

            Beer galaxyCat = Beer.builder()
                    .beerName("Galaxy Cat")
                    .beerStyle(BeerStyleEnum.PALE_ALE)
                    .minOnHand(12)
                    .quantityToBrew(200)
                    .upc(BEER_2_UPC)
                    .build();

            beerRepository.save(galaxyCat);
            beerInventoryRepository.save(BeerInventory.builder()
                    .beer(galaxyCat)
                    .quantityOnHand(500)
                    .build());

            Beer pinball = Beer.builder()
                    .beerName("Pinball Porter")
                    .beerStyle(BeerStyleEnum.PORTER)
                    .minOnHand(12)
                    .quantityToBrew(200)
                    .upc(BEER_3_UPC)
                    .build();

            beerRepository.save(pinball);
            beerInventoryRepository.save(BeerInventory.builder()
                    .beer(pinball)
                    .quantityOnHand(500)
                    .build());

        }
    }

    private void loadSecurityData() {

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

//    3.    set specific Authorities to different Roles - ACHTUNG: Set.of() baut immutable Collections, aber hibernate/jpa benötigt mutable Collections  => Failed to load ApplicationContext deswegen muss man die Sets so bauen: new HasSet()
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

        User user = userRepository.save(User.builder()
                .username("user")
                .password(passwordEncoder.encode("userpw")) // weil in H2-MEM-DB gespeichert wird -> password encoder
                .role(userRole) // nach Umbau mit Roles/Authority
                .build());

        userRepository.save(User.builder()
                .username("admin")
                .password(passwordEncoder.encode("adminpw")) // weil in H2-MEM-DB gespeichert wird -> password encoder
                .role(adminRole) // nach Umbau mit Roles/Authority
                .build());

        userRepository.save(User.builder()
                .username("customer")
                .password(passwordEncoder.encode("customerpw")) // weil in H2-MEM-DB gespeichert wird -> password encoder
                .role(customerRole) // nach Umbau mit Roles/Authority
                .build());

        userRepository.save(User.builder()
                .username("customer2")
                .password(passwordEncoder.encode("customer2pw")) // weil in H2-MEM-DB gespeichert wird -> password encoder
                .role(customerRole) // nach Umbau mit Roles/Authority
                .build());

        log.debug("Users loaded: " + userRepository.count());

    }
}
