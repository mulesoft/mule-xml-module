/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.validator;

import static java.util.stream.Collectors.joining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mule.functional.api.exception.ExpectedError.none;
import static org.mule.module.xml.api.XmlError.SCHEMA_NOT_HONOURED;
import static org.mule.runtime.core.api.util.IOUtils.getResourceAsStream;
import org.mule.functional.api.exception.ExpectedError;
import org.mule.module.xml.XmlTestCase;
import org.mule.module.xml.api.SchemaViolation;
import org.mule.runtime.core.api.event.CoreEvent;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Rule;
import org.junit.Test;

public class SchemaValidationTestCase extends XmlTestCase {

  private static final String SIMPLE_SCHEMA = "validation/schema1.xsd";
  private static final String INCLUDE_SCHEMA = "validation/schema-with-include.xsd";

  private static final String VALID_XML_FILE = "validation/validation1.xml";
  private static final String INVALID_XML_FILE = "validation/validation2.xml";

  @Rule
  public ExpectedError expectedError = none();

  @Override
  protected String getConfigFile() {
    return "validation/schema-validator-config.xml";
  }

  @Test
  public void validSchema() throws Exception {
    validate(getValidPayload(), SIMPLE_SCHEMA);
  }

  @Test
  public void invalidSchema() throws Exception {
    expectValidationFailure();
    validate(getInvalidPayload(), SIMPLE_SCHEMA);
  }

  @Test
  public void validWithIncludes() throws Exception {
    validate(getValidPayload(), INCLUDE_SCHEMA);
  }

  @Test
  public void invalidWithIncludes() throws Exception {
    expectValidationFailure();
    validate(getInvalidPayload(), INCLUDE_SCHEMA);
  }

  private void expectValidationFailure() {
    expectedError.expectErrorType(ERROR_NAMESPACE, SCHEMA_NOT_HONOURED.name());
    expectedError.expectEvent(new BaseMatcher<CoreEvent>() {

      @Override
      public boolean matches(Object item) {
        CoreEvent event = (CoreEvent) item;
        List<SchemaViolation> problems = (List<SchemaViolation>) event.getError().get().getErrorMessage().getPayload().getValue();
        assertThat(problems, hasSize(1));
        assertThat(problems.get(0).getDescription(),
                   equalTo("cvc-complex-type.2.4.a: Invalid content was found starting with element 'fail'. One of '{used}' is expected."));

        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("Error validation failed");
      }
    });
  }

  private void validate(InputStream payload, String... schemas) throws Exception {
    flowRunner("validateSchema")
        .withPayload(payload)
        .withVariable("schemas", parseSchemas(schemas))
        .run();
  }

  private String parseSchemas(String... schemas) {
    if (schemas == null || schemas.length == 0) {
      return "";
    }

    return Stream.of(schemas).map(String::trim).collect(joining(", "));
  }

  private InputStream getValidPayload() throws Exception {
    return getResourceAsStream(VALID_XML_FILE, getClass());
  }

  private InputStream getInvalidPayload() throws Exception {
    return getResourceAsStream(INVALID_XML_FILE, getClass());
  }
}
