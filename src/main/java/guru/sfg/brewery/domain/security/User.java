package guru.sfg.brewery.domain.security;

import guru.sfg.brewery.domain.security.Authority;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

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

    @Singular // Lombokerweiterung, die es ermöglicht, über das Builder-Pattern eine Authority hinzuzufügen
    @ManyToMany(cascade = CascadeType.MERGE)    // CascadeType.Merge = es muss eindeutig festgelegt werden
    @JoinTable(name = "user_authority",         // weil manytomany muss eine join table her.
        joinColumns = {@JoinColumn(name = "USER_ID", referencedColumnName = "ID")},                 // das Mapping für
        inverseJoinColumns = {@JoinColumn(name = "AUTHORITY_ID", referencedColumnName = "ID")}      // die Join Table
    )
    private Set<Authority> authorities;         // Achtung das ist nicht GrantedAuthority, sondern die selbst geschriebene

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
