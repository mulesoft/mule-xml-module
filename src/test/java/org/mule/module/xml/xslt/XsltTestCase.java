/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.xslt;

import static org.custommonkey.xmlunit.XMLUnit.compareXML;
import static org.custommonkey.xmlunit.XMLUnit.setIgnoreWhitespace;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mule.module.xml.api.XmlError.NULL_CONTEXT_PROPERTY;
import static org.mule.runtime.core.api.util.IOUtils.getResourceAsString;
import org.mule.functional.api.exception.ExpectedError;
import org.mule.module.xml.XmlTestCase;

import org.junit.Rule;
import org.junit.Test;

public class XsltTestCase extends XmlTestCase {

  @Rule
  public ExpectedError expectedError = ExpectedError.none();

  @Override
  protected String getConfigFile() {
    return "xsl/xslt-config.xml";
  }

  @Override
  protected void doSetUp() throws Exception {
    setIgnoreWhitespace(true);
  }

  @Test
  public void groupByCities() throws Exception {
    String cities = getResourceAsString("cities.xml", getClass());
    String output = flowRunner("groupCities").withPayload(cities).run().getMessage().getPayload().getValue().toString();
    String expected = getResourceAsString("transformed-cities.xml", getClass());
    assertThat(compareXML(expected, output).similar(), is(true));
  }

  @Test
  public void booksAsCsv() throws Exception {
    String books = getResourceAsString("books.xml", getClass());
    String xslt = getResourceAsString("xsl/books-csv.xsl", getClass());
    String output = flowRunner("booksAsCsv")
        .withPayload(books)
        .withVariable("xsl", xslt)
        .run().getMessage().getPayload().getValue().toString();

    final String expected = "Title,Author,Category,Stock-Value\n" +
        "\"Pride and Prejudice\",\"Jane Austen\",\"MMP(Unclassified)\",\"N/A\"\n" +
        "\"Wuthering Heights\",\"Charlotte Bronte\",\"P(Unclassified)\",\"N/A\"\n" +
        "\"Tess of the d'Urbervilles\",\"Thomas Hardy\",\"P(Unclassified)\",\"N/A\"\n" +
        "\"Jude the Obscure\",\"Thomas Hardy\",\"P(Unclassified)\",\"N/A\"\n" +
        "\"The Big Over Easy\",\"Jasper Fforde\",\"H(Unclassified)\",\"N/A\"\n" +
        "\"The Eyre Affair\",\"Jasper Fforde\",\"P(Unclassified)\",\"N/A\"";

    assertThat(output.trim(), equalTo(expected));
  }

  @Test
  public void nullParameter() throws Exception {
    expectedError.expectErrorType(ERROR_NAMESPACE, NULL_CONTEXT_PROPERTY.name());
    flowRunner("nullParam").withPayload("<parameter/>").run();
  }
}
