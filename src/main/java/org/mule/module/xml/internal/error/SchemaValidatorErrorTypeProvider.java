/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.internal.error;

import static org.mule.module.xml.api.XmlError.INVALID_INPUT_XML;
import static org.mule.module.xml.api.XmlError.INVALID_SCHEMA;
import static org.mule.module.xml.api.XmlError.SCHEMA_NOT_FOUND;
import static org.mule.module.xml.api.XmlError.SCHEMA_NOT_HONOURED;
import static org.mule.module.xml.api.XmlError.TRANSFORMATION;
import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import java.util.HashSet;
import java.util.Set;

public class SchemaValidatorErrorTypeProvider implements ErrorTypeProvider {

  @Override
  public Set<ErrorTypeDefinition> getErrorTypes() {
    Set<ErrorTypeDefinition> errors = new HashSet<>();
    errors.add(SCHEMA_NOT_HONOURED);
    errors.add(TRANSFORMATION);
    errors.add(INVALID_INPUT_XML);
    errors.add(INVALID_SCHEMA);
    errors.add(SCHEMA_NOT_FOUND);

    return errors;
  }
}
