/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.client;

import org.mule.DefaultMuleEvent;
import org.mule.DefaultMuleMessage;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.client.LocalMuleClient;
import org.mule.api.endpoint.EndpointBuilder;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.transport.ReceiveException;
import org.mule.session.DefaultMuleSession;

import java.util.Map;

import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentMap;

public class DefaultLocalMuleClient implements LocalMuleClient
{

    protected MuleContext muleContext;
    private ConcurrentMap inboundEndpointCache = new ConcurrentHashMap();
    private ConcurrentMap outboundEndpointCache = new ConcurrentHashMap();

    public DefaultLocalMuleClient(MuleContext muleContext)
    {
        this.muleContext = muleContext;
    }

    public MuleMessage process(OutboundEndpoint endpoint,
                               Object payload,
                               Map<String, Object> messageProperties) throws MuleException
    {
        return process(endpoint, new DefaultMuleMessage(payload, messageProperties, muleContext));

    }

    public MuleMessage process(OutboundEndpoint endpoint, MuleMessage message) throws MuleException
    {
        return returnMessage(endpoint.process(createMuleEvent(message, endpoint)));
    }

    public MuleMessage request(InboundEndpoint endpoint, long timeout) throws MuleException
    {
        try
        {
            return endpoint.request(timeout);
        }
        catch (Exception e)
        {
            throw new ReceiveException(endpoint, timeout, e);
        }
    }

    public void dispatch(String url, Object payload, Map<String, Object> messageProperties)
        throws MuleException
    {
        dispatch(url, new DefaultMuleMessage(payload, messageProperties, muleContext));
    }

    public MuleMessage send(String url, Object payload, Map<String, Object> messageProperties)
        throws MuleException
    {
        return send(url, new DefaultMuleMessage(payload, messageProperties, muleContext));
    }

    public MuleMessage send(String url, MuleMessage message) throws MuleException
    {
        OutboundEndpoint endpoint = getOutboundEndpoint(url, MessageExchangePattern.REQUEST_RESPONSE, null);
        return returnMessage(endpoint.process(createMuleEvent(message, endpoint)));
    }

    public MuleMessage send(String url, Object payload, Map<String, Object> messageProperties, long timeout)
        throws MuleException
    {
        return send(url, new DefaultMuleMessage(payload, messageProperties, muleContext), timeout);

    }

    public MuleMessage send(String url, MuleMessage message, long timeout) throws MuleException
    {
        OutboundEndpoint endpoint = getOutboundEndpoint(url, MessageExchangePattern.REQUEST_RESPONSE, timeout);
        return returnMessage(endpoint.process(createMuleEvent(message, endpoint)));
    }

    public void dispatch(String url, MuleMessage message) throws MuleException
    {
        OutboundEndpoint endpoint = getOutboundEndpoint(url, MessageExchangePattern.ONE_WAY, null);
        endpoint.process(createMuleEvent(message, endpoint));
    }

    public MuleMessage request(String url, long timeout) throws MuleException
    {
        InboundEndpoint endpoint = getInboundEndpoint(url, MessageExchangePattern.ONE_WAY);
        try
        {
            return endpoint.request(timeout);
        }
        catch (Exception e)
        {
            throw new ReceiveException(endpoint, timeout, e);
        }
    }

    public MuleMessage process(String uri,
                               MessageExchangePattern mep,
                               Object payload,
                               Map<String, Object> messageProperties) throws MuleException
    {
        return process(uri, mep, new DefaultMuleMessage(payload, messageProperties, muleContext));
    }

    public MuleMessage process(String uri, MessageExchangePattern mep, MuleMessage message)
        throws MuleException
    {
        OutboundEndpoint endpoint = getOutboundEndpoint(uri, mep, null);
        return returnMessage(endpoint.process(createMuleEvent(message, endpoint)));
    }

    protected MuleEvent createMuleEvent(MuleMessage message, OutboundEndpoint endpoint)
    {
        DefaultMuleSession session = new DefaultMuleSession(muleContext);
        return new DefaultMuleEvent(message, endpoint, session);
    }

    protected MuleMessage returnMessage(MuleEvent event)
    {
        if (event != null)
        {
            return event.getMessage();
        }
        else
        {
            return null;
        }
    }

    protected OutboundEndpoint getOutboundEndpoint(String uri,
                                                   MessageExchangePattern mep,
                                                   Long responseTimeout) throws MuleException
    {
        OutboundEndpoint endpoint = (OutboundEndpoint) outboundEndpointCache.get(uri + ":" + mep.toString()
                                                                                 + ":" + responseTimeout);
        if (endpoint == null)
        {
            EndpointBuilder endpointBuilder = muleContext.getRegistry()
                .lookupEndpointFactory()
                .getEndpointBuilder(uri);
            endpointBuilder.setExchangePattern(mep);
            if (responseTimeout != null && responseTimeout > 0)
            {
                endpointBuilder.setResponseTimeout(responseTimeout.intValue());
            }
            endpoint = muleContext.getRegistry().lookupEndpointFactory().getOutboundEndpoint(endpointBuilder);
            OutboundEndpoint concurrentlyAddedEndpoint = (OutboundEndpoint) outboundEndpointCache.putIfAbsent(
                uri + ":" + mep.toString() + ":" + responseTimeout, endpoint);
            if (concurrentlyAddedEndpoint != null)
            {
                return concurrentlyAddedEndpoint;
            }
        }
        return endpoint;
    }

    protected InboundEndpoint getInboundEndpoint(String uri, MessageExchangePattern mep) throws MuleException
    {
        InboundEndpoint endpoint = (InboundEndpoint) inboundEndpointCache.get(uri + ":" + mep.toString());
        if (endpoint == null)
        {
            EndpointBuilder endpointBuilder = muleContext.getRegistry()
                .lookupEndpointFactory()
                .getEndpointBuilder(uri);
            endpointBuilder.setExchangePattern(mep);
            endpoint = muleContext.getRegistry().lookupEndpointFactory().getInboundEndpoint(endpointBuilder);
            InboundEndpoint concurrentlyAddedEndpoint = (InboundEndpoint) inboundEndpointCache.putIfAbsent(
                uri + ":" + mep.toString(), endpoint);
            if (concurrentlyAddedEndpoint != null)
            {
                return concurrentlyAddedEndpoint;
            }
        }
        return endpoint;
    }

}
