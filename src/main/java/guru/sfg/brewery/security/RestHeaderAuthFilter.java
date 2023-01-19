package guru.sfg.brewery.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class RestHeaderAuthFilter extends AbstractAuthenticationProcessingFilter {

    public RestHeaderAuthFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher);
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        String username = getUsername(request);
        String password = getPassword(request);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);

        if (username == null){
            username = "";
        }
        if (password == null){
            password = "";
        }

        log.debug("Authenticating User: " + username);

        if (!StringUtils.isEmpty(username)){
             return this.getAuthenticationManager().authenticate(token);        // wenn String vorhanden, dann return AuthenticationManager
        } else {
            return null;                                                        // wenn kein String vorhanden, dann return null
        }
    }

    // see BeerRestControllerIT.testDeleteBeerById()
    String getUsername(HttpServletRequest request){
        return request.getHeader("Api-Key");
    }
    String getPassword(HttpServletRequest request){
        return request.getHeader("Api-Secret");
    }




    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)res;

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Request is to process authentication");
            }

            try{
            Authentication authResult = attemptAuthentication(request, response);
            if(authResult != null){
                    this.successfulAuthentication(request, response, chain, authResult);
            } else {
                chain.doFilter(request, response);
            }

            }catch (AuthenticationException e){
                unsuccessfulAuthentication(request, response, e);   // ändert die Exception zu unauthorized und löscht den SecurityContext
            }                                                       // siehe auch die hier überschriebene Methode
        }




    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Authentication success. Updating SecurityContextHolder to contain: " + authResult);
        }

        SecurityContextHolder.getContext().setAuthentication(authResult);   // set Context with authResult - das nur be erfolgreicher Authorisierung nicht null ist (doFilter()

    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {

        SecurityContextHolder.clearContext();                                       // 1. clear SecurityContext

        if (this.log.isDebugEnabled()) {
            this.log.debug("Authentication request failed: " + failed.toString(), failed);
            this.log.debug("Updated SecurityContextHolder to contain null Authentication");
        }

        // ersetzt die Fehlermeldung "BadCredentials" - aus Methode onAuthenticationFailure()
        //        SimpleUrlAuthenticationFailureHandler implements AuthenticationFailureHandler
        //        SimpleUrlAuthenticationFailureHandler. onAuthenticationFailure()
        response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());


//        this.rememberMeServices.loginFail(request, response);                     // nicht benötigt aus super()
//        this.failureHandler.onAuthenticationFailure(request, response, failed);   // nicht benötigt aus super()
    }



    }




