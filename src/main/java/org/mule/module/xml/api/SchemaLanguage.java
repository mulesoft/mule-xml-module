/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.api;

import javax.xml.XMLConstants;

/**
 * Lists the schema languages supported
 */
public enum SchemaLanguage {

  /**
   * The default http://www.w3.org/2001/XMLSchema language
   */
  W3C(XMLConstants.W3C_XML_SCHEMA_NS_URI),

  /**
   * The default http://relaxng.org/ns/structure/1.0 language
   */
  RELAXNG(XMLConstants.RELAXNG_NS_URI);

  private final String languageUri;

  SchemaLanguage(String uri) {
    languageUri = uri;
  }

  public String getLanguageUri() {
    return languageUri;
  }
}
