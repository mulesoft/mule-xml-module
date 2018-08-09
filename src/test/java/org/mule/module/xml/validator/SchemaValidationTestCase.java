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
import static org.mule.runtime.core.api.util.xmlsecurity.XMLSecureFactories.EXTERNAL_ENTITIES_PROPERTY;
import org.mule.functional.api.exception.ExpectedError;
import org.mule.module.xml.XmlTestCase;
import org.mule.module.xml.api.SchemaViolation;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.tck.junit4.rule.SystemProperty;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Rule;
import org.junit.Test;

public class SchemaValidationTestCase extends XmlTestCase {


  // this is disabled (secure) by default, so we need to change it for the test
  @Rule
  public SystemProperty externalEntities = new SystemProperty(EXTERNAL_ENTITIES_PROPERTY, "true");

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
  public void schemaWithIncludedTypes() throws Exception {
    expectedError.expectErrorType(ERROR_NAMESPACE, SCHEMA_NOT_HONOURED.name());
    validate("validateSchemaWithReferences",
             getResourceAsStream("validation/referencing/xml-with-referencing-schema.xml"),
             "validation/referencing/schema-with-references.xsd");
  }

  @Test
  public void extractErrorsUsingExpressions() throws Exception {
    Event event = validate("extractErrorsFromException", getInvalidPayload(), SIMPLE_SCHEMA);
    List<SchemaViolation> violations = (List<SchemaViolation>) event.getMessage().getPayload().getValue();
    assertViolations(violations);
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
        assertViolations(problems);

        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("Error validation failed");
      }
    });
  }

  private void assertViolations(List<SchemaViolation> problems) {
    assertThat(problems, hasSize(1));
    assertThat(problems.get(0).getDescription(),
               equalTo(
                       "cvc-complex-type.2.4.a: Invalid content was found starting with element 'fail'. One of '{used}' is expected."));
  }

  private Event validate(InputStream payload, String... schemas) throws Exception {
    return validate("validateSchema", payload, schemas);
  }

  private Event validate(String flowName, InputStream payload, String... schemas) throws Exception {
    return flowRunner(flowName)
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

  private InputStream getValidPayload() {
    return getResourceAsStream(VALID_XML_FILE);
  }

  private InputStream getInvalidPayload() {
    return getResourceAsStream(INVALID_XML_FILE);
  }

  private InputStream getResourceAsStream(String path) {
    return getClass().getClassLoader().getResourceAsStream(path);
  }
}
