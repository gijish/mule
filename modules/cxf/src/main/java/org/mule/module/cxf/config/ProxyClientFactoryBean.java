/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.cxf.config;

import org.mule.module.cxf.CxfOutboundMessageProcessor;
import org.mule.module.cxf.builder.ProxyClientMessageProcessorBuilder;

import org.springframework.beans.factory.FactoryBean;

public class ProxyClientFactoryBean extends ProxyClientMessageProcessorBuilder implements FactoryBean
{

    public Object getObject() throws Exception
    {
        return build();
    }

    public Class<?> getObjectType()
    {
        return CxfOutboundMessageProcessor.class;
    }

    public boolean isSingleton()
    {
        return true;
    }

}
