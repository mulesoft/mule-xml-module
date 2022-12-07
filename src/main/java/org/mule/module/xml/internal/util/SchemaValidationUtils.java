/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.internal.util;

import org.mule.module.xml.api.SchemaContent;
import org.mule.module.xml.internal.error.SchemaInputException;

import java.util.List;

import com.google.common.base.Strings;

/**
 * General utility methods for checking schema input.
 *
 * @since 1.3.4
 */
public class SchemaValidationUtils {

  private SchemaValidationUtils() {}

  /**
   * perform check on schema location field and schema content field, only one can be used and must provided.
   * @param schemas
   * @param schemaContents
   */
  public static void checkSchemaInput(String schemas, List<SchemaContent> schemaContents) {
    if ((Strings.isNullOrEmpty(schemas) && isBlank(schemaContents))
        || (!Strings.isNullOrEmpty(schemas) && !isBlank(schemaContents))) {
      throw new SchemaInputException("Either Schema or Schema Content must be provided, and you cannot provide both. ");
    }
  }

  public static boolean isBlank(List<SchemaContent> list) {
    return list == null || list.isEmpty();
  }
}
