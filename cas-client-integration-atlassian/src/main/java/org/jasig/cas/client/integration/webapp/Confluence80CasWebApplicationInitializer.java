package org.jasig.cas.client.integration.webapp;

import java.util.EnumSet;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.jasig.cas.client.authentication.AuthenticationFilter;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

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