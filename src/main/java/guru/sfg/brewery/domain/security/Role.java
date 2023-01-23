package guru.sfg.brewery.domain.security;

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
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users;

    @Singular // Lombokerweiterung, die es ermöglicht, über das Builder-Pattern eine Authority hinzuzufügen
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER)    // CascadeType.Merge = es muss eindeutig festgelegt werden
//    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)    // CascadeType.Merge = es muss eindeutig festgelegt werden
    @JoinTable(name = "role_authority",         // weil manytomany muss eine join table her.
        joinColumns = {@JoinColumn(name = "ROLE_ID", referencedColumnName = "ID")},                 // das Mapping für
        inverseJoinColumns = {@JoinColumn(name = "AUTHORITY_ID", referencedColumnName = "ID")}      // die Join Table
    )
    private Set<Authority> authorities;
}
