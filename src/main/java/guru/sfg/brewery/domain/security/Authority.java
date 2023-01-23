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
public class Authority {        // im Prinzip aufgebaut wie SimpleGrantedAuthority - aber nicht erweiternd!

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String permission;

        @ManyToMany(mappedBy = "authorities")   // mappedBy legt die Verantwortung fürs Mapping in die Zuordnung User.authorities
    private Set<Role> roles;
}
