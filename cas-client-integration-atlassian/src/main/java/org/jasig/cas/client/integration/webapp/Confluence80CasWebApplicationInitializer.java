package org.jasig.cas.client.integration.webapp;

import java.util.EnumSet;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.jasig.cas.client.authentication.AuthenticationFilter;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

/**
 * This class is a workaround for the fact that Confluence 8.0+ does not support
 * {@link org.springframework.web.context.ContextLoaderListener} and
 * {@link org.springframework.web.filter.DelegatingFilterProxy} in its
 * {@code web.xml} file.  This class is a {@link WebApplicationInitializer} that
 * initializes the CAS SSO filters.
 * 
 * <p>See <a href="https://community.developer.atlassian.com/t/redirection-loop-to-login-page-using-custom-sso/69325">Redirection loop to login page using Custom SSO</a>
 * and <a href="https://support.atlassian.com/requests/CSP-316874/">CSP-316874</a>
 * for more information.</p>
 * 
 * For Confluence to use this class, it must be added to the $INSTALL_DIR/confluence/WEB-INF/classes/
 * directory.
 * 
 * As this class doesn't work when added to a JAR file it is ok for jira to use the same jar as confluence
 * 
 * Every filter defined by this initializer is by default added for <code>/*</code> path
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class Confluence80CasWebApplicationInitializer implements WebApplicationInitializer {

    private static final Logger logger = Logger.getLogger(Confluence80CasWebApplicationInitializer.class.getName());

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        logger.info("Initializing CAS SSO filters");
        initSingleSignOutFilter(servletContext);
        initAuthenticationFilter(servletContext);
        initValidationFilter(servletContext);
        logger.info("End of initializing CAS SSO filters");
    }

    private void initSingleSignOutFilter(ServletContext servletContext) {
        FilterRegistration casSingleSignOutRegistration = servletContext.getFilterRegistration("CasSingleSignOutFilter");
        if (casSingleSignOutRegistration == null) {
            casSingleSignOutRegistration = servletContext.addFilter("CasSingleSignOutFilter", SingleSignOutFilter.class);
        }
        logger.info("Registering CAS single sign out filter");
        casSingleSignOutRegistration.addMappingForUrlPatterns(
            EnumSet.allOf(DispatcherType.class),
            false,
            "/*");
        logInitParams(casSingleSignOutRegistration);
    }

    private void initAuthenticationFilter(ServletContext servletContext) {
        FilterRegistration casAuthenticationRegistration = servletContext.getFilterRegistration("CasAuthenticationFilter");
        if (casAuthenticationRegistration == null) {
            casAuthenticationRegistration = servletContext.addFilter("CasAuthenticationFilter", AuthenticationFilter.class);
        }
        logger.info("Registering CAS authentication filter");
        casAuthenticationRegistration.addMappingForUrlPatterns(
            EnumSet.allOf(DispatcherType.class),
            false,
            "/*");
        logInitParams(casAuthenticationRegistration);
    }

    private void initValidationFilter(ServletContext servletContext) {
        FilterRegistration casValidationRegistration = servletContext.getFilterRegistration("CasValidationFilter");
        if (casValidationRegistration == null) {
            casValidationRegistration = servletContext.addFilter("CasValidationFilter", Cas20ProxyReceivingTicketValidationFilter.class);
        }
        logger.info("Registering CAS validation filter");
        casValidationRegistration.addMappingForUrlPatterns(
            EnumSet.allOf(DispatcherType.class),
            false,
            "/*");
        logInitParams(casValidationRegistration);
    }

    private void logInitParams(FilterRegistration filterRegistration) {
        // if (!logger.isDebugEnabled()) {
        //     return;
        // }
        Map<String, String> initParams = filterRegistration.getInitParameters();
        StringBuilder sb = new StringBuilder(filterRegistration.getName() + " init params:");
        for (Map.Entry<String, String> entry : initParams.entrySet()) {
            sb.append("\n\t").append(entry.getKey()).append(" = ").append(entry.getValue());
        }
        logger.info(sb.toString());
    }

}