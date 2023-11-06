/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.api;

import static org.mule.module.xml.api.XmlError.SCHEMA_NOT_HONOURED;
import org.mule.runtime.api.exception.ErrorMessageAwareException;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.util.List;

/**
 * A {@link ModuleException} associated with the {@link XmlError#SCHEMA_NOT_HONOURED} type
 *
 * @since 1.0
 */
public class SchemaValidationException extends ModuleException implements ErrorMessageAwareException {

  private Message message;


  /**
   * Creates a new instance
   *
   * @param message  the exception description
   * @param problems the list of {@link SchemaViolation} found
   */
  public SchemaValidationException(String message, List<SchemaViolation> problems) {
    super(message, SCHEMA_NOT_HONOURED);
    this.message = Message.builder().collectionValue(problems, SchemaViolation.class).build();
  }


  public void setMessage(Message message) {
    this.message = message;
  }

  @Override
  public Message getErrorMessage() {
    return message;
  }
}
