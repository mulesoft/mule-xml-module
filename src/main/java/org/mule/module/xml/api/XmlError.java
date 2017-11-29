/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.api;

import static java.util.Optional.ofNullable;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import org.mule.runtime.extension.api.error.MuleErrors;

import java.util.Optional;

/**
 * Defines the error types thrown by this module
 *
 * @since 1.0
 */
public enum XmlError implements ErrorTypeDefinition<XmlError> {

  /**
   * The input document is not a valid XML
   */
  INVALID_INPUT_XML,

  /**
   * The supplied XPath expression is invalid
   */
  INVALID_XPATH_EXPRESSION,

  /**
   * A context property with a null value was supplied
   */
  NULL_CONTEXT_PROPERTY,

  /**
   * There was an error transforming an XML document
   */
  TRANSFORMATION(MuleErrors.TRANSFORMATION),

  /**
   * The input XML document does not honour its schema
   */
  SCHEMA_NOT_HONOURED(MuleErrors.VALIDATION),

  /**
   * The schema could not be found
   */
  SCHEMA_NOT_FOUND,

  /**
   * The supplied schema is invalid
   */
  INVALID_SCHEMA;

  private ErrorTypeDefinition<? extends Enum<?>> parentError;

  XmlError(ErrorTypeDefinition<? extends Enum<?>> parentError) {
    this.parentError = parentError;
  }

  XmlError() {}

  @Override
  public Optional<ErrorTypeDefinition<? extends Enum<?>>> getParent() {
    return ofNullable(parentError);
  }

}
