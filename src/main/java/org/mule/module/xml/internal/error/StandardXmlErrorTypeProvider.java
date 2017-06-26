/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.internal.error;

import static org.mule.module.xml.api.XmlError.INVALID_INPUT_XML;
import static org.mule.module.xml.api.XmlError.NULL_CONTEXT_PROPERTY;
import static org.mule.module.xml.api.XmlError.TRANSFORMATION;
import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import java.util.HashSet;
import java.util.Set;

/**
 * Base error type provider which contains basic error types common to most operations
 *
 * @since 1.0
 */
public class StandardXmlErrorTypeProvider implements ErrorTypeProvider {

  @Override
  public Set<ErrorTypeDefinition> getErrorTypes() {
    Set<ErrorTypeDefinition> errors = new HashSet<>();
    errors.add(INVALID_INPUT_XML);
    errors.add(TRANSFORMATION);
    errors.add(INVALID_INPUT_XML);
    errors.add(NULL_CONTEXT_PROPERTY);

    return errors;
  }
}
