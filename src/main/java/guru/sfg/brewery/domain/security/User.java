package guru.sfg.brewery.domain.security;

import guru.sfg.brewery.domain.security.Authority;
import lombok.*;

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
public class User {             // im Prinzip gebaut wie User von Spring Security - aber nicht erweitert!

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


//    neu zum Umbau auf Nutzung von Roles statt Authority
//    Die Authorities werden jetzt nicht mehr pro User gespeichert, sondern pro Rolle - die Rollen stehen als Entity zwiscjen User und Authority
//    deswegen werden die Authorities beim User auf Transient gesetzt und aus den Rollen des Users gestreamt
    @Transient
    private Set<Authority> authorities;

//  Getter für Authorities mit Mapping, weil die Authorities von Rolle des Users bezogen werden
    public Set<Authority> getAuthorities() {
        return this.roles.stream()
                .map(Role::getAuthorities)
                .flatMap(Set::stream)
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

    // @Builder.Default Lombok erweiterung
    @Builder.Default    // damit bei der Nutzung vom Builder Pattern die Werte nicht null sind, sondern so, wie hier eingestellt
    private boolean accountNonExpired=true;
    @Builder.Default    // damit bei der Nutzung vom Builder Pattern die Werte nicht null sind, sondern so, wie hier eingestellt
    private boolean accountNonLocked=true;
    @Builder.Default    // damit bei der Nutzung vom Builder Pattern die Werte nicht null sind, sondern so, wie hier eingestellt
    private boolean credentialsNonExpired=true;
    @Builder.Default    // damit bei der Nutzung vom Builder Pattern die Werte nicht null sind, sondern so, wie hier eingestellt
    private boolean enabled=true;
}
