/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.pattern.core.config;

import org.mule.config.spring.handlers.MuleNamespaceHandler;
import org.mule.config.spring.parsers.specific.BridgeDefinitionParser;
import org.mule.tck.logging.TestAppender;

import org.apache.logging.log4j.Level;
import org.junit.Test;

public class CoreDeprecationTestCase extends AbstractDeprecationTestCase
{

    @Override
    protected String getConfigFile()
    {
        return "core-config.xml";
    }

    @Test
    public void ensureCoreDeprecation()
    {
        testAppender.ensure(new TestAppender.Expectation(Level.WARN.toString(), BridgeDefinitionParser.class.getName(), MuleNamespaceHandler.PATTERNS_DEPRECATION_MESSAGE));
    }
}