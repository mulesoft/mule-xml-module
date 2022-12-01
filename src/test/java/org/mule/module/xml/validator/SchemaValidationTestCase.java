/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.validator;

import static java.util.stream.Collectors.joining;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mule.functional.api.exception.ExpectedError.none;
import static org.mule.module.xml.api.XmlError.SCHEMA_NOT_HONOURED;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Rule;
import org.junit.Test;
import org.mule.functional.api.exception.ExpectedError;
import org.mule.module.xml.XmlTestCase;
import org.mule.module.xml.api.SchemaContent;
import org.mule.module.xml.api.SchemaValidationException;
import org.mule.module.xml.api.SchemaViolation;
import org.mule.module.xml.api.XmlError;
import org.mule.runtime.api.event.Event;
import org.mule.runtime.core.api.event.CoreEvent;

import com.google.common.io.CharStreams;

public class SchemaValidationTestCase extends XmlTestCase {

  private static final String INVALID_INPUT_XML = "Cannot parse input XML because it is invalid.";
  private static final String INVALID_INPUT_XML_STRING = "must be terminated by the matching end-tag";
  private static final String SCHEMA_INPUT_ERROR_STRING =
      "Either Schema or Schema Content must be provided, and you cannot provide both.";
  private static final String DOCTYPE_IS_DISALLOWED_STRING = "DOCTYPE is disallowed";
  private static final String SIMPLE_SCHEMA = "validation/schema1.xsd";
  private static final String INCLUDE_SCHEMA = "validation/schema-with-include.xsd";

  private static final String VALID_XML_FILE = "validation/validation1.xml";
  private static final String INVALID_XML_FILE = "validation/validation2.xml";
  private static final String INVALID_INPUT_XML_FILE = "validation/validation4.xml";

  private static final String EXTERNAL_ENTITY_XML_FILE = "validation/externalEntityValidation.xml";

  private static final String BILLION_LAUGHS_XML_FILE = "validation/billionLaughs.xml";

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
  public void invalidWithInclude() throws Exception {
    expectedError.expectErrorType(ERROR_NAMESPACE, SCHEMA_NOT_HONOURED.name());
    validate("validateSchemaWithReferences",
             getResourceAsStream("validation/include/xml-with-referencing-schema.xml"),
             "validation/include/schema-with-references.xsd");
  }

  @Test
  public void extractErrorsUsingExpressions() throws Exception {
    Event event = validate("extractErrorsFromException", getInvalidPayload(), SIMPLE_SCHEMA);
    List<SchemaViolation> violations = (List<SchemaViolation>) event.getMessage().getPayload().getValue();
    assertViolations(violations);
  }

  @Test
  public void extractErrorsUsingExpressionsWithSchemaContent() throws Exception {
    Event event = validateFromSchemaContent("extractErrorsFromExceptionWithSchemaContent", getInvalidPayload(), SIMPLE_SCHEMA);
    List<SchemaViolation> violations = (List<SchemaViolation>) event.getMessage().getPayload().getValue();
    assertViolations(violations);
  }

  @Test
  public void validWithImport() throws Exception {
    validate("validateSchemaWithReferences", getValidPayload(), INCLUDE_SCHEMA);
  }

  @Test
  public void invalidWithImport() throws Exception {
    expectValidationFailure();
    validate("validateSchemaWithReferences", getInvalidPayload(), INCLUDE_SCHEMA);
  }

  @Test
  public void invalidInput() throws Exception {
    expectedBehaviourOnInvalidInput(INVALID_INPUT_XML_STRING);
    validate(getInvalidInputPayload(), SIMPLE_SCHEMA);
  }

  @Test
  public void externalEntityValidation() throws Exception {
    expectedBehaviourOnInvalidInput(DOCTYPE_IS_DISALLOWED_STRING);
    validate(getExternalEntityPayload(), SIMPLE_SCHEMA);
  }

  @Test
  public void billionLaughs() throws Exception {
    expectedBehaviourOnInvalidInput(DOCTYPE_IS_DISALLOWED_STRING);
    validate(getBillionLaughsPayload(), SIMPLE_SCHEMA);
  }

  @Test
  public void externalEntityValidationWithExpandEntitiesALL() throws Exception {
    validate("validateSchemaWithReferences", getExternalEntityPayload(), SIMPLE_SCHEMA);
  }

  @Test
  public void validSchemaWithSchemaContent() throws Exception {
    validateFromSchemaContent("validateSchemaWithSchemaContent", getValidPayload(), SIMPLE_SCHEMA);
  }

  @Test
  public void invalidSchemaWithSchemaContent() throws Exception {
    expectValidationFailure();
    validateFromSchemaContent("validateSchemaWithSchemaContent", getInvalidPayload(), SIMPLE_SCHEMA);
  }

  @Test
  public void schemaInputErrorWithoutSchemaContentAndSchema() throws Exception {
    expectedBehaviourOnSchemaInputError(SCHEMA_INPUT_ERROR_STRING);
    validateFromSchemaContent("validateSchemaWithSchemaContent", getValidPayload(), null);
  }

  @Test
  public void schemaInputErrorWithSchemaContentAndSchema() throws Exception {
    expectedBehaviourOnSchemaInputError(SCHEMA_INPUT_ERROR_STRING);
    validateSchemaInputWithBothSchemaAttributes("validateSchemaWithSchemaContent", getValidPayload(), SIMPLE_SCHEMA);
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
    assertThat(problems.get(0).getColumnNumber(), not(equalTo(-1)));
    assertThat(problems.get(0).getLineNumber(), not(equalTo(-1)));
    assertThat(problems.get(0).getDescription(),
               equalTo(
                       "cvc-complex-type.2.4.a: Invalid content was found starting with element 'fail'. One of '{used}' is expected."));
  }

  private void expectedBehaviourOnInvalidInput(String problemDescription) {
    expectedError.expectErrorType(ERROR_NAMESPACE, XmlError.INVALID_INPUT_XML.name());
    expectedError.expectEvent(new BaseMatcher<CoreEvent>() {

      @Override
      public boolean matches(Object item) {
        CoreEvent event = (CoreEvent) item;
        assertThat(event.getError().get().getDescription(), containsString(INVALID_INPUT_XML));
        SchemaValidationException schemaValidationException =
            (SchemaValidationException) event.getError().get().getCause().getCause();
        List<SchemaViolation> problems =
            (List<SchemaViolation>) schemaValidationException.getErrorMessage().getPayload().getValue();
        assertThat(problems.get(0).getColumnNumber(), not(equalTo(-1)));
        assertThat(problems.get(0).getLineNumber(), not(equalTo(-1)));
        assertThat(problems.get(0).getDescription(), containsString(problemDescription));
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("Error validation failed");
      }
    });
  }

  private void expectedBehaviourOnSchemaInputError(String problemDescription) {
    expectedError.expectErrorType(ERROR_NAMESPACE, XmlError.SCHEMA_INPUT_ERROR.name());
    expectedError.expectEvent(new BaseMatcher<CoreEvent>() {

      @Override
      public boolean matches(Object item) {
        CoreEvent event = (CoreEvent) item;
        assertThat(event.getError().get().getDescription(), containsString(SCHEMA_INPUT_ERROR_STRING));
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("Error validation failed");
      }
    });
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

  private Event validateFromSchemaContent(String flowName, InputStream payload, String... schemas)
      throws Exception {
    List<SchemaContent> schemaContents = parseSchemaContent(schemas);
    return flowRunner(flowName)
        .withPayload(payload)
        .withVariable("schemaContents", schemaContents)
        .run();
  }

  private Event validateSchemaInputWithBothSchemaAttributes(String flowName, InputStream payload, String... schemas)
      throws Exception {
    List<SchemaContent> schemaContents = parseSchemaContent(schemas);
    return flowRunner(flowName)
        .withPayload(payload)
        .withVariable("schemaContents", schemaContents)
        .withVariable("schemas", SIMPLE_SCHEMA)
        .run();
  }

  private List<SchemaContent> parseSchemaContent(String... schemas) {
    if (schemas == null || schemas.length == 0) {
      return new ArrayList<SchemaContent>();
    }

    InputStream inputStream = getResourceAsStream(schemas[0]);
    List<SchemaContent> schemaContents = new ArrayList<>();

    String text = null;
    try (Reader reader = new InputStreamReader(inputStream)) {
      text = CharStreams.toString(reader);
    } catch (IOException e) {
      e.printStackTrace();
    }

    SchemaContent schemaContent = new SchemaContent(text, "schema1.xsd");
    schemaContents.add(schemaContent);

    return schemaContents;
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

  private InputStream getInvalidInputPayload() {
    return getResourceAsStream(INVALID_INPUT_XML_FILE);
  }

  private InputStream getExternalEntityPayload() {
    return getResourceAsStream(EXTERNAL_ENTITY_XML_FILE);
  }

  private InputStream getBillionLaughsPayload() {
    return getResourceAsStream(BILLION_LAUGHS_XML_FILE);
  }

  private InputStream getResourceAsStream(String path) {
    return getClass().getClassLoader().getResourceAsStream(path);
  }
}
