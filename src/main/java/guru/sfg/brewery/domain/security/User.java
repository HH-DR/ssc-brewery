package guru.sfg.brewery.domain.security;

import guru.sfg.brewery.domain.Customer;
import guru.sfg.brewery.domain.security.Authority;
import lombok.*;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Set;
import java.util.stream.Collectors;

// @Data - macht Probleme bei ManyToMany - Relationsships, weil es in einen infinite Loop kommt udn dann crasht
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class User implements UserDetails, CredentialsContainer {             // im Prinzip gebaut wie User von Spring Security - aber nicht erweitert!
                                // Der User von SpringSecurity implementiert 2 Interfaces
                                // man kann also auch den eigenen User diese Interfaces implementieren lassen

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String username;
    private String password;
//    auskommentiert zum Umbau auf Nutzung von Roles statt Authority
//    @Singular // Lombokerweiterung, die es ermöglicht, über das Builder-Pattern eine Authority hinzuzufügen
//    @ManyToMany(cascade = CascadeType.MERGE)    // CascadeType.Merge = es muss eindeutig festgelegt werden
//    @JoinTable(name = "user_authority",         // weil manytomany muss eine join table her.
//        joinColumns = {@JoinColumn(name = "USER_ID", referencedColumnName = "ID")},                 // das Mapping für
//        inverseJoinColumns = {@JoinColumn(name = "AUTHORITY_ID", referencedColumnName = "ID")}      // die Join Table
//    )
//    private Set<Authority> authorities;         // Achtung das ist nicht GrantedAuthority, sondern die selbst geschriebene


//    ==== neu zum Umbau auf Nutzung von Roles statt Authority
//    Die Authorities werden jetzt nicht mehr pro User gespeichert, sondern pro Rolle - die Rollen stehen als Entity zwiscjen User und Authority
//    deswegen werden die Authorities beim User auf Transient gesetzt und aus den Rollen des Users gestreamt
//    @Transient
//    private Set<Authority> authorities;
//
////  Getter für Authorities mit Mapping, weil die Authorities von Rolle des Users bezogen werden
//    public Set<Authority> getAuthorities() {
//        return this.roles.stream()
//                .map(Role::getAuthorities)
//                .flatMap(Set::stream)
//                .collect(Collectors.toSet());
//    }

//    NEU zum Umbau mit Implementierung von UserDetails, CredentialsContainer
//    Set<Authority> verschwindet, und wird durch Set<GrantedAuthority> ersetzt

    @ManyToOne(fetch = FetchType.EAGER)
    private Customer customer;

    @Transient
    public Set<GrantedAuthority> getAuthorities() {
        return this.roles.stream()
                .map(Role::getAuthorities)
                .flatMap(Set::stream)
                .map(authority -> {return new SimpleGrantedAuthority(authority.getPermission());}) // Mapping von eigener Authority zu GrantedAuthority
                .collect(Collectors.toSet());
    }




    @Singular // Lombokerweiterung, die es ermöglicht, über das Builder-Pattern eine Authority hinzuzufügen
//    @ManyToMany(cascade ={ CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER)    // CascadeType.CascadeType.PERSIST ist NUR für das erstmalige speichern eines Objekts möglich. -> Exception bei Öffnen, Ändern und danach wieder speichern
    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)    // CascadeType.Merge = es muss eindeutig festgelegt werden
    @JoinTable(name = "user_role",         // weil manytomany muss eine join table her.
        joinColumns = {@JoinColumn(name = "USER_ID", referencedColumnName = "ID")},                 // das Mapping für
        inverseJoinColumns = {@JoinColumn(name = "ROLE_ID", referencedColumnName = "ID")}      // die Join Table
    )
    private Set<Role> roles;

//     @Builder.Default Lombok erweiterung
    @Builder.Default    // damit bei der Nutzung vom Builder Pattern die Werte nicht null sind, sondern so, wie hier eingestellt
    private boolean accountNonExpired=true;
    @Builder.Default    // damit bei der Nutzung vom Builder Pattern die Werte nicht null sind, sondern so, wie hier eingestellt
    private boolean accountNonLocked=true;
    @Builder.Default    // damit bei der Nutzung vom Builder Pattern die Werte nicht null sind, sondern so, wie hier eingestellt
    private boolean credentialsNonExpired=true;
    @Builder.Default    // damit bei der Nutzung vom Builder Pattern die Werte nicht null sind, sondern so, wie hier eingestellt
    private boolean enabled=true;



    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
    @Override
    public void eraseCredentials() {
        this.password = null;   // das ist die Originalimplementierung von Spring
    }


}
