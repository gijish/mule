/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.rss.endpoint;

import org.mule.api.MessagingException;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.config.i18n.CoreMessages;
import org.mule.endpoint.DefaultInboundEndpoint;
import org.mule.module.rss.transformers.ObjectToRssFeed;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TODO
 */
public class RssInboundEndpoint extends DefaultInboundEndpoint
{
    private boolean splitFeed;

    private Date lastUpdate;

    private List<String> acceptedMimeTypes;

    private ObjectToRssFeed inTransform = new ObjectToRssFeed();

    private Set<String> supportedProtocols = new HashSet<String>(2);

    public RssInboundEndpoint(boolean splitFeed, Date lastUpdate, List<String> acceptedContentTypes, InboundEndpoint ie)
    {
        super(ie.getConnector(), ie.getEndpointURI(), ie.getName(),
                ie.getProperties(), ie.getTransactionConfig(), ie.isDeleteUnacceptedMessages(),
                ie.getExchangePattern(), ie.getResponseTimeout(), ie.getInitialState(),
                ie.getEncoding(), ie.getEndpointBuilderName(), ie.getMuleContext(), ie.getRetryPolicyTemplate(), 
                ie.getMessageProcessorsFactory(), ie.getMessageProcessors(), ie.getResponseMessageProcessors(), ie.isDisableTransportTransformer(), ie.getMimeType());
        this.splitFeed = splitFeed;
        this.lastUpdate = lastUpdate;
        this.acceptedMimeTypes = acceptedContentTypes;
    }

    public boolean isSplitFeed()
    {
        return splitFeed;
    }

    public Date getLastUpdate()
    {
        return lastUpdate;
    }

    public List<String> getAcceptedMimeTypes()
    {
        return acceptedMimeTypes;
    }

    void registerSupportedProtocol(String protocol)
    {
        supportedProtocols.add(protocol);
    }

    boolean unregisterProtocol(String protocol)
    {
        return supportedProtocols.remove(protocol);
    }

    @Override
    public boolean isProtocolSupported(String protocol)
    {
        return supportedProtocols.contains(protocol);
    }

    public boolean onMessage(MuleMessage message) throws MuleException
    {
        String mimeType = getMime(message);

        if (getProtocol().startsWith("http") && !isMimeSupported(mimeType))
        {
            if (mimeType == null)
            {
                throw new MessagingException(CoreMessages.createStaticMessage("Mime type not set on message, cannot validate that message is an AtomInboundEndpointFactoryBean feed"), message);
            }
            else
            {
                throw new MessagingException(CoreMessages.createStaticMessage("Mime type not supported '" + mimeType + "', supported types are: " + getAcceptedMimeTypes()), message);
            }
        }
        else if (mimeType == null)
        {
            logger.warn("Mime type not set on message, but connector protocol '" + getProtocol() + "' does not explicitly support mimeTypes. Message type will not be validated");
        }
        message.applyTransformers(inTransform);
        return true;
    }

    public boolean isMimeSupported(String mime)
    {
        return acceptedMimeTypes.contains(mime);
    }

    private String getMime(MuleMessage m)
    {
        if (m == null)
        {
            return null;
        }

        String mimeType = m.getOutboundProperty("Content-Type");
        int i = mimeType.indexOf(";");
        if (i > -1)
        {
            mimeType = mimeType.substring(0, i);
        }
        return mimeType;
    }
}
