package guru.sfg.brewery.security.permissions;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// === Das hier ist best practice==
// === auf diese Art werden die authorities nur noch an einem spezifischen Ort deklariert und nicht mehr an verschiedenen
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority('customer.delete')")
public @interface CustomerDeletePermission {
}
