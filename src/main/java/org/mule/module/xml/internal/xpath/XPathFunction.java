/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.internal.xpath;

import static org.mule.module.xml.api.EntityExpansion.NEVER;

import org.mule.module.xml.api.NamespaceMapping;
import org.mule.module.xml.internal.XmlModule;
import org.mule.module.xml.internal.operation.XPathOperation;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.api.lifecycle.Startable;
import org.mule.runtime.api.lifecycle.Stoppable;
import org.mule.runtime.extension.api.annotation.param.Optional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Similar to the xpath-extract operation but in the form of a DataWeave function.
 *
 * @since 1.0
 */
public class XPathFunction implements Initialisable, Startable, Stoppable {

  // By default, remove trailing newlines, to be backward compatible with previous xml-module versions.
  private static final boolean DEFAULT_KEEP_TRAILING_NEWLINES_CONFIGURATION = false;

  private XPathOperation xpathOperation;
  private XmlModule config = new XmlModule();

  @Override
  public void initialise() throws InitialisationException {
    xpathOperation = new XPathOperation(NEVER);
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

  /**
   * Evaluates the {@code xpath} expression over the given XML {@code content} using the {@code contextProperties}.
   *
   * @param xpath the XPath script
   * @param content the XML content on which the XPath is evaluated
   * @param contextProperties Properties that will be made available to the transform context.
   * @param ns NamespaceMapping Maps a prefix to a namespace URI
   * @return a List of Strings with all the matching elements
   */
  public List<String> xpath(String xpath, InputStream content, Map<String, Object> contextProperties,
                            @Optional NamespaceMapping... ns) {
    List<NamespaceMapping> namespaces = new ArrayList<>();
    if (ns != null)
      namespaces.addAll(Arrays.asList(ns));
    return xpathOperation.xpathExtract(content, xpath, contextProperties, namespaces, null, config,
                                       DEFAULT_KEEP_TRAILING_NEWLINES_CONFIGURATION);
  }
}
