/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.internal.error;

import static org.mule.module.xml.api.XmlError.INVALID_XPATH_EXPRESSION;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import java.util.Set;

/**
 * Provides the error types for the XPath operation
 *
 * @since 1.0
 */
public class XPathErrorTypeProvider extends StandardXmlErrorTypeProvider {

  @Override
  public Set<ErrorTypeDefinition> getErrorTypes() {
    Set<ErrorTypeDefinition> errors = super.getErrorTypes();
    errors.add(INVALID_XPATH_EXPRESSION);

    return errors;
  }
}
