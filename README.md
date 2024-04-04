# Disclaimer

Code base is a fork of [apereo java cas client ver. 3.6.4](https://github.com/apereo/java-cas-client/blob/cas-client-3.6.4)
Goal is to add support for Confuence 8+ using existing atlassian support by Apereo. For changes made see [Confluence80CasAuthenticator.java](./cas-client-integration-atlassian/src/main/java/org/jasig/cas/client/integration/atlassian/Confluence80CasAuthenticator.java) and [Confuence80CasWebApplicationInitializer.java](./cas-client-integration-atlassian/src/main/java/org/jasig/cas/client/integration/webapp/Confluence80CasWebApplicationInitializer.java)

Known issues:

- possible problems with some diacritics characters in some places like confluence space description due to CAS filter order.

# Java Apereo CAS Client [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jasig.cas.client/cas-client-core/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.jasig.cas.client/cas-client)

<a name="intro"></a>
## Intro
This is the official home of the Java Apereo CAS client. The client consists of a collection of Servlet filters that are suitable for most Java-based web applications. It also serves as an API platform to interact with the CAS server programmatically to make authentication requests, validate tickets and consume principal attributes.

All client artifacts are published to Maven central. Depending on functionality, applications will need include one or more of the listed dependencies in their configuration.

<a name="build"></a>
## Build [![Build Status](https://travis-ci.org/apereo/java-cas-client.png?branch=master)](https://travis-ci.org/apereo/java-cas-client)

```bash
git clone git@github.com:apereo/java-cas-client.git
cd java-cas-client
mvn clean package
```

Please note that to be deployed in Maven Central, we mark a number of JARs as provided (related to JBoss and Memcache
Clients).  In order to build the clients, you must enable the commented out repositories in the appropriate `pom.xml`
files in the modules (`cas-client-integration-jboss` and `cas-client-support-distributed-memcached`) or follow the instructions on how to install the file manually.

<a name="components"></a>
## Components

- Core functionality, which includes CAS authentication/validation filters.

```xml
<dependency>
    <groupId>org.jasig.cas.client</groupId>
    <artifactId>cas-client-core</artifactId>
    <version>${java.cas.client.version}</version>
</dependency>
```

- Atlassian integration (Deprecated) is provided by this dependency:

```xml
<dependency>
   <groupId>org.jasig.cas.client</groupId>
   <artifactId>cas-client-integration-atlassian</artifactId>
   <version>${java.cas.client.version}</version>
</dependency>
```

- Tomcat 9.0.x is provided by this dependency:

```xml
<dependency>
   <groupId>org.jasig.cas.client</groupId>
   <artifactId>cas-client-integration-tomcat-v90</artifactId>
   <version>${java.cas.client.version}</version>
</dependency>
```

<a name="configuration"></a>
## Configuration

### Strategies
The client provides multiple strategies for the deployer to provide client settings. The following strategies are supported:

- JNDI (`JNDI`)
- Properties File (`PROPERTY_FILE`). The configuration is provided via an external properties file. The path may be specified in the web context as such:

```xml
<context-param>
    <param-name>configFileLocation</param-name>
    <param-value>/etc/cas/file.properties</param-value>
</context-param>
```
If no location is specified, by default `/etc/java-cas-client.properties` will be used.

- System Properties (`SYSTEM_PROPERTIES`)
- Web Context (`WEB_XML`)
- Default (`DEFAULT`)

In order to instruct the client to pick a strategy, strategy name must be specified in the web application's context:

```xml
<context-param>
    <param-name>configurationStrategy</param-name>
    <param-value>DEFAULT</param-value>
</context-param>
```

If no `configurationStrategy` is defined, `DEFAULT` is used which is a combination of `WEB_XML` and `JNDI`. 

<a name="client-configuration-using-webxml"></a>
### Client Configuration Using `web.xml`

The client can be configured in `web.xml` via a series of `context-param`s and filter `init-param`s. Each filter for the client has a required (and optional) set of properties. The filters are designed to look for these properties in the following way:

- Check the filter's local `init-param`s for a parameter matching the required property name.
- Check the `context-param`s for a parameter matching the required property name.
- If two properties are found with the same name in the `init-param`s and the `context-param`s, the `init-param` takes precedence. 

**Note**: If you're using the `serverName` property, you should note well that the fragment-URI (the stuff after the #) is not sent to the server by all browsers, thus the CAS client can't capture it as part of the URL.

An example application that is protected by the client is [available here](https://github.com/UniconLabs/cas-sample-java-webapp).

<a name="orgjasigcasclientauthenticationauthenticationfilter"></a>
#### org.jasig.cas.client.authentication.AuthenticationFilter
The `AuthenticationFilter` is what detects whether a user needs to be authenticated or not. If a user needs to be authenticated, it will redirect the user to the CAS server.

```xml
<filter>
  <filter-name>CAS Authentication Filter</filter-name>
  <filter-class>org.jasig.cas.client.authentication.AuthenticationFilter</filter-class>
  <init-param>
    <param-name>casServerUrlPrefix</param-name>
    <param-value>https://battags.ad.ess.rutgers.edu:8443/cas</param-value>
  </init-param>
  <init-param>
    <param-name>serverName</param-name>
    <param-value>http://www.acme-client.com</param-value>
  </init-param>
</filter>
<filter-mapping>
    <filter-name>CAS Authentication Filter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

| Property | Description | Required
|----------|-------|-----------
| `casServerUrlPrefix` | The start of the CAS server URL, i.e. `https://localhost:8443/cas` | Yes (unless `casServerLoginUrl` is set)
| `casServerLoginUrl` | Defines the location of the CAS server login URL, i.e. `https://localhost:8443/cas/login`. This overrides `casServerUrlPrefix`, if set. | Yes (unless `casServerUrlPrefix` is set)
| `serverName` | The name of the server this application is hosted on. Service URL will be dynamically constructed using this, i.e. https://localhost:8443 (you must include the protocol, but port is optional if it's a standard port). | Yes
| `service` | The service URL to send to the CAS server, i.e. `https://localhost:8443/yourwebapp/index.html` | No
| `renew` | specifies whether `renew=true` should be sent to the CAS server. Valid values are either `true/false` (or no value at all). Note that `renew` cannot be specified as local `init-param` setting. | No
| `gateway ` | specifies whether `gateway=true` should be sent to the CAS server. Valid values are either `true/false` (or no value at all) | No
| `artifactParameterName ` | specifies the name of the request parameter on where to find the artifact (i.e. `ticket`). | No
| `serviceParameterName ` | specifies the name of the request parameter on where to find the service (i.e. `service`) | No
| `encodeServiceUrl ` | Whether the client should auto encode the service url. Defaults to `true` | No
| `ignorePattern` | Defines the url pattern to ignore, when intercepting authentication requests. | No
| `ignoreUrlPatternType` | Defines the type of the pattern specified. Defaults to `REGEX`. Other types are `CONTAINS`, `EXACT`, `FULL_REGEX`. Can also accept a fully-qualified class name that implements `UrlPatternMatcherStrategy`. | No
| `gatewayStorageClass` | The storage class used to record gateway requests | No
| `authenticationRedirectStrategyClass` | The class name of the component to decide how to handle authn redirects to CAS | No
| `method` | The method used by the CAS server to send the user back to the application. Defaults to `null` | No

##### Ignore Patterns

The following types are supported:

| Type | Description 
|----------|-------
| `REGEX` | Matches the URL the `ignorePattern` using `Matcher#find()`. It matches the next occurrence within the substring that matches the regex.
| `CONTAINS` | Uses the `String#contains()` operation to determine if the url contains the specified pattern. Behavior is case-sensitive.
| `EXACT` | Uses the `String#equals()` operation to determine if the url exactly equals the specified pattern. Behavior is case-sensitive.
| `FULL_REGEX` | Matches the URL the `ignorePattern` using `Matcher#matches()`. It matches the expression against the entire string as it implicitly add a `^` at the start and `$` at the end of the pattern, so it will not match substring or part of the string. `^` and `$` are meta characters that represents start of the string and end of the string respectively.

<a name="rgjasigcasclientvalidationcas10ticketvalidationfilter"></a>
#### org.jasig.cas.client.validation.Cas10TicketValidationFilter
Validates tickets using the CAS 1.0 Protocol.

```xml
<filter>
  <filter-name>CAS Validation Filter</filter-name>
  <filter-class>org.jasig.cas.client.validation.Cas10TicketValidationFilter</filter-class>
  <init-param>
    <param-name>casServerUrlPrefix</param-name>
    <param-value>https://somewhere.cas.edu:8443/cas</param-value>
  </init-param>
  <init-param>
    <param-name>serverName</param-name>
    <param-value>http://www.the-client.com</param-value>
  </init-param>    
</filter>
<filter-mapping>
    <filter-name>CAS Validation Filter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

| Property | Description | Required
|----------|-------|-----------
| `casServerUrlPrefix ` | The start of the CAS server URL, i.e. `https://localhost:8443/cas` | Yes
| `serverName` | The name of the server this application is hosted on. Service URL will be dynamically constructed using this, i.e. `https://localhost:8443` (you must include the protocol, but port is optional if it's a standard port). | Yes
| `renew` | Specifies whether `renew=true` should be sent to the CAS server. Valid values are either `true/false` (or no value at all). Note that `renew` cannot be specified as local `init-param` setting. | No
| `redirectAfterValidation ` | Whether to redirect to the same URL after ticket validation, but without the ticket in the parameter. Defaults to `true`. | No
| `useSession ` | Whether to store the Assertion in session or not. If sessions are not used, tickets will be required for each request. Defaults to `true`. | No
| `exceptionOnValidationFailure ` | Whether to throw an exception or not on ticket validation failure. Defaults to `true`. | No
| `sslConfigFile` | A reference to a properties file that includes SSL settings for client-side SSL config, used during back-channel calls. The configuration includes keys for `protocol` which defaults to `SSL`, `keyStoreType`, `keyStorePath`, `keyStorePass`, `keyManagerType` which defaults to `SunX509` and `certificatePassword`. | No.
| `encoding` | Specifies the encoding charset the client should use | No
| `hostnameVerifier` | Hostname verifier class name, used when making back-channel calls | No

<a name="orgjasigcasclientvalidationcas20proxyreceivingticketvalidationfilter"></a>
#### org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter
Validates the tickets using the CAS 2.0 protocol. If you provide either the `acceptAnyProxy` or the `allowedProxyChains` parameters, a `Cas20ProxyTicketValidator` will be constructed. Otherwise a general `Cas20ServiceTicketValidator` will be constructed that does not accept proxy tickets. 

**Note**: If you are using proxy validation, you should place the `filter-mapping` of the validation filter before the authentication filter.

```xml
<filter>
  <filter-name>CAS Validation Filter</filter-name>
  <filter-class>org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter</filter-class>
  <init-param>
    <param-name>casServerUrlPrefix</param-name>
    <param-value>https://battags.ad.ess.rutgers.edu:8443/cas</param-value>
  </init-param>
  <init-param>
    <param-name>serverName</param-name>
    <param-value>http://www.acme-client.com</param-value>
  </init-param>
</filter>
<filter-mapping>
    <filter-name>CAS Validation Filter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

| Property | Description | Required
|----------|-------|-----------
| `casServerUrlPrefix ` | The start of the CAS server URL, i.e. `https://localhost:8443/cas` | Yes
| `serverName` | The name of the server this application is hosted on. Service URL will be dynamically constructed using this, i.e. `https://localhost:8443` (you must include the protocol, but port is optional if it's a standard port). | Yes
| `renew` | Specifies whether `renew=true` should be sent to the CAS server. Valid values are either `true/false` (or no value at all). Note that `renew` cannot be specified as local `init-param` setting. | No
| `redirectAfterValidation ` | Whether to redirect to the same URL after ticket validation, but without the ticket in the parameter. Defaults to `true`. | No
| `useSession ` | Whether to store the Assertion in session or not. If sessions are not used, tickets will be required for each request. Defaults to `true`. | No
| `exceptionOnValidationFailure ` | whether to throw an exception or not on ticket validation failure. Defaults to `true` | No
| `proxyReceptorUrl ` | The URL to watch for `PGTIOU/PGT` responses from the CAS server. Should be defined from the root of the context. For example, if your application is deployed in `/cas-client-app` and you want the proxy receptor URL to be `/cas-client-app/my/receptor` you need to configure proxyReceptorUrl to be `/my/receptor`. | No
| `acceptAnyProxy ` | Specifies whether any proxy is OK. Defaults to `false`. | No
| `allowedProxyChains ` | Specifies the proxy chain. Each acceptable proxy chain should include a space-separated list of URLs (for exact match) or regular expressions of URLs (starting by the `^` character). Each acceptable proxy chain should appear on its own line. | No
| `proxyCallbackUrl` | The callback URL to provide the CAS server to accept Proxy Granting Tickets. | No
| `proxyGrantingTicketStorageClass ` | Specify an implementation of the ProxyGrantingTicketStorage class that has a no-arg constructor. | No
| `sslConfigFile` | A reference to a properties file that includes SSL settings for client-side SSL config, used during back-channel calls. The configuration includes keys for `protocol` which defaults to `SSL`, `keyStoreType`, `keyStorePath`, `keyStorePass`, `keyManagerType` which defaults to `SunX509` and `certificatePassword`. | No.
| `encoding` | Specifies the encoding charset the client should use | No
| `secretKey` | The secret key used by the `proxyGrantingTicketStorageClass` if it supports encryption. | No
| `cipherAlgorithm` | The algorithm used by the `proxyGrantingTicketStorageClass` if it supports encryption. Defaults to `DESede` | No
| `millisBetweenCleanUps` | Startup delay for the cleanup task to remove expired tickets from the storage. Defaults to `60000 msec` | No
| `ticketValidatorClass` | Ticket validator class to use/create | No
| `hostnameVerifier` | Hostname verifier class name, used when making back-channel calls | No
| `privateKeyPath` | The path to a private key to decrypt PGTs directly sent encrypted as an attribute | No
| `privateKeyAlgorithm` | The algorithm of the private key. Defaults to `RSA` | No

#### org.jasig.cas.client.validation.Cas30ProxyReceivingTicketValidationFilter
Validates the tickets using the CAS 3.0 protocol. If you provide either the `acceptAnyProxy` or the `allowedProxyChains` parameters, 
a `Cas30ProxyTicketValidator` will be constructed. Otherwise a general `Cas30ServiceTicketValidator` will be constructed that does not 
accept proxy tickets. Supports all configurations that are available for `Cas20ProxyReceivingTicketValidationFilter`.

#### org.jasig.cas.client.validation.json.Cas30JsonProxyReceivingTicketValidationFilter
Indentical to `Cas30ProxyReceivingTicketValidationFilter`, yet the filter is able to accept validation responses from CAS
that are formatted as JSON per guidelines laid out by the CAS protocol. 
See the [protocol documentation](https://apereo.github.io/cas/5.1.x/protocol/CAS-Protocol-Specification.html)
for more info.

<a name="orgjasigcasclientutilhttpservletrequestwrapperfilter"></a>
#### org.jasig.cas.client.util.HttpServletRequestWrapperFilter
Wraps an `HttpServletRequest` so that the `getRemoteUser` and `getPrincipal` return the CAS related entries.

```xml
<filter>
  <filter-name>CAS HttpServletRequest Wrapper Filter</filter-name>
  <filter-class>org.jasig.cas.client.util.HttpServletRequestWrapperFilter</filter-class>
</filter>
<filter-mapping>
  <filter-name>CAS HttpServletRequest Wrapper Filter</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping>
```

| Property | Description | Required
|----------|-------|-----------
| `roleAttribute` | Used to determine the principal role. | No
| `ignoreCase` | Whether role checking should ignore case. Defaults to `false` | No

<a name="orgjasigcasclientutilassertionthreadlocalfilter"></a>
#### org.jasig.cas.client.util.AssertionThreadLocalFilter
Places the `Assertion` in a `ThreadLocal` for portions of the application that need access to it. This is useful when the Web application that this filter "fronts" needs to get the Principal name, but it has no access to the `HttpServletRequest`, hence making `getRemoteUser()` call impossible.

```xml
<filter>
  <filter-name>CAS Assertion Thread Local Filter</filter-name>
  <filter-class>org.jasig.cas.client.util.AssertionThreadLocalFilter</filter-class>
</filter>
<filter-mapping>
  <filter-name>CAS Assertion Thread Local Filter</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping>
```

<a name="orgjasigcasclientutilerrorredirectfilter"></a>
#### org.jasig.cas.client.util.ErrorRedirectFilter
Filters that redirects to the supplied url based on an exception.  Exceptions and the urls are configured via init filter name/param values.

| Property | Description | Required
|----------|-------|-----------
| `defaultErrorRedirectPage` | Default url to redirect to, in case no error matches are found. | Yes
| `java.lang.Exception` | Fully qualified exception name. Its value must be redirection url | No


```xml
<filter>
  <filter-name>CAS Error Redirect Filter</filter-name>
  <filter-class>org.jasig.cas.client.util.ErrorRedirectFilter</filter-class>
  <init-param>
    <param-name>java.lang.Exception</param-name>
    <param-value>/error.jsp</param-value>
  </init-param>
  <init-param>
    <param-name>defaultErrorRedirectPage</param-name>
    <param-value>/defaulterror.jsp</param-value>
  </init-param>
</filter>
<filter-mapping>
  <filter-name>CAS Error Redirect Filter</filter-name>
  <url-pattern>/*</url-pattern>
</filter-mapping>
```

<a name="tomcat-678-integration"></a>
## Tomcat 6/7/8/9 Integration
The client supports container-based CAS authentication and authorization support for the Tomcat servlet container. 

Suppose a single Tomcat container hosts multiple Web applications with similar authentication and authorization needs. Prior to Tomcat container support, each application would require a similar configuration of CAS servlet filters and authorization configuration in the `web.xml` servlet descriptor. Using the new container-based authentication/authorization feature, a single CAS configuration can be applied to the container and leveraged by all Web applications hosted by the container.

CAS authentication support for Tomcat is based on the Tomcat-specific Realm component. The Realm component has a fairly broad surface area and RealmBase is provided as a convenient superclass for custom implementations; the CAS realm implementations derive from `RealmBase`. Unfortunately RealmBase and related components have proven to change over both major and minor number releases, which requires version-specific CAS components for integration. We have provided 3 packages with similar components with the hope of supporting all 6.x, 7.x and 8.x versions. **No support for 5.x is provided.**

<a name="component-overview"></a>
### Component Overview
In the following discussion of components, only the Tomcat 8.x components are mentioned. Tomcat 8.0.x components are housed inside
`org.jasig.cas.client.tomcat.v8` while Tomcat 8.5.x components are inside `org.jasig.cas.client.tomcat.v85`. Tomcat 9 packages are
available at `org.jasig.cas.client.tomcat.v90`. You should be able to use the same exact configuration between the two modules provided package names are adjusted for each release. 

The Tomcat 7.0.x and 6.0.x components have exactly the same name, but **are in the tomcat.v7 and tomcat.v6 packages**, e.g. 
`org.jasig.cas.client.tomcat.v7.Cas20CasAuthenticator` or `org.jasig.cas.client.tomcat.v6.Cas20CasAuthenticator`.

<a name="authenticators"></a>
#### Authenticators
Authenticators are responsible for performing CAS authentication using a particular protocol. All protocols supported by the Jasig Java CAS client are supported: CAS 1.0, CAS 2.0, and SAML 1.1. The following components provide protocol-specific support:

```
org.jasig.cas.client.tomcat.v8.Cas10CasAuthenticator
org.jasig.cas.client.tomcat.v8.Cas20CasAuthenticator
org.jasig.cas.client.tomcat.v8.Cas20ProxyCasAuthenticator
org.jasig.cas.client.tomcat.v8.Saml11Authenticator
```

<a name="realms"></a>
#### Realms
In terms of CAS configuration, Tomcat realms serve as containers for users and role definitions. The roles defined in a Tomcat realm may be referenced in the web.xml servlet descriptor to define authorization constraints on Web applications hosted by the container. Two sources of user/role data are supported:

```
org.jasig.cas.client.tomcat.v8.PropertiesCasRealm
org.jasig.cas.client.tomcat.v8.AssertionCasRealm
```

`PropertiesCasRealm` uses a Java properties file as a source of static user/role information. This component is conceptually similar to the `MemoryRealm` component that ships with Tomcat and defines user/role data via XML configuration. The PropertiesCasRealm is different in that it explicitly lacks support for passwords, which have no use with CAS.

`AssertionCasRealm` is designed to be used in conjunction with the SAML 1.1. protocol to take advantage of CAS attribute release to provide for dynamic user/role data driven by the CAS server. With this component the deployer may define a role attribute, e.g. memberOf, which could be backed by LDAP group membership information. In that case the user would be added to all roles defined in the SAML attribute assertion for values of the the `memberOf` attribute.

<a name="valves"></a>
#### Valves
A number of Tomcat valves are provided to handle functionality outside Realms and Authenticators.

##### Logout Valves
Logout valves provide a way of destroying the CAS authentication state bound to the container for a particular user/session; the destruction of authenticated state is synonymous with logout for the container and its hosted applications. (Note this does not destroy the CAS SSO session.) The implementations provide various strategies to map a URI onto the state-destroying logout function.

```
org.jasig.cas.client.tomcat.v8.StaticUriLogoutValve
org.jasig.cas.client.tomcat.v8.RegexUriLogoutValve
```

##### SingleSignOutValve
The `org.jasig.cas.client.tomcat.v8.SingleSignOutValve` allows the container to participate in CAS single sign-out. In particular this valve handles the SAML LogoutRequest message sent from the CAS server that is delivered when the CAS SSO session ends.

##### ProxyCallbackValve
The `org.jasig.cas.client.tomcat.v8.ProxyCallbackValve` provides a handler for watching request URIs for requests that contain a proxy callback request in support of the CAS 2.0 protocol proxy feature.

<a name="container-setup"></a>
### Container Setup
The version-specific CAS libraries must be placed on the container classpath, `$CATALINA_HOME/lib`.

<a name="context-configuration"></a>
### Context Configuration
The Realm, Authenticator, and Valve components are wired together inside a Tomcat Context configuration element. The location and scope of the Context determines the scope of the applied configuration. To apply a CAS configuration to every Web application hosted in the container, configure the default Context at `$CATALINA_HOME/conf/context.xml`. Note that individual Web applications/servlets can override the default context; see the Context Container reference for more information. 

Alternatively, CAS configuration can be applied to individual Web applications through a Context configuration element located in a `$CONTEXT_NAME.xml` file placed in `$CATALINA_HOME/conf/$ENGINE/$HOST`, where `$ENGINE` is typically Catalina and `$HOST` is `localhost`, `$CATALINA_HOME/conf/Catalina/localhost`. For example, to configure the Tomcat manager servlet, a `manager.xml` file contains Context configuration elements.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context privileged="true">
  <!--
    The following configuration uses the CAS 2.0 protocol and a static
    properties file to define allowed users to the Tomcat manager application.
    The content of manager-users.properties contains entries like the following:
 
      admin=manager-gui,manager-script,manager-jmx,manager-status
      operator=manager-status
      deployer=manager-script
 
    Where admin, operator, and deployer are valid logins for the CAS server.
    The path to the properties file is relative to $CATALINA_HOME.
 
    This example also configures the container for CAS single sign-out.
  -->
  <Realm
    className="org.jasig.cas.client.tomcat.v8.PropertiesCasRealm"
    propertiesFilePath="conf/manager-user-roles.properties"
    />
  <Valve
    className="org.jasig.cas.client.tomcat.v8.Cas20CasAuthenticator"
    encoding="UTF-8"
    casServerLoginUrl="https://server.example.com/cas/login"
    casServerUrlPrefix="https://server.example.com/cas/"
    serverName="client.example.com"
    />
 
  <!-- Single sign-out support -->
  <Valve
    className="org.jasig.cas.client.tomcat.v8.SingleSignOutValve"
    artifactParameterName="SAMLart"
    />
 
  <!--
    Uncomment one of these valves to provide a logout URI for the
    manager servlet.
  -->
  <!--
  <Valve
    className="org.jasig.cas.client.tomcat.v8.RegexUriLogoutValve"
    logoutUriRegex="/manager/logout.*"
    />
  <Valve
    className="org.jasig.cas.client.tomcat.v8.StaticUriLogoutValve"
    logoutUri="/manager/logout.html"
    />
  -->
</Context>
```

The following example shows how to configure a Context for dynamic role data provided by the CAS attribute release feature.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context privileged="true">
  <!--
    The following configuration uses the SAML 1.1 protocol and role data
    provided by the assertion to enable dynamic server-driven role data.
    The attribute used for role data is "memberOf".
  -->
  <Realm
    className="org.jasig.cas.client.tomcat.v8.AssertionCasRealm"
    roleAttributeName="memberOf"
    />
  <Valve
    className="org.jasig.cas.client.tomcat.v8.Saml11Authenticator"
    encoding="UTF-8"
    casServerLoginUrl="https://server.example.com/cas/login"
    casServerUrlPrefix="https://server.example.com/cas/"
    serverName="client.example.com"
    />
 
  <!-- Single sign-out support -->
  <Valve
    className="org.jasig.cas.client.tomcat.v8.SingleSignOutValve"
    artifactParameterName="SAMLart"
    />
</Context>
```

<a name="atlassian-integration"></a>
## Atlassian Integration
The clien includes Atlassian Confluence and JIRA support. Support is enabled by a custom CAS authenticator that extends the default authenticators.

<a name="configuration"></a>
### Configuration

<a name="jira_home-location"></a>
#### $JIRA_HOME Location
 
- WAR/EAR Installation: <extracted archive directory>/webapp
`/opt/atlassian/jira/atlassian-jira-enterprise-x.y.z/webapp`

- Standalone: <extracted archive directory>/atlassian-jira
`/opt/atlassian/jira/atlassian-jira-enterprise-x.y.z-standalone/atlassian-jira`

<a name="confluence_install-description"></a>
#### $CONFLUENCE_INSTALL Description

- <extracted archive directory>/confluence
`/opt/atlassian/confluence/confluence-x.y.z/confluence`

<a name="filters-definition-jira"></a>
#### Filters definition - Changes to web.xml for Jira
Add the CAS filters to the end of the filter list. See `web.xml` configuration of the client.

<a name="filters-definition-confluence"></a>
#### Filters definition - configuration for Confluence
For Confluence version 4.x through 7.19.1 add the CAS filters to the end of the filter list. See `web.xml` configuration of the client.

For Confluence version 8.x and newer extract org.jasig.cas.client.integration.webapp.Confluence80CasWebApplicationInitializer.class file from jar and add it to the Confluence WEB-INF/classes in the same package path.
Add the CAS filters configuration to web.xml. See `web.xml` configuration of the client.
Filter mapping should be defined by the Confluence80CasWebApplicationInitializer class for /* path.


<a name="modify-the-seraph-configxml"></a>
#### Modify the seraph-config.xml
To rely on the Single Sign Out functionality to sign off of Jira, comment out the normal logout URL and replace it with the CAS logout URL. Also, change the login links to point to the CAS login service.

```xml
<init-param>
    <!--
      The login URL to redirect to when the user tries to access a protected resource (rather than clicking on
      an explicit login link). Most of the time, this will be the same value as 'link.login.url'.
    - if the URL is absolute (contains '://'), then redirect that URL (for SSO applications)
    - else the context path will be prepended to this URL
 
    If '${originalurl}' is present in the URL, it will be replaced with the URL that the user requested.
    This gives SSO login pages the chance to redirect to the original page
    -->
    <param-name>login.url</param-name>
    <!--<param-value>/login.jsp?os_destination=${originalurl}</param-value>-->
    <param-value>http://cas.institution.edu/cas/login?service=${originalurl}</param-value>
</init-param>
<init-param>
    <!--
      the URL to redirect to when the user explicitly clicks on a login link (rather than being redirected after
      trying to access a protected resource). Most of the time, this will be the same value as 'login.url'.
    - same properties as login.url above
    -->
    <param-name>link.login.url</param-name>
    <!--<param-value>/login.jsp?os_destination=${originalurl}</param-value>-->
    <!--<param-value>/secure/Dashboard.jspa?os_destination=${originalurl}</param-value>-->
    <param-value>http://cas.institution.edu/cas/login?service=${originalurl}</param-value>
</init-param>
<init-param>
    <!-- URL for logging out.
    - If relative, Seraph just redirects to this URL, which is responsible for calling Authenticator.logout().
    - If absolute (eg. SSO applications), Seraph calls Authenticator.logout() and redirects to the URL
    -->
    <param-name>logout.url</param-name>
    <!--<param-value>/secure/Logout!default.jspa</param-value>-->
    <param-value>https://cas.institution.edu/cas/logout</param-value>
</init-param>
```

<a name="cas-authenticator"></a>
#### CAS Authenticator
Comment out the `DefaultAuthenticator` like so in `[$JIRA_HOME|$CONFLUENCE_INSTALL]/WEB-INF/classes/seraph-config.xml`:

```xml
<!-- CROWD:START - The authenticator below here will need to be commented out for Crowd SSO integration -->
<!--
<authenticator class="com.atlassian.seraph.auth.DefaultAuthenticator"/>
-->
<!-- CROWD:END -->
```

For JIRA, add in the Client Jira Authenticator:

```xml
<!-- CAS:START - Java Client Jira Authenticator -->
<authenticator class="org.jasig.cas.client.integration.atlassian.JiraCasAuthenticator"/>
<!-- CAS:END -->
```

For Confluence, add in the Client Confluence Authenticator:

```xml
<!-- CAS:START - Java Client Confluence Authenticator -->
<authenticator class="org.jasig.cas.client.integration.atlassian.ConfluenceCasAuthenticator"/>
<!-- CAS:END -->
```

<a name="confluence-cas-logout"></a>
#### Confluence CAS Logout

As of this writing, Atlassian doesn't support a config option yet (like Jira). To rely on the Single Sign Out functionality to sign off of Confluence we need to modify the logout link.


- Copy `$CONFLUENCE_INSTALL/WEB-INF/lib/confluence-x.x.x.jar` to a temporary directory
- `mkdir /tmp/confluence-jar && cp WEB-INF/lib/confluence-x.y.z.jar /tmp/confluence-jar`
- Unpack the jar
- `cd /tmp/confluence-jar && jar xvf confluence-x.y.z.jar`
- `cp xwork.xml $CONFLUENCE_INSTALL/WEB-INF/classes`
- `cp xwork.xml $CONFLUENCE_INSTALL/WEB-INF/classes/ && cd $CONFLUENCE_INSTALL/WEB-INF/classes/`
- Edit `$CONFLUENCE_INSTALL/WEB-INF/classes/xwork.xml`, find the logout action and comment out the success result and replace it with this one:

```xml
<!-- <result name="success" type="velocity">/logout.vm</result> -->
<!-- CAS:START - CAS Logout Redirect -->
<result name="success" type="redirect">https://cas.institution.edu/cas/logout</result>
<!-- CAS:END -->
```

<a name="copy-jars"></a>
#### Copy Jars
Copy cas-client-core-x.y.x.jar and cas-client-integration-atlassian-x.y.x.jar to `$JIRA_HOME/WEB-INF/lib`
