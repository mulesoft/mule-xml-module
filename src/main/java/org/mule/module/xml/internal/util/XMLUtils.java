/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.internal.util;

import org.mule.module.xml.internal.error.InvalidInputXmlException;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;

/**
 * General utility methods for working with XML.
 *
 * @since 1.0
 */
public class XMLUtils {

  /**
   * Transforms the source XML using the given {@code factory}
   *
   * @param src     XML content
   * @param factory a document factory
   * @return a {@link Node}
   */
  public static Node toDOMNode(InputStream src, DocumentBuilderFactory factory) {
    return toDOMNode(src, factory, null);
  }

  /**
   * Transforms the source XML using the given {@code factory} and {@code entityResolver}
   *
   * @param src            XML content
   * @param factory        a document factory
   * @param entityResolver an {@link EntityResolver}
   * @return a {@link Node}
   */
  public static Node toDOMNode(InputStream src, DocumentBuilderFactory factory, EntityResolver entityResolver) {
    try {
      final DocumentBuilder documentBuilder = factory.newDocumentBuilder();
      if (entityResolver != null) {
        documentBuilder.setEntityResolver(entityResolver);
      }
      return documentBuilder.parse(src);
    } catch (Exception e) {
      throw new InvalidInputXmlException("Cannot parse input XML because it is invalid.", e);
    }
  }
}

