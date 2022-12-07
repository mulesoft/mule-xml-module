/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.internal.util;

import org.mule.module.xml.api.SchemaContent;
import org.mule.module.xml.internal.error.SchemaInputException;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class SchemaValidationUtilsTestCase {

  @Test(expected = SchemaInputException.class)
  public void testCheckSchemaInputWithEmptyValues() {
    SchemaValidationUtils.checkSchemaInput(null, null);
  }

  @Test(expected = SchemaInputException.class)
  public void testCheckSchemaInputWithBothValues() {
    SchemaContent content = new SchemaContent("schemaText", "schemaName");
    List<SchemaContent> list = new ArrayList<>();
    list.add(content);
    SchemaValidationUtils.checkSchemaInput("schema.xsd", list);
  }

  public void testCheckSchemaInputWithValues() {
    SchemaContent content = new SchemaContent("schemaText", "schemaName");
    List<SchemaContent> list = new ArrayList<>();
    list.add(content);
    SchemaValidationUtils.checkSchemaInput("schema.xsd", null);
    SchemaValidationUtils.checkSchemaInput("", list);
  }
}
