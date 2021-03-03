/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.xpath;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mule.runtime.core.api.util.IOUtils.getResourceAsStream;
import org.mule.module.xml.XmlTestCase;
import org.mule.runtime.core.api.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Test;

public class XPathTestCase extends XmlTestCase {

  private static final String HANDKERCHIEF = "handkerchief";
  private static final String FOR_THE_SAME_HANDKERCHIEF = "<LINE>For the same handkerchief?</LINE>";
  private static final String TAG_DEFAULT_XMLNS = "<Title xmlns=\"http://www.books.org/2001/XMLSchema\">Fundamentals</Title>";
  private static final String TITLE_DEFAULT_XMLNS = "Fundamentals";
  private static final Double LINES_COUNT = 3556D;

  @Override
  protected String getConfigFile() {
    return "xpath/xpath-config.xml";
  }

  @Test
  public void xpathWithParameters() throws Exception {
    assertLines("shakespeareLines", getOthello());
  }

  @Test
  public void xpathGetAttributeValue() throws Exception {
    List<String> results = (List<String>) flowRunner("getAttributes")
        .withPayload(getOthello())
        .run().getMessage().getPayload().getValue();

    assertThat(results, hasSize(1));
    assertThat(results.get(0), equalTo("latin"));
  }

  @Test
  public void xpathFunctionWithParametersWithStreamPayload() throws Exception {
    assertLines("shakespeareLinesFunction", getOthello());
  }

  @Test
  public void xpathFunctionWithParametersWithStringPayload() throws Exception {
    assertLines("shakespeareLinesFunction", getOthelloString());
  }

  @Test
  public void xpathFunctionWithParametersAndStreamPayload() throws Exception {
    assertLines("shakespeareLinesFunction", getOthello());
  }

  @Test
  public void xpathFunctionWithParametersAndStringPayload() throws Exception {
    assertLines("shakespeareLinesFunction", getOthelloString());
  }

  @Test
  public void xpathFunctionWithDefaultNamespaceTag() throws Exception {
    List<String> lines = (List<String>) flowRunner("testNamespaceDefault")
        .withPayload(getTestNamespaceDefault())
        .run().getMessage().getPayload().getValue();
    assertThat(lines, hasSize(1));
    assertThat(lines.get(0), equalTo(TAG_DEFAULT_XMLNS));
  }

  @Test
  public void xpathFunctionWithDefaultNamespaceText() throws Exception {
    List<String> lines = (List<String>) flowRunner("testNamespaceDefaultText")
        .withPayload(getTestNamespaceDefault())
        .run().getMessage().getPayload().getValue();
    assertThat(lines, hasSize(1));
    assertThat(lines.get(0), equalTo(TITLE_DEFAULT_XMLNS));
  }

  @Test
  public void xpathFunctionWithDefaultNamespaceError() throws Exception {
    List<String> lines = (List<String>) flowRunner("testNamespaceDefaultError")
        .withPayload(getTestNamespaceDefault())
        .run().getMessage().getPayload().getValue();
    assertThat(lines, hasSize(0));
  }

  @Test
  public void foreach() throws Exception {
    assertForeach("foreach");
  }

  @Test
  public void foreachWithFunction() throws Exception {
    assertForeach("foreachWithFunction");
  }

  private void assertForeach(String flowName) throws Exception {
    List<String> lines =
        (List<String>) flowRunner(flowName).withPayload(getOthello()).run().getMessage().getPayload().getValue();
    assertThat(lines, hasSize(LINES_COUNT.intValue()));
  }

  private void assertLines(String flowName, Object payload) throws Exception {
    List<String> lines = (List<String>) flowRunner(flowName)
        .withPayload(payload)
        .withVariable("word", HANDKERCHIEF)
        .run().getMessage().getPayload().getValue();

    assertThat(lines, hasSize(27));
    assertThat(lines.stream().allMatch(v -> v.contains(HANDKERCHIEF)), is(true));

    String line = lines.get(0);
    assertThat(line, equalTo(FOR_THE_SAME_HANDKERCHIEF));
  }

  private InputStream getOthello() throws IOException {
    return getResourceAsStream("othello.xml", getClass());
  }

  private String getOthelloString() throws IOException {
    return IOUtils.toString(getOthello());
  }

  private InputStream getTestNamespaceDefault() throws IOException {
    return getResourceAsStream("test-namespace-default.xml", getClass());
  }
}
