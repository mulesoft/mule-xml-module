/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.api;

import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;

import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Text;

import java.util.Objects;

public class SchemaContent {

  /*
   * Schema name content to define.
   */
  @Parameter
  @Expression(NOT_SUPPORTED)
  private String schemaName;

  /*
   * Schema text content to define.
   */
  @Parameter
  @Text
  @Expression(NOT_SUPPORTED)
  private String schemaText;

  public SchemaContent() {}

  public SchemaContent(final String schemaText, final String schemaName) {
    this.schemaText = schemaText;
    this.schemaName = schemaName;
  }

  public String getSchemaText() {
    return schemaText;
  }

  public String getSchemaName() {
    return schemaName;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof SchemaContent)) {
      return false;
    }
    SchemaContent other = (SchemaContent) obj;
    return Objects.equals(schemaText, other.schemaText) && Objects.equals(schemaName, other.schemaName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(schemaText, schemaName);
  }

}
