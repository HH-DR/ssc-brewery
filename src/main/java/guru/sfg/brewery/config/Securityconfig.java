package guru.sfg.brewery.config;

import guru.sfg.brewery.security.MyPasswordEncoderFactories;
//    auskommentiert, weil Filter security.RestHeaderAuthFilter auskommentiert wurde
//import guru.sfg.brewery.security.RestHeaderAuthFilter;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.asm.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
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
import org.springframework.security.data.repository.query.SecurityEvaluationContextExtension;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.springframework.security.authorization.AuthenticatedReactiveAuthorizationManager.authenticated;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
//@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class Securityconfig extends WebSecurityConfigurerAdapter {


    private final UserDetailsService userDetailsService;
    private final PersistentTokenRepository persistentTokenRepository;

//    Für die Nutzung von der Dependency Spring-Security-Data muss diese Bean erstellt werden
    @Bean
    public SecurityEvaluationContextExtension securityEvaluationContextExtension(){
        return new SecurityEvaluationContextExtension();
    }

//    auskommentiert, weil der Filter security.RestHeaderAuthFilter auskommentiert wurde
//    RestHeaderAuthFilter restHeaderAuthFilter(AuthenticationManager authenticationManager){               // create Filter
//        RestHeaderAuthFilter filter = new RestHeaderAuthFilter(new AntPathRequestMatcher("/api/**"));     // set Antmatcher for path the filter is used for
//        filter.setAuthenticationManager(authenticationManager);                                           // set AuthenticationManager (this case 0 InmemoryAuthMan)
//        return filter;
//    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .authorizeRequests(authorize -> {
                    authorize
                            .antMatchers("/h2-console/**").permitAll() //do not use in production!
                            .antMatchers("/", "/webjars/**", "/login","/resources/**").permitAll();
                } )
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .formLogin(loginConfigurer -> {
                    loginConfigurer
                            .loginProcessingUrl("/login")
                            .loginPage("/").permitAll()
                            .successForwardUrl("/")
                            .defaultSuccessUrl("/");
                })
                .logout(logoutConfigurer -> {
                    logoutConfigurer
                            .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                            .logoutSuccessUrl("/")
                            .permitAll();
                })
                .httpBasic()
                .and().csrf().ignoringAntMatchers("/h2-console/**", "/api/**");

//    Remember Me Token: hash based
        http
                .rememberMe()
                .key("thisIsMyKey")
                .userDetailsService(userDetailsService);

//    Remember Me Token: Persitant token - Problem mit Db - Table schema.sql wird zwar gebaut, aber nicht gefunden => datasource nicht richtig angeschlossen
//          http
//                  .rememberMe()
//                  .tokenRepository(persistentTokenRepository)
//                  .userDetailsService(userDetailsService);

//h2 console config
        http.headers().frameOptions().sameOrigin();
    }



///////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////
//      Die Methode protected void configure(HttpSecurity http) aus dem WebSecurityConfigurerAdapter
//      wird von Spring Security dazu genutzt, den Standard-User anzulegen und die login Form
//    die unten stehende Variante enthält viel kaputt gespielte Möglichkeiten.
//    Die oben stehende Variante ist ein Kopie von Spring Guru

//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
////        super.configure(http);//        kein super() call
//
////    auskommentiert, weil Filter security.RestHeaderAuthFilter auskommentiert wurde
//////        Adding Custom Filter
////        http.addFilterBefore(restHeaderAuthFilter(authenticationManager()), // Filter vor Filter… einfügen
////                UsernamePasswordAuthenticationFilter.class)                // und den aktuellen AuthenticationManager mitgeben
////                .csrf().disable();                                          // csrf disable, sonst blockiert csrf den Filter
//
////        http.csrf().disable();  // disables csrf-protection globally  -----das muss neu gemacht werden, nachdem der security.RestHeaderAuthFilter auskommentiert wurde
//        http    .csrf().ignoringAntMatchers("/h2-console/**", "/api/**"); // disables csrf-protection for specific endpoints
//
//        http
//                .authorizeRequests(authorize -> {
//                    authorize
//                            .antMatchers("/h2-console/**").permitAll()                              // do not use accessible h2 in production
//                            .antMatchers("/", "webjars/**", "/login", "/login.html","/resources/**").permitAll()  // antmatcher must be before anyRequests(), **-syntax, resources einbinden z.B. CSS
//
////===== Mit dem Refactoring, das die Authorities, den Roles zuordnet, sind diese Rollenbasierten Freigaben nicht mehr nötig.
////      Stattdessen wird an den Methoden mit @PreAuthorize("hasAuthority('entityname.authority')") der Zugang geregelt --@PreAuthorize("hasAuthority('beer.read')")
////===== Der Vorteil ist, dass die konfiogurationsdatei deutlich übersichtlicher wird.
//
////                            .antMatchers(HttpMethod.GET, "/api/v1/beer/**").hasAnyRole("ADMIN","CUSTOMER","USER")
////                            .antMatchers("/bbers/find", "/beers*").hasAnyRole("ADMIN","CUSTOMER","USER")
////                            .mvcMatchers(HttpMethod.GET, "/api/v1/beerUpc/{upc}").hasAnyRole("ADMIN","CUSTOMER","USER")
////  Die Authorisierung zur Nutzung der Delete-Methode wurde mit @PreAuthorize direkt an die Methode gelegt => äufgräumtere config-Datei
////                            .mvcMatchers(HttpMethod.DELETE, "/api/v1/beer/**").hasRole("ADMIN") // Achtung: nicht ROLE_ADMIN an dieser Stelle
////                            .mvcMatchers(HttpMethod.GET,"brewery/breweries").hasAnyRole("ADMIN", "CUSTOMER")
//                            ;
//                })
//                .authorizeRequests()
//                .anyRequest().authenticated()   // any request must be authenticated
//                .and()
//                .formLogin(loginConfigurer -> {
//                    loginConfigurer
////                            .loginProcessingUrl("/login")
////                            .loginProcessingUrl("/login.html")
//                            .loginProcessingUrl("/")
//                            .loginPage("/login")
//                            .successForwardUrl("/")
//                            .defaultSuccessUrl("/")
//                            .failureUrl("/?error") // dieser parameter kann in der html page so genutzt werden: <p th:if="${param.error}" class="alert alert-danger" >Wrong username or password</p>
//                            .permitAll();
//                })  .logout(logoutConfigurer ->
//                        logoutConfigurer
//                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
//                                .logoutSuccessUrl("/?logout") // dieser parameter kann in der html page so genutzt werden: <p th:if="${param.logout}" class="alert alert-success" >You have logged out.</p>
//                                .permitAll()
//                )
//                .httpBasic();                   // enable http basic authentication
//
////    Remember Me cookiedefinition
//        http
//                .rememberMe()
//                .key("thisIsMyKey")
//                .userDetailsService(userDetailsService);
//
//
//        // configuration to access H2 DB via console - spring security forbids frames by default
//        http.headers().frameOptions().sameOrigin();
//
//    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////



///////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////
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
//
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
////        super.configure(auth);
//        auth.inMemoryAuthentication()
//                .withUser("admin")
////                .password("{noop}adminpw")                //{noop} ist der Name des PasswordEncoders und MUSS angegeben werden
//                .password("adminpw")                        //{noop} ist der Name des PasswordEncoders und MUSS angegeben werden
//                                                             //    Im nächsten Schritt kann ein PasswordEncoder eingesetzt werden, dann muss aber der Eintrag {noop} aus der password - Zeile entfernt werden
//                .roles("ADMIN")
//                .and()
//                .withUser("user")
////                .password("{noop}userpw")                 // Im nächsten Schritt kann ein PasswordEncoder eingesetzt werden, dann muss aber der Eintrag {noop} aus der password - Zeile entfernt werden
////                .password("userpw")
////                Die folgenden Passwörter sind mit den darunter stehenden PasswordEncoder-Tests abgestimmt (da überschrieben wird, gilt immer das letzte, nicht-auskommentierte
//
//                .password("{SSHA}ruALtduE1LIaIBE6ccmvsr+jcr60M2XjjZ/94w==")                                     // dieser ldap-Hash von userpw wird gespeichert und im Test BeerRestControllerIT.initCreationForm verglichen
//                .password("a0d0e59da7bbf1023ed1c76867ee70def236ee506f811989c8f8c7e5a610362f770647e7fd7ba7ca")   // dieser sha256-Hash von userpw wird gespeichert und im Test BeerRestControllerIT.initCreationForm verglichen
//                .password("$2a$10$ubEl.WXMVin7hOXjTfbXvuPTrSmJt7CqCOQhSZ.jB8Ykm2McIQij6")                       // dieser BCrypt-Hash von userpw wird gespeichert und im Test BeerRestControllerIT.initCreationForm verglichen
//                .password("{bcrypt}$2a$10$ubEl.WXMVin7hOXjTfbXvuPTrSmJt7CqCOQhSZ.jB8Ykm2McIQij6")              // dieser BCrypt-Hash von userpw wird gespeichert und im Test BeerRestControllerIT.initCreationForm verglichen,
//                                                                                                                // Besonderheit: Nutzung von PWEncoderFactory (Test 5)
//                .roles("USER")
//
//                .and()
//                .withUser("customer")
//                .password("{bcrypt}")
//                .roles("CUSTOMER");
//    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
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




//===============================================================================================
//===============================================================================================
//  =================== HOW TO USE H2-DB Einträge zur Authentifizierung ===================
//    Neue Technik: nicht mehr die InMemoryAuthentication mit Variante 1 und 2 sondern die Authentication mit JpaUserDetails und
//    den in der H2-Db gespeicherten Users.
//    Sondern: Es wird die configure() komplett auskommentiert und Spring Security kümmert sich selbst um die Erstellung eines AuthenticationManagers.
//      D.h. Spring Boot macht eine Autoconfiguration.
//      UND WEIL es einen eigenen Passwordencoder und einen eigenen UserDetailsService gibt, nutzt Spring Boot diese beiden auch!

//    Dabei entsteht ein Fehler beim Aufruf der localhost:8080/login :
//    ===> failed to lazily initialize a collection of role: guru.sfg.brewery.domain.security.User.authorities, could not initialize proxy - no Session
//
//    Das passiert, weil im JpaUserDetailsService in der Methode loadUserByUsername() die Methode convertToSpringAuthorities() aufgerufen wird,
//    die versucht ein Object der Collection<? extends GrantedAuthority> zu erreichen.
//    Dieses Objekt wurde aber noch nicht konfiguriert und der Kontext, in dem der Aufruf stattfindet ist von JPA.
//    JPA macht aber alle Calls von sich aus transactional - also ist der Context beim Aufruf der convert-Methode geschlossen.
//    Eine lösungs-Möglichkeit ist: Den Aufrauf der aussenliegenden Methode loadUserByUsername() auch transactional zu machen,
//    dann kümmert sich nämlich Spring darum, dass eine entsprechende Bean "eagerly" bereitgestellt wird.
    }
///////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////////////////////







}
