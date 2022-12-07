/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.internal.error;

import static org.mule.module.xml.api.XmlError.SCHEMA_INPUT_ERROR;
import org.mule.module.xml.api.XmlError;
import org.mule.runtime.extension.api.exception.ModuleException;

/**
 * A {@link ModuleException} associated with the {@link XmlError#SCHEMA_INPUT_ERROR} type
 *
 * @since 1.3.4
 */
public class SchemaInputException extends ModuleException {

  public SchemaInputException(String message, Throwable cause) {
    super(message, SCHEMA_INPUT_ERROR, cause);
  }

  public SchemaInputException(String message) {
    super(message, SCHEMA_INPUT_ERROR);
  }
}
