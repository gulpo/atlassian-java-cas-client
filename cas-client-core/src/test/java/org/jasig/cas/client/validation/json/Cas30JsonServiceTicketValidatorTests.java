/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.client.validation.json;

import org.jasig.cas.client.PublicTestHttpServer;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorage;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorageImpl;
import org.jasig.cas.client.proxy.ProxyRetriever;
import org.jasig.cas.client.validation.AbstractTicketValidatorTests;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

public class Cas30JsonServiceTicketValidatorTests extends AbstractTicketValidatorTests {
    private static final PublicTestHttpServer server = PublicTestHttpServer.instance(8088);

    private Cas30JsonServiceTicketValidator ticketValidator;

    @Before
    public void setUp() throws Exception {
        this.ticketValidator = new Cas30JsonServiceTicketValidator(CONST_CAS_SERVER_URL_PREFIX + "8088");
        this.ticketValidator.setProxyCallbackUrl("test");
        this.ticketValidator.setProxyGrantingTicketStorage(getProxyGrantingTicketStorage());
        this.ticketValidator.setProxyRetriever(getProxyRetriever());
        this.ticketValidator.setRenew(true);
    }

    private ProxyGrantingTicketStorage getProxyGrantingTicketStorage() {
        return new ProxyGrantingTicketStorageImpl();
    }

    private ProxyRetriever getProxyRetriever() {
        return new ProxyRetriever() {

            /** Unique Id for serialization. */
            private static final long serialVersionUID = 1L;

            @Override
            public String getProxyTicketIdFor(final String proxyGrantingTicketId, final String targetService) {
                return "test";
            }
        };
    }

    @Test
    public void testSuccessfulJsonResponse() throws Exception {
        final String RESPONSE = "{ " +
            "\"serviceResponse\" : {  "  +
                "\"authenticationSuccess\" : {   " +
                    "\"user\" : \"casuser\",  " +
                    "\"proxyGrantingTicket\" : \"PGTIOU-84678-8a9d\" ," +
                    "\"attributes\" : {      " +
                        "\"cn\" : [ \"Name\" ]  " +
                    '}' +
                '}' +
            '}' +
        '}';

        server.content = RESPONSE.getBytes(server.encoding);
        final Assertion assertion = ticketValidator.validate("test", "test");
        Assert.assertEquals(assertion.getPrincipal().getName(), "casuser");
        Assert.assertTrue(assertion.getPrincipal().getAttributes().containsKey("cn"));
    }

    @Test(expected = TicketValidationException.class)
    public void testFailingJsonResponse() throws Exception {
        final String RESPONSE = "{ " +
                "\"serviceResponse\" : {  "  +
                    "\"authenticationFailure\" : {   " +
                        "\"code\" : \"INVALID_TICKET\",  " +
                        "\"description\" : \"Description\"  " +
                    '}' +
                '}' +
            '}';

        server.content = RESPONSE.getBytes(server.encoding);
        ticketValidator.validate("test", "test");

    }


    @Test
    public void testSuccessfulXmlResponseWithJson() throws Exception {
        final String RESPONSE = "<cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'><cas:authenticationSuccess><cas:user>"
                + "test</cas:user><cas:proxyGrantingTicket>PGTIOU</cas:proxyGrantingTicket></cas:authenticationSuccess></cas:serviceResponse>";
        server.content = RESPONSE.getBytes(server.encoding);
        ticketValidator.validate("test", "test");
    }
}
