package guru.sfg.brewery.security.listeners;

import guru.sfg.brewery.domain.security.User;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Check;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthenticationSuccessListener {

    @EventListener  // ist eine Component. sobald ein Event vom Typ AuthenticationSuccessEvent auftaucht, wird dieser Listener aufgerufen
    public void listen(AuthenticationSuccessEvent authenticationSuccessEvent){
        log.debug("Login User Okay");

//    Check Type of event-invoking data then get the source and cast it
       if(authenticationSuccessEvent.getSource() instanceof UsernamePasswordAuthenticationToken){
           UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authenticationSuccessEvent.getSource();

           if (token.getPrincipal() instanceof User){
               User user = (User) token.getPrincipal();
               log.debug("Principal Name: " + user.getUsername());
           }
           if(token.getDetails() instanceof WebAuthenticationDetails){
               WebAuthenticationDetails details = (WebAuthenticationDetails) token.getDetails();
               log.debug("Logged in from IP: " + details.getRemoteAddress());
           }
       }
    }
}
