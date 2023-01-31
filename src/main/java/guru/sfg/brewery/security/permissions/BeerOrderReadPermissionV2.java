package guru.sfg.brewery.security.permissions;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyAuthority('order.read', 'customer.order.read')")
//diese Variante soll ohne User Info aus der Pfadvariable genutzt werden
public @interface BeerOrderReadPermissionV2 {

}
