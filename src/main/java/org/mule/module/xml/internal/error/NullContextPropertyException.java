/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.internal.error;

import static org.mule.module.xml.api.XmlError.NULL_CONTEXT_PROPERTY;
import org.mule.module.xml.api.XmlError;
import org.mule.runtime.extension.api.exception.ModuleException;

/**
 * A {@link ModuleException} associated with the {@link XmlError#NULL_CONTEXT_PROPERTY} type
 *
 * @since 1.0
 */
public class NullContextPropertyException extends ModuleException {

  public NullContextPropertyException(String message) {
    super(message, NULL_CONTEXT_PROPERTY);
  }
}
