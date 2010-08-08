/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.endpoint.outbound;

import org.mule.MessageExchangePattern;
import org.mule.api.MuleEvent;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.processor.MessageProcessor;
import org.mule.api.routing.filter.Filter;
import org.mule.api.security.EndpointSecurityFilter;
import org.mule.api.transaction.TransactionConfig;
import org.mule.api.transformer.Transformer;
import org.mule.endpoint.AbstractMessageProcessorTestCase;
import org.mule.processor.builder.InterceptingChainMessageProcessorBuilder;
import org.mule.tck.testmodels.mule.TestMessageProcessor;

/**
 * Unit test for configuring message processors on an outbound endpoint.
 */
public class OutboundEndpointMessageProcessorsTestCase extends AbstractMessageProcessorTestCase
{
    private MuleEvent testOutboundEvent;
    private OutboundEndpoint endpoint;
    private MuleEvent result;

    @Override
    protected void doSetUp() throws Exception
    {
        super.doSetUp();
        endpoint = createOutboundEndpoint(null, null, null, null, 
            MessageExchangePattern.REQUEST_RESPONSE, null);
        testOutboundEvent = createTestOutboundEvent(endpoint);
    }

    public void testProcessors() throws Exception
    {
        InterceptingChainMessageProcessorBuilder builder = new InterceptingChainMessageProcessorBuilder();
        builder.chain(new TestMessageProcessor("1"), new TestMessageProcessor("2"), new TestMessageProcessor("3"));
        MessageProcessor mpChain = builder.build();
        
        result = mpChain.process(testOutboundEvent);
        assertEquals(TEST_MESSAGE + ":1:2:3", result.getMessage().getPayload());
    }

    public void testNoProcessors() throws Exception
    {
        InterceptingChainMessageProcessorBuilder builder = new InterceptingChainMessageProcessorBuilder();
        MessageProcessor mpChain = builder.build();
        
        result = mpChain.process(testOutboundEvent);
        assertEquals(TEST_MESSAGE, result.getMessage().getPayload());
    }

    protected OutboundEndpoint createOutboundEndpoint(Filter filter,
                                                      EndpointSecurityFilter securityFilter,
                                                      Transformer in,
                                                      Transformer response,
                                                      MessageExchangePattern exchangePattern,
                                                      TransactionConfig txConfig) throws Exception
    {
        return createTestOutboundEndpoint(filter, securityFilter, in, response, exchangePattern, 
            txConfig);
    }
}
