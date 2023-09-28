/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.api;

/**
 * Security property which defines how to treat entities expansion.
 *
 * @since 1.0
 */
public enum EntityExpansion {

  /**
   * Never accept XML documents with references to external files nor expand internal entity references in the XML body.
   * This prevents XXE and a Billion Laughs (DoS) attacks
   */
  NEVER(false, false),

  /**
   * Never accept XML documents with references to external files but do expand internal entity references in the XML body.
   * This prevents XXE but remains vulnerable to a Billion Laughs (DoS) attacks. Use under your own risk with trusted documents
   * only
   */
  INTERNAL(false, true),

  /**
   * Accept XML documents with references to external files and expand internal entity references in the XML body.
   * This is vulnerable to XXE and a Billion Laughs (DoS) attacks. Use under your own risk with trusted documents
   * only
   */
  ALL(true, true);

  private final boolean acceptExternalEntities;
  private final boolean expandInternalEntities;

  EntityExpansion(boolean acceptExternalEntities, boolean expandInternalEntities) {
    this.acceptExternalEntities = acceptExternalEntities;
    this.expandInternalEntities = expandInternalEntities;
  }

  public boolean isAcceptExternalEntities() {
    return acceptExternalEntities;
  }

  public boolean isExpandInternalEntities() {
    return expandInternalEntities;
  }
}
