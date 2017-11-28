/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.internal.operation;

import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.ConfigOverride;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

/**
 * Parameter group for security related parameters
 *
 * @since 1.0
 */
public class SecuritySettings {

  /**
   * Whether to accept XML documents with references to external files.
   * Note that even if allowed, these won't be expanded when expandInternalEntities is false.
   * Default value is false. Setting this value to true will make your application
   * vulnerable to XXE attacks. Use with extreme care.
   */
  @Parameter
  @ConfigOverride
  @Expression(NOT_SUPPORTED)
  @Placement(order = 1, tab = "Security")
  @Summary("Set to false to prevent XXE attacks")
  private boolean acceptExternalEntities;

  /**
   * Whether to expand internal entity references in the XML body.
   * Default value is false. Setting this value to true will make your application
   * vulnerable to Billion Laughs (DoS) attacks. Use with extreme care.
   */
  @Parameter
  @ConfigOverride
  @Expression(NOT_SUPPORTED)
  @Placement(order = 2, tab = "Security")
  @Summary("Set to false to prevent DoS attacks")
  private boolean expandInternalEntities;

  public SecuritySettings() {}

  public SecuritySettings(boolean acceptExternalEntities, boolean expandInternalEntities) {
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
