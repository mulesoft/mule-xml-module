/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.xslt;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mule.functional.api.exception.ExpectedError.none;
import static org.mule.module.xml.api.XmlError.INVALID_INPUT_XML;
import org.mule.functional.api.exception.ExpectedError;
import org.mule.module.xml.XmlTestCase;
import org.mule.runtime.core.api.util.IOUtils;

import org.junit.Rule;
import org.junit.Test;

/**
 * This test case validates that by default
 * the XSLT transformer is not vulnerable to
 * External Entity Processing attack unless explicitly allowed
 * <p>
 * <b>EIP Reference:</b> <a
 * href="https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Processing"<a/>
 * </p>
 */
public class XsltTransformerXXETestCase extends XmlTestCase {

  @Rule
  public ExpectedError expectedError = none();

  private String input = makeInput();

  @Override
  protected String getConfigFile() {
    return "xsl/xslt-xxe-config.xml";
  }

  @Test
  public void xxeSafe() throws Exception {
    assertSafe("safeXxe");
  }

  @Test
  public void overriddenSafe() throws Exception {
    assertSafe("overriddenSafe");
  }

  @Test
  public void unsafeXxe() throws Exception {
    assertUnsafe("unsafeXxe");
  }

  @Test
  public void overriddenUnsafe() throws Exception {
    assertUnsafe("overriddenUnsafe");
  }

  private void assertSafe(String flowName) throws Exception {
    expectedError.expectErrorType(ERROR_NAMESPACE, INVALID_INPUT_XML.name());
    flowRunner(flowName).withPayload(input.getBytes()).run();
  }

  private void assertUnsafe(String flowName) throws Exception {
    assertThat(flowRunner(flowName).withPayload(input).run().getMessage().getPayload().getValue().toString(),
               containsString("secret"));
  }

  private String makeInput() {
    return String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<!DOCTYPE spi_doc_type[ <!ENTITY spi_entity_ref SYSTEM 'file:%s'>]>\n" +
        "<root>\n" +
        "<elem>&spi_entity_ref;</elem>\n" +
        "<something/>\n" +
        "</root>", IOUtils.getResourceAsUrl("xxe-passwd.txt", this.getClass()).getPath());
  }
}
