package com.github.nderwin.lee7.authentication;

import com.github.nderwin.lee7.LogAspect;
import com.github.nderwin.lee7.authentication.entity.AuthenticationToken;
import com.github.nderwin.lee7.authentication.entity.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.Interceptors;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.security.auth.message.callback.PasswordValidationCallback;
import javax.security.auth.message.module.ServerAuthModule;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static javax.security.auth.message.AuthStatus.SEND_FAILURE;
import static javax.security.auth.message.AuthStatus.SEND_SUCCESS;
import static javax.security.auth.message.AuthStatus.SUCCESS;

@ApplicationScoped
@Interceptors(LogAspect.class)
public class HttpServerAuthModule implements ServerAuthModule {

    private static final Logger LOG = Logger.getLogger(HttpServerAuthModule.class.getName());

    private CallbackHandler handler;

    EntityManager em;

    private final Class<?>[] supportedMessageTypes = new Class[]{
        HttpServletRequest.class,
        HttpServletResponse.class
    };

    public HttpServerAuthModule() {
        try {
            em = ((EntityManagerFactory) new InitialContext().lookup("java:comp/env/jpa/emf")).createEntityManager();
        } catch (NamingException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    @Override
    public Class[] getSupportedMessageTypes() {
        return supportedMessageTypes;
    }

    @Override
    public void initialize(final MessagePolicy requestPolicy, final MessagePolicy responsePolicy, final CallbackHandler handler, final Map options) throws AuthException {
        this.handler = handler;
    }

    @Override
    public void cleanSubject(final MessageInfo messageInfo, final Subject subject) throws AuthException {
    }

    @Override
    public AuthStatus secureResponse(final MessageInfo messageInfo, final Subject serviceSubject) throws AuthException {
        return SEND_SUCCESS;
    }

    @Override
    public AuthStatus validateRequest(final MessageInfo messageInfo, final Subject clientSubject, final Subject serviceSubject) throws AuthException {
        HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
        HttpServletResponse response = (HttpServletResponse) messageInfo.getResponseMessage();

        Callback[] callbacks;

        boolean mandatory = Boolean.parseBoolean(
                messageInfo.getMap().get("javax.security.auth.message.MessagePolicy.isMandatory").toString()
        );

        LOG.log(Level.CONFIG, "Authenticating {1} endpoint {0}", new Object[]{
            request.getRequestURI(),
            (mandatory ? "protected" : "anonymous")
        });

        if (mandatory) {
            String token = request.getHeader("X-Auth-Token");

            if (null == token) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return SEND_FAILURE;
            }

            AuthenticationToken auth = em.find(AuthenticationToken.class, token);
            if (null != auth) {
                try {
                    User user = em.createNamedQuery("findByUsername", User.class)
                            .setParameter("username", auth.getUsername())
                            .getSingleResult();
                    
                    // TODO - maybe use a query instead?
                    List<String> roles = new ArrayList<>(user.getRoles().size());
                    user.getRoles().stream().forEach((r) -> {
                        roles.add(r.getName());
                    });
                    
                    callbacks = new Callback[]{
                        new CallerPrincipalCallback(clientSubject, user.getUsername()),
                        new PasswordValidationCallback(clientSubject, user.getUsername(), user.getPassword().toCharArray()),
                        new GroupPrincipalCallback(clientSubject, roles.toArray(new String[]{}))
                    };

                    try {
                        handler.handle(callbacks);
                    } catch (IOException | UnsupportedCallbackException e) {
                        throw (AuthException) new AuthException(e.getMessage()).initCause(e);
                    }
                } catch (NoResultException | NonUniqueResultException ex) {
                    throw (AuthException) new AuthException(ex.getMessage()).initCause(ex);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return SEND_FAILURE;
            }
        }

        return SUCCESS;
    }
}
