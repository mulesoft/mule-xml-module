/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.internal.util;

import org.mule.runtime.core.api.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Resolves entities by using the systemId as a path in the current classloader
 *
 * @since 1.0
 */
public class LocalEntityResolver implements EntityResolver {

  @Override
  public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
    try {
      InputStream is = IOUtils.getResourceAsStream(systemId, getClass());
      if (is != null) {
        return new InputSource(is);
      } else {
        throw new SAXException("URI resource not found: " + systemId);
      }
    } catch (IOException e) {
      throw new SAXException(e);
    }
  }
}
