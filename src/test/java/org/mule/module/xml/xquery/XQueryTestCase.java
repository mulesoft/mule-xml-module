/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.xquery;

import static org.custommonkey.xmlunit.XMLUnit.compareXML;
import static org.custommonkey.xmlunit.XMLUnit.setIgnoreWhitespace;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import org.mule.module.xml.XmlTestCase;
import org.mule.runtime.core.api.util.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.junit.Test;

public class XQueryTestCase extends XmlTestCase {

  private String input;

  @Override
  protected String getConfigFile() {
    return "xquery/xquery-config.xml";
  }

  @Override
  protected void doSetUp() throws Exception {
    setIgnoreWhitespace(true);
    input = IOUtils.getResourceAsString("cd-catalog.xml", getClass());
  }

  private List<String> xquery(String flowName) throws Exception {
    return xquery(flowName, input);
  }

  private List<String> xquery(String flowName, Object input) throws Exception {
    return (List<String>) flowRunner(flowName).withPayload(input).run().getMessage().getPayload().getValue();
  }

  @Test
  public void tryCatch() throws Exception {
    List<String> elements = xquery("tryCatch");
    assertThat(elements, hasSize(1));
    String element = elements.get(0);
    assertThat(element, containsString("error"));
    assertThat(element, containsString("Caught error"));
  }

  @Test
  public void switchStatement() throws Exception {
    List<String> elements = xquery("switch");

    assertThat(elements, hasSize(1));
    assertThat(elements.get(0), containsString("Quack"));
  }

  @Test
  public void groupBy() throws Exception {
    List<String> elements = xquery("groupBy");

    assertThat(elements, hasSize(2));
    assertThat(elements.get(0), equalTo("<odd>1 3 5 7 9</odd>"));
    assertThat(elements.get(1), equalTo("<even>2 4 6 8 10</even>"));
  }

  @Test
  public void books() throws Exception {
    List<String> nodes = xquery("books", getBooks());
    assertThat(nodes, hasSize(6));
    compareXML("<book><author><AUTHOR>Jasper Fforde</AUTHOR></author><title><TITLE>The Eyre Affair</TITLE></title></book>",
               nodes.get(0));
  }

  @Test
  public void multipleInputsByPath() throws Exception {
    URL booksUrl = IOUtils.getResourceAsUrl("books.xml", getClass());
    URL citiesURL = IOUtils.getResourceAsUrl("cities.xml", getClass());

    assertMultipleInputs("multipleInputsByPath", booksUrl.getPath(), citiesURL.getPath());
  }

  private void assertMultipleInputs(String flowName, Object books, Object cities) throws Exception {
    List<String> elements = (List<String>) flowRunner(flowName)
        .withPayload(input).withVariable("books", books)
        .withVariable("cities", cities)
        .run().getMessage().getPayload().getValue();

    assertThat(elements, hasSize(1));

    String element = elements.get(0);
    assertThat(element, containsString("title=\"Pride and Prejudice\""));
    assertThat(element, containsString("city=\"milan\""));
  }

  private String getBooks() throws IOException {
    return IOUtils.getResourceAsString("books.xml", getClass());
  }
}
