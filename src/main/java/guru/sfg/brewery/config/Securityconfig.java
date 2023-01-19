package guru.sfg.brewery.config;

import guru.sfg.brewery.security.MyPasswordEncoderFactories;
import guru.sfg.brewery.security.RestHeaderAuthFilter;
import net.bytebuddy.asm.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.security.authorization.AuthenticatedReactiveAuthorizationManager.authenticated;

@Configuration
@EnableWebSecurity
public class Securityconfig extends WebSecurityConfigurerAdapter {

    RestHeaderAuthFilter restHeaderAuthFilter(AuthenticationManager authenticationManager){         // create Filter
        RestHeaderAuthFilter filter = new RestHeaderAuthFilter(new AntPathRequestMatcher("/api/**"));   // set Antmatcher for path the filter is used for
        filter.setAuthenticationManager(authenticationManager);                                     // set AuthenticationManager (this case 0 InmemoryAuthMan)
        return filter;
    }


//      Diese Methode aus dem WebSecurityConfigurerAdapter
//      wird von Spring Security dazu genutzt, den Standard-User anzulegen und die login Form
    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        super.configure(http);//        kein super() call

//        Adding Custom Filter
        http.addFilterBefore(restHeaderAuthFilter(authenticationManager()), // Filter vor Filter… einfügen
                UsernamePasswordAuthenticationFilter.class)                // und den aktuellen AuthenticationManager mitgeben
                .csrf().disable();                                          // csrf disable, sonst blockiert csrf den Filter


        http
                .authorizeRequests(authorize -> {
                    authorize
                            .antMatchers("/", "webjars/**", "/login", "/resources/**").permitAll() // antmatcher must be before anyRequests(), **-syntax, resources einbinden z.B. CSS
                            .antMatchers(HttpMethod.GET, "/api/v1/beer/**").permitAll()
                            .mvcMatchers(HttpMethod.GET, "/api/v1/beerUpc/{upc}").permitAll();
                })
                .authorizeRequests()
                .anyRequest().authenticated()   // any request must be authenticated
                .and()
                .formLogin()                    // create a login page
                .and()
                .httpBasic();                   // enable http basic authentication
    }

    // =========================================================================================================================
    // Um nicht die Autoconfiguration für User von SpringSecurity oder die UserConfiguration in application.properties zu nutzen,
    // kann man User auch über die zwei folgenden Varianten erstellen.

    // ===== VARIANTE 1 =====
    // dies überschreibt die default implementierung vom UserDetailsManager aus der application.properties
    // und legt eine HashMap mit den Werten (von (in diesem Fall) user und admin) an.
    // Folge: der default user aus der application.properties kann gelöscht werden, da er nicht mehr genutzt wird

//    @Override
//    @Bean                                            // muss Bean sein, damit es in den Spring Context kommt
//    protected UserDetailsService userDetailsService() {
////        return super.userDetailsService();        // kein super() call
//                                                    // create 2 Users: here instead of in application.properties
//        UserDetails admin = User.withDefaultPasswordEncoder()
//                .username("admin")
//                .password("adminpw")
//                .roles("ADMIN")
//                .build();
//
//        UserDetails user = User.withDefaultPasswordEncoder()
//                .username("user")
//                .password("userpw")
//                .roles("USER")
//                .build();
//
//        return new InMemoryUserDetailsManager(admin, user);
//    }

    // ===== VARIANTE 2 - Fluent API (Abfolge versch. Method-Calls =====

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        super.configure(auth);
        auth.inMemoryAuthentication()
                .withUser("admin")
//                .password("{noop}adminpw")                //{noop} ist der Name des PasswordEncoders und MUSS angegeben werden
                .password("adminpw")                        //{noop} ist der Name des PasswordEncoders und MUSS angegeben werden
                                                             //    Im nächsten Schritt kann ein PasswordEncoder eingesetzt werden, dann muss aber der Eintrag {noop} aus der password - Zeile entfernt werden
                .roles("ADMIN")
                .and()
                .withUser("user")
//                .password("{noop}userpw")                 // Im nächsten Schritt kann ein PasswordEncoder eingesetzt werden, dann muss aber der Eintrag {noop} aus der password - Zeile entfernt werden
//                .password("userpw")
//                Die folgenden Passwörter sind mit den darunter stehenden PasswordEncoder-Tests abgestimmt (da überschrieben wird, gilt immer das letzte, nicht-auskommentierte
                .password("{SSHA}ruALtduE1LIaIBE6ccmvsr+jcr60M2XjjZ/94w==")                                     // dieser ldap-Hash von userpw wird gespeichert und im Test BeerRestControllerIT.initCreationForm verglichen
                .password("a0d0e59da7bbf1023ed1c76867ee70def236ee506f811989c8f8c7e5a610362f770647e7fd7ba7ca")   // dieser sha256-Hash von userpw wird gespeichert und im Test BeerRestControllerIT.initCreationForm verglichen
                .password("$2a$10$ubEl.WXMVin7hOXjTfbXvuPTrSmJt7CqCOQhSZ.jB8Ykm2McIQij6")                       // dieser BCrypt-Hash von userpw wird gespeichert und im Test BeerRestControllerIT.initCreationForm verglichen
                .password("{bcrypt}$2a$10$ubEl.WXMVin7hOXjTfbXvuPTrSmJt7CqCOQhSZ.jB8Ykm2McIQij6")              // dieser BCrypt-Hash von userpw wird gespeichert und im Test BeerRestControllerIT.initCreationForm verglichen,
                                                                                                                // Besonderheit: Nutzung von PWEncoderFactory (Test 5)
                .roles("USER")

                .and()
                .withUser("customer")
                .password("{bcrypt}")
                .roles("CUSTOMER");
    }

//    Im nächsten Schritt kann ein PasswordEncoder eingesetzt werden, dann muss aber der Eintrag {noop} aus der password - Zeile entfernt werden
    @Bean
    PasswordEncoder passwordEncoder() {
//        Test 1 - NoOpPasswordEncoder
//        return NoOpPasswordEncoder.getInstance();
//        Test 2 - LdapShaPasswordEncoder
//        return new LdapShaPasswordEncoder(); // damit Test funktioneren muss jetzt ein Wert aus dem Test PasswordEncodingTests.testLdap wie der folgende
                                                // {SSHA}ruALtduE1LIaIBE6ccmvsr+jcr60M2XjjZ/94w==
                                                // in das PW bei der Usererstellung eingesetzt werden
//        Test 3 - StandardPasswordEncoder
//        return new StandardPasswordEncoder(); // für sha256 Password Encoder
//        Test 4 - BCryptPasswordEncoder
//        return new BCryptPasswordEncoder();
//        Test 5 - PasswordEncoderFactories
//          return PasswordEncoderFactories.createDelegatingPasswordEncoder(); // neu in Spring 5
        // diese PasswordEncoder - Factory kann mit all den anderen Encodern umgehen
        // - man gibt dann bei der UserConfiguration den gewählten Encoder in {}an, z.B.
        // .password("{bcrypt}$2a$10$ubEl.WXMVin7hOXjTfbXvuPTrSmJt7CqCOQhSZ.jB8Ykm2McIQij6")
        // default ist bcrypt
//        Test 6 -Eigene PasswordEncoderFactory siehe security.MyPasswordEncoderFactories
        return MyPasswordEncoderFactories.createDelegatingPasswordEncoder();





    }








}
