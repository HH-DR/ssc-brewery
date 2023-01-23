package guru.sfg.brewery.security.permissions;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)             // WICHTIG: Damit das zur Runteime vorgehalten wird
@PreAuthorize("hasAuthority('beer.read')")      // bezeichnet die Annotation, die mit'@BeerReadPermission' ersetzt werden soll
public @interface BeerReadPermission {
//    Damit man nicht immer @PreAuthorize("hasAuthority('beer.read')") als Annotation an die Methoden schreiben muss,
//    und eventuelle Veränderungen einfacher einpflegen kann,
//    kann man diese AnnotationsKlasse (@interface) nutzen.




}
