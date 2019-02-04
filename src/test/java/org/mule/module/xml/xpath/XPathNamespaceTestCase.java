/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.xpath;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import org.mule.module.xml.XmlTestCase;
import org.mule.tck.junit4.rule.SystemProperty;
import org.mule.test.runner.RunnerDelegateTo;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.Parameterized;

@RunnerDelegateTo(Parameterized.class)
public class XPathNamespaceTestCase extends XmlTestCase {
  private static final String KEEP_NEWLINES_PROPERTY_NAME = "xmlModuleShouldAddTrailingNewlinesProperty";
  private final static String COMMON_EXPECTED_ANSWER = "<ns1:echo xmlns:ns1=\"http://simple.component.mule.org/\"\n"
      + "          xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">Hello!</ns1:echo>";

  @Rule
  public SystemProperty shouldAddTrailingNewlines;
  private String expectedAnswer;

  public XPathNamespaceTestCase(String addTrailingNewlinesProperty, String expectedAnswer) {
    this.expectedAnswer = expectedAnswer;
    // Set a systemProperty consumed by the test app
    this.shouldAddTrailingNewlines = new SystemProperty(KEEP_NEWLINES_PROPERTY_NAME, addTrailingNewlinesProperty);
  }

  @Parameterized.Parameters(name = "Testing with 'should add trailing new lines' property at: {0}")
  public static Collection<Object[]> data() {
    return asList(new Object[][] {
        {"false", COMMON_EXPECTED_ANSWER},
        {"true", COMMON_EXPECTED_ANSWER + "\n"}
    });
  }

  @Override
  protected String getConfigFile() {
    return "xpath/xpath-namespace-config.xml";
  }

  @Test
  public void xpathWithNamespaces() throws Exception {
    assertXpathWihNamespace("xpathWithFullNs");
  }

  @Test
  public void xpathWithMergedNamespaces() throws Exception {
    assertXpathWihNamespace("xpathWithMergedNs");
  }

  private void assertXpathWihNamespace(String flowName) throws Exception {
    List<String> result =
        (List<String>) flowRunner(flowName)
            .withPayload(getEnvelope())
            .run().getMessage().getPayload().getValue();

    assertThat(result, hasSize(1));
    assertThat(result.get(0), equalTo(expectedAnswer));
  }

  private InputStream getEnvelope() {
    return getClass().getResourceAsStream("/request.xml");
  }
}
