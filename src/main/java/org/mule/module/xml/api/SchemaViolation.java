/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.api;

/**
 * Describes one single violation found while validating that an XML input complies with a certain schema
 *
 * @sine 1.0
 */
public class SchemaViolation {

  private int lineNumber;
  private int columnNumber;
  private String description;

  public SchemaViolation() {}

  /**
   * Creates a new instance
   *
   * @param lineNumber   the line number in which the problem was found
   * @param columnNumber the column number in which the problem was found
   * @param description  the description of the problem
   */
  public SchemaViolation(int lineNumber, int columnNumber, String description) {
    this.lineNumber = lineNumber;
    this.columnNumber = columnNumber;
    this.description = description;
  }

  public void setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  /**
   * @return the line number in which the problem was found
   */
  public int getLineNumber() {
    return lineNumber;
  }


  public void setColumnNumber(int columnNumber) {
    this.columnNumber = columnNumber;
  }

  /**
   * @return the column number in which the problem was found
   */
  public int getColumnNumber() {
    return columnNumber;
  }


  public void setDescription(String description) {
    this.description = description;
  }
  /**
   * @return the description of the problem
   */
  public String getDescription() {
    return description;
  }
}
