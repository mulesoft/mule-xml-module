/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.internal.operation;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.Objects.hash;
import static java.util.stream.Collectors.toCollection;
import static org.mule.module.xml.internal.util.XMLUtils.toDOMNode;
import static org.mule.module.xml.internal.util.SchemaValidationUtils.checkSchemaInput;
import static org.mule.runtime.api.meta.model.display.PathModel.Type.FILE;
import static org.mule.runtime.api.meta.model.operation.ExecutionType.CPU_INTENSIVE;
import static org.mule.runtime.core.api.util.IOUtils.getResourceAsUrl;
import static org.mule.runtime.core.api.util.xmlsecurity.XMLSecureFactories.createWithConfig;
import org.mule.module.xml.api.EntityExpansion;
import org.mule.module.xml.api.SchemaLanguage;
import org.mule.module.xml.api.SchemaContent;
import org.mule.module.xml.api.SchemaValidationException;
import org.mule.module.xml.api.SchemaViolation;
import org.mule.module.xml.internal.XmlModule;
import org.mule.module.xml.internal.error.InvalidInputXmlException;
import org.mule.module.xml.internal.error.InvalidSchemaException;
import org.mule.module.xml.internal.error.SchemaValidatorErrorTypeProvider;
import org.mule.module.xml.internal.error.TransformationException;
import org.mule.module.xml.internal.util.MuleResourceResolver;
import org.mule.runtime.core.api.util.StringUtils;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.execution.Execution;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.Path;
import org.mule.runtime.extension.api.annotation.param.stereotype.Validator;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Operation to validate an XML document against a schema
 *
 * @since 1.0
 */
public class SchemaValidatorOperation
    extends PooledTransformerOperation<SchemaValidatorOperation.SchemaKey, javax.xml.validation.Validator> {

  private LSResourceResolver resourceResolver = new MuleResourceResolver();

  /**
   * Validates that the input content is compliant with a given schema. This operation supports referencing many schemas
   * (using comma as a separator) which include each other.
   *
   * @param schemas The path to the schema file. You can specify multiple schema locations by using a comma separator
   * @param schemaLanguage The schema language to use.
   * @param content the XML content to validate
   * @param config the config
   */
  @Validator
  @Execution(CPU_INTENSIVE)
  @Throws(SchemaValidatorErrorTypeProvider.class)
  public void validateSchema(@Optional @Path(type = FILE, acceptedFileExtensions = "xsd") String schemas,
                             @Optional @NullSafe List<SchemaContent> schemaContents,
                             @Optional(defaultValue = "W3C") SchemaLanguage schemaLanguage,
                             @Content(primary = true) InputStream content,
                             @Config XmlModule config) {
    checkSchemaInput(schemas, schemaContents);
    withTransformer(new SchemaKey(schemas, schemaContents, schemaLanguage.getLanguageUri(), expandEntities), validator -> {

      // set again since the reset() method may nullify this
      validator.setResourceResolver(resourceResolver);

      List<SchemaViolation> errors = new LinkedList<>();
      List<SchemaViolation> fatalErrors = new LinkedList<>();

      validator.setErrorHandler(new ErrorHandler() {

        @Override
        public void warning(SAXParseException exception) {}

        @Override
        public void error(SAXParseException exception) {
          trackError(exception);
        }

        @Override
        public void fatalError(SAXParseException exception) {
          fatalErrors.add(new SchemaViolation(exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage()));
          trackError(exception);
        }

        private void trackError(SAXParseException exception) {
          errors.add(new SchemaViolation(exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage()));
        }
      });

      try {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setFeature("http://xml.org/sax/features/external-general-entities", expandEntities.isAcceptExternalEntities());
        spf.setFeature("http://xml.org/sax/features/external-parameter-entities", expandEntities.isAcceptExternalEntities());
        spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", !expandEntities.isExpandInternalEntities());
        spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",
                       expandEntities.isExpandInternalEntities());
        validator.validate(new SAXSource(spf.newSAXParser().getXMLReader(), new InputSource(content)));

      } catch (SAXParseException e) {

        if (!fatalErrors.isEmpty()) {
          SchemaValidationException schemaValidationException =
              new SchemaValidationException("Input XML was not compliant with the schema. Check this error's Mule message for the "
                  + "list of problems (e.g: #[error.errorMessage.payload[0].description)", fatalErrors);
          throw new InvalidInputXmlException("Cannot parse input XML because it is invalid.",
                                             schemaValidationException);
        }
        throw new TransformationException("Failed to validate schema. " + e.getMessage(), e);
      } catch (IOException e) {
        throw new InvalidInputXmlException("Could not validate schema because the input was not valid XML. " + e.getMessage(), e);
      }

      if (!errors.isEmpty()) {
        throw new SchemaValidationException("Input XML was not compliant with the schema. Check this error's Mule message for the "
            + "list of problems (e.g: #[error.errorMessage.payload[0].description)", errors);
      }
      return null;
    });
  }

  @Override
  protected BasePooledObjectFactory<javax.xml.validation.Validator> createPooledObjectFactory(SchemaKey key) {
    return new BasePooledObjectFactory<javax.xml.validation.Validator>() {

      @Override
      public javax.xml.validation.Validator create() throws ParserConfigurationException, SAXException, IOException {
        Source[] schemas = null;
        if (key.schemaContents.isEmpty()) {
          schemas = loadSchemas(key.schemas);
        } else {
          schemas = loadSchemasFromSchemaContents(key.schemaContents);
        }

        SchemaFactory schemaFactory = createWithConfig(key.expandEntities.isAcceptExternalEntities(),
                                                       key.expandEntities.isExpandInternalEntities())
                                                           .getSchemaFactory(key.schemaLanguage);

        synchronized (schemaFactory) {
          schemaFactory.setResourceResolver(resourceResolver);

          Schema schema;
          try {
            schema = schemaFactory.newSchema(schemas);
          } catch (SAXException e) {
            throw new InvalidSchemaException("The supplied schemas were not valid. " + e.getMessage(), e);
          }

          return schema.newValidator();
        }
      }

      @Override
      public void passivateObject(PooledObject<javax.xml.validation.Validator> p) {
        p.getObject().setErrorHandler(null);
        p.getObject().reset();
      }

      @Override
      public PooledObject<javax.xml.validation.Validator> wrap(javax.xml.validation.Validator validator) {
        return new DefaultPooledObject<>(validator);
      }
    };
  }

  private Source[] loadSchemas(Set<String> paths) {
    Source[] schemas = new Source[paths.size()];

    int i = 0;
    for (String path : paths) {
      try {
        final URL resourceAsUrl = getResourceAsUrl(path, getClass());
        if (resourceAsUrl == null) {
          throw new InvalidSchemaException(format("Schema '%s' could not be found", path));
        }
        schemas[i++] = new DOMSource(toDOMNode(resourceAsUrl.openStream(), documentBuilderFactory), resourceAsUrl.toString());
      } catch (Exception e) {
        throw new InvalidSchemaException(format("Failed to load schema '%s'. %s", path, e.getMessage()), e);
      }
    }

    return schemas;
  }

  private Source[] loadSchemasFromSchemaContents(List<SchemaContent> schemaContents) {
    if (schemaContents == null || schemaContents.isEmpty()) {
      return new Source[0];
    }
    String[] schemaArray = convertSchemaText(schemaContents);
    Source[] schemas = new Source[schemaArray.length];

    int i = 0;
    for (String schema : schemaArray) {
      try {
        schemas[i++] = new StreamSource(new StringReader(schema));
      } catch (Exception e) {
        throw new InvalidSchemaException(format("Failed to load schema '%s'. %s", schema, e.getMessage()), e);
      }
    }

    return schemas;
  }

  private String[] convertSchemaText(List<SchemaContent> schemaContents) {
    String[] schemas = null;

    if (schemaContents.isEmpty()) {
      schemas = new String[1];
      schemas[0] = "";
      return schemas;
    }

    schemas = new String[schemaContents.size()];

    for (int i = 0; i < schemaContents.size(); i++) {
      schemas[i] = schemaContents.get(i).getSchemaText();
    }

    return schemas;
  }

  class SchemaKey {

    private final Set<String> schemas;
    private final String schemaLanguage;
    private final EntityExpansion expandEntities;
    private final List<SchemaContent> schemaContents;

    public SchemaKey(String schemas, String schemaLanguage, EntityExpansion expandEntities) {
      this.schemas = parseSchemas(schemas);
      this.schemaLanguage = schemaLanguage;
      this.expandEntities = expandEntities;
      this.schemaContents = null;
    }

    public SchemaKey(String schemas, List<SchemaContent> schemaContents, String schemaLanguage,
                     EntityExpansion expandEntities) {
      this.schemas = parseSchemas(schemas);
      this.schemaContents = schemaContents;
      this.schemaLanguage = schemaLanguage;
      this.expandEntities = expandEntities;
    }

    private Set<String> parseSchemas(String schemas) {
      if (StringUtils.isBlank(schemas)) {
        return emptySet();
      }

      return Stream.of(schemas.split(",")).map(String::trim).collect(toCollection(HashSet::new));
    }

    @Override
    public boolean equals(Object obj) {
      SchemaKey other = (SchemaKey) obj;
      if (other == null) {
        return false;
      }
      return Objects.equals(schemas, other.schemas)
          && Objects.equals(schemaContents, other.schemaContents)
          && Objects.equals(schemaLanguage, other.schemaLanguage)
          && expandEntities == other.expandEntities;
    }

    @Override
    public int hashCode() {
      return hash(schemas, schemaContents, schemaLanguage, expandEntities);
    }
  }
}
