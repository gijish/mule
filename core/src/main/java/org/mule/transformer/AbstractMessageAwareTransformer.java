/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transformer;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.DataType;
import org.mule.api.transformer.TransformerException;
import org.mule.config.i18n.CoreMessages;

/**
 * <code>AbstractMessageAwareTransformer</code> is a transformer that has a reference
 * to the current message. This message can be used obtains properties associated
 * with the current message useful to the transform. Note that when part of a
 * transform chain, the Message payload reflects the pre-transform message state,
 * unless there is no current event for this thread, then the message will be a new
 * DefaultMuleMessage with the src as its payload. Transformers should always work on the
 * src object not the message payload.
 *
 * @see org.mule.api.MuleMessage
 * @see org.mule.DefaultMuleMessage
 */

public abstract class AbstractMessageAwareTransformer extends AbstractTransformer implements MessageAwareTransformer
{

    @Override
    public boolean isSourceDataTypeSupported(DataType<?> dataType, boolean exactMatch)
    {
        //TODO RM* This is a bit of hack since we could just register MuleMessage as a supportedType, but this has some
        //funny behaviour in certain ObjectToXml transformers
        return (super.isSourceDataTypeSupported(dataType, exactMatch) || MuleMessage.class.isAssignableFrom(dataType.getType()));
    }

    @Override
    public final Object doTransform(Object src, String enc) throws TransformerException
    {
        return doTransform(src, enc, null);
    }
    
    public final Object doTransform(Object src, String enc, MuleEvent event) throws TransformerException
    {
        MuleMessage message;
        if (src instanceof MuleMessage)
        {
            message = (MuleMessage) src;
        }
        else if (muleContext.getConfiguration().isAutoWrapMessageAwareTransform())
        {
            message = new DefaultMuleMessage(src, muleContext);
        }
        else
        {
            if (event == null)
            {
                throw new TransformerException(CoreMessages.noCurrentEventForTransformer(), 
                    (MuleEvent) null, this);
            }
            message = event.getMessage();
            if (!message.getPayload().equals(src))
            {
                throw new IllegalStateException("Transform payload does not match current event");
            }
        }
        return transform(message, enc);
    }

    public abstract Object transform(MuleMessage message, String outputEncoding) throws TransformerException;

}
