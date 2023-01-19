package guru.sfg.brewery.web.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.LdapShaPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordEncodingTests {

    static final String PASSWORD = "userpw";

    @Test
    void testBCrypt() {
        PasswordEncoder bcrypt = new BCryptPasswordEncoder(); // standardmässig nutzt Spring eine Stärke von 10 Zeichen bei BCrypt
        PasswordEncoder bcrypt16 = new BCryptPasswordEncoder(16); // man kann aber auch höhere Stärken einstellen z.B. 16
        // neben der Stärke kann man auch die Version der Encodierung einstellen
        // beides wird im zurückgegebenen String angezeigt: $2a$16 => 2a = version 16 ist die Stärke

        System.out.println(bcrypt.encode(PASSWORD));
        System.out.println(bcrypt.encode(PASSWORD));
    }

    @Test
    void testSha256() {
        PasswordEncoder sha256 = new StandardPasswordEncoder();
        System.out.println(sha256.encode(PASSWORD)); // weil ein Random SALT genutzt wird, werden verschiedene HashWerte produziert
        System.out.println(sha256.encode(PASSWORD)); // weil ein Random SALT genutzt wird, werden verschiedene HashWerte produziert

    }

    @Test
    void testLdap() {
        PasswordEncoder ldap = new LdapShaPasswordEncoder();
        System.out.println(ldap.encode(PASSWORD)); // weil ein Random SALT genutzt wird, werden verschiedene HashWerte produziert
        System.out.println(ldap.encode(PASSWORD)); // weil ein Random SALT genutzt wird, werden verschiedene HashWerte produziert
        String encodedPassword = ldap.encode(PASSWORD);
        System.out.println(encodedPassword);
        assertTrue(ldap.matches(PASSWORD, encodedPassword));
    }

    @Test
    void testNoop() {
        PasswordEncoder noop = NoOpPasswordEncoder.getInstance();

        System.out.println(noop.encode(PASSWORD));
    }

    @Test
    void hashingExample() {
        //plain password hash
        System.out.println(DigestUtils.md5DigestAsHex(PASSWORD.getBytes()));
        String salted = PASSWORD + "ThisIsMySALTVALUE";
        // salted password hash (but hash never changes)
        System.out.println(DigestUtils.md5DigestAsHex(salted.getBytes()));
    }
}
