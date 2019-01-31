/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.xpath;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import org.mule.module.xml.XmlTestCase;

import java.io.InputStream;
import java.util.List;

import org.junit.Test;

public class XPathNamespaceTestCase extends XmlTestCase {

  @Override
  protected void doSetUpBeforeMuleContextCreation() throws Exception {
    // Tests developed according to old Saxon version, where no newlines where added at the
    // end of xml results.
    super.doSetUpBeforeMuleContextCreation();
    System.setProperty("xmlModuleKeepNewlinesConfig", "false");
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
    String expected = "<ns1:echo xmlns:ns1=\"http://simple.component.mule.org/\"\n"
        + "          xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">Hello!</ns1:echo>";
    assertThat(result.get(0), equalTo(expected));
  }

  private InputStream getEnvelope() {
    return getClass().getResourceAsStream("/request.xml");
  }
}
