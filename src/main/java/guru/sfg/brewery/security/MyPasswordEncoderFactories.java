package guru.sfg.brewery.security;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.*;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;

public class MyPasswordEncoderFactories {

//    diese Methode stammt aus der originalen PasswordEncoderFactories und wird einfach etwas kleiner gemacht in der Auswahl
//    zusätzlich wird ein privater Consztructor eingebaut
    public static PasswordEncoder createDelegatingPasswordEncoder() {
        String encodingId = "bcrypt";
        Map<String, PasswordEncoder> encoders = new HashMap();
        encoders.put(encodingId, new BCryptPasswordEncoder());
        encoders.put("ldap", new LdapShaPasswordEncoder());
        encoders.put("noop", NoOpPasswordEncoder.getInstance());
        encoders.put("pbkdf2", new Pbkdf2PasswordEncoder());
        encoders.put("scrypt", new SCryptPasswordEncoder());
        encoders.put("SHA-256", new MessageDigestPasswordEncoder("SHA-256"));
        return new DelegatingPasswordEncoder(encodingId, encoders);
    }
//    do not instantiate class
    private MyPasswordEncoderFactories(){}

}
