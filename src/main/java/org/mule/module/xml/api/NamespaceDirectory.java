/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.api;

import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.dsl.xml.TypeDsl;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import java.util.ArrayList;
import java.util.List;

@Alias("namespaceDirectory")
@TypeDsl(allowInlineDefinition = false, allowTopLevelDefinition = true)
public class NamespaceDirectory {

  /**
   * Globally maps prefixes to namespace uris. On this version, the xpath-extract operation is the only one
   * to consume these mappings
   */
  @Parameter
  @Optional
  @NullSafe
  @Expression(NOT_SUPPORTED)
  private List<NamespaceMapping> namespaces = new ArrayList<>();

  public NamespaceDirectory() {}

  public void setNamespaces(List<NamespaceMapping> namespaces) {
    this.namespaces = namespaces;
  }

  public List<NamespaceMapping> getNamespaces() {
    return namespaces;
  }
}
