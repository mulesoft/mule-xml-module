/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.internal.xpath;

import static java.util.Collections.emptyList;
import org.mule.module.xml.internal.XmlModule;
import org.mule.module.xml.internal.operation.SecuritySettings;
import org.mule.module.xml.internal.operation.XPathOperation;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.lifecycle.Stoppable;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 
 */
public class XPathFunction implements Initialisable, Startable, Stoppable {

  private XPathOperation xpathOperation;
  private XmlModule config = new XmlModule();

  @Override
  public void initialise() throws InitialisationException {
    xpathOperation = new XPathOperation(new SecuritySettings(false, false));
    xpathOperation.initialise();
  }

  @Override
  public void start() throws MuleException {
    xpathOperation.start();
  }

  @Override
  public void stop() throws MuleException {
    xpathOperation.stop();
  }

  public List<String> xpath(String xpath, InputStream content, Map<String, Object> contextProperties) {
    return xpathOperation.xpathExtract(content, xpath, contextProperties, emptyList(), config);
  }
}
