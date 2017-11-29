/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.internal;

import static org.mule.module.xml.api.EntityExpansion.NEVER;
import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import org.mule.module.xml.api.EntityExpansion;
import org.mule.module.xml.api.NamespaceMapping;
import org.mule.module.xml.api.XmlError;
import org.mule.module.xml.internal.operation.SchemaValidatorOperation;
import org.mule.module.xml.internal.operation.XPathOperation;
import org.mule.module.xml.internal.operation.XQueryOperation;
import org.mule.module.xml.internal.operation.XsltOperation;
import org.mule.module.xml.internal.xpath.XPathFunction;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.ExpressionFunctions;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.runtime.extension.api.annotation.error.ErrorTypes;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import java.util.ArrayList;
import java.util.List;

/**
 * A Mule Module which provides functionality to handle XML. This module should be used for advanced use cases or use cases
 * associated with XML standards such as XSLT, XPath and XQuery or XSD.
 * Take in mind that DataWeave is not only perfectly capable to handle most of those use cases and can replace those standards
 * the majority of the times, but it's also our recommended approach.
 *
 * @since 1.0
 */
@Extension(name = "XML")
@Operations({XsltOperation.class, XPathOperation.class, XQueryOperation.class, SchemaValidatorOperation.class})
@ErrorTypes(XmlError.class)
@ExpressionFunctions(XPathFunction.class)
@Xml(prefix = "xml-module")
public class XmlModule {

  /**
   * Allows to globally map prefixes to namespace uris. On this version, the xpath-extract operation is the only one
   * to consume these mappings
   */
  @Parameter
  @Optional
  @NullSafe
  @Expression(NOT_SUPPORTED)
  private List<NamespaceMapping> namespaces = new ArrayList<>();

  /**
   * Defines how to treat entity expansion. Setting a value different than NEVER renders the application
   * vulnerable to XXE and/or DoS attacks
   */
  @Parameter
  @Optional(defaultValue = "NEVER")
  @Summary("Set to NEVER to prevent XXE and DoS attacks")
  private EntityExpansion expandEntities = NEVER;

  public List<NamespaceMapping> getNamespaces() {
    return namespaces;
  }

}
