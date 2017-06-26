/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.internal.error;

import static org.mule.module.xml.api.XmlError.INVALID_SCHEMA;
import org.mule.module.xml.api.XmlError;
import org.mule.runtime.extension.api.exception.ModuleException;

/**
 * A {@link ModuleException} associated with the {@link XmlError#INVALID_SCHEMA} type
 *
 * @since 1.0
 */
public class InvalidSchemaException extends ModuleException {

  public InvalidSchemaException(String message) {
    super(message, INVALID_SCHEMA);
  }

  public InvalidSchemaException(String message, Throwable cause) {
    super(message, INVALID_SCHEMA, cause);
  }
}
