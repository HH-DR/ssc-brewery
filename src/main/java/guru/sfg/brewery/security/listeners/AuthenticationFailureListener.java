package guru.sfg.brewery.security.listeners;

import guru.sfg.brewery.domain.security.LoginFailure;
import guru.sfg.brewery.domain.security.User;
import guru.sfg.brewery.repositories.security.LoginFailureRepository;
import guru.sfg.brewery.repositories.security.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthenticationFailureListener {

    private final UserRepository userRepository;
    private final LoginFailureRepository loginFailureRepository;

    @EventListener
    public void listen(AuthenticationFailureBadCredentialsEvent event){
        log.debug("Login failure.");

        if(event.getSource() instanceof UsernamePasswordAuthenticationToken){
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) event.getSource();
            LoginFailure.LoginFailureBuilder builder = LoginFailure.builder();

            if(token.getDetails() instanceof WebAuthenticationDetails){
                WebAuthenticationDetails details = (WebAuthenticationDetails) token.getDetails();
                log.debug("Attempted login from:" + details.getRemoteAddress());
                builder.sourceIp(details.getRemoteAddress());
            }
            if (token.getPrincipal() instanceof String){
                log.debug("Attempted username: " + token.getPrincipal());
                builder.username((String) token.getPrincipal());
                userRepository.findByUsername((String) token.getPrincipal()).ifPresent(builder::user);
            }
            LoginFailure failure = loginFailureRepository.save(builder.build());
            log.debug("Failure Event Id: " + failure.getId());

            if(failure.getUser() != null){
                lockUserAccount(failure.getUser());
            }
        }
    }

    private void lockUserAccount(User user) {
        List<LoginFailure> failures = loginFailureRepository.findAllByUserAndCreatedDateIsAfter(
                user, Timestamp.valueOf(LocalDateTime.now().minusDays(1)));
        if(failures.size() > 3){
            log.debug("Locking User Account of: " + user.getUsername());
            user.setAccountNonLocked(false);
        }
    }
}
