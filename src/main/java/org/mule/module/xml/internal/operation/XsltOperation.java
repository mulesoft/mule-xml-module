/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.internal.operation;

import static java.lang.String.format;
import static org.mule.module.xml.internal.util.XMLUtils.toDOMNode;
import static org.mule.runtime.api.meta.model.operation.ExecutionType.CPU_INTENSIVE;
import static org.mule.runtime.extension.api.annotation.param.MediaType.ANY;
import org.mule.module.xml.internal.XmlModule;
import org.mule.module.xml.internal.error.NullContextPropertyException;
import org.mule.module.xml.internal.error.StandardXmlErrorTypeProvider;
import org.mule.module.xml.internal.error.TransformationException;
import org.mule.module.xml.internal.error.TransformerErrorListener;
import org.mule.module.xml.internal.util.FileSchemeCorrectionOutputUriResolver;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.execution.Execution;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.DefaultEncoding;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.Text;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Controller;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.w3c.dom.Node;

/**
 * Operation to transform an XML document using XSLT
 *
 * @since 1.0
 */
public class XsltOperation extends PooledTransformerOperation<String, XsltTransformer> {

  /**
   * Uses XSLT to transform the input content. You can set transformation context properties which will be made available on the
   * stylesheet.
   *
   * @param content           the XML content to transform
   * @param xslt              the XSLT script definition
   * @param contextProperties Properties that wil be made available to the transform context.
   * @param outputEncoding    the output encoding. Defaults to the application's default
   * @param config            the config object
   * @return the transformed document
   */
  @Execution(CPU_INTENSIVE)
  @MediaType(value = ANY, strict = false)
  @Throws(StandardXmlErrorTypeProvider.class)
  public String xsltTransform(@Content(primary = true) InputStream content,
                              @Text String xslt,
                              @Optional @Content @NullSafe Map<String, Object> contextProperties,
                              @DefaultEncoding String outputEncoding,
                              @Config XmlModule config) {

    Node node = toDOMNode(content, documentBuilderFactory, entityResolver);
    TransformerErrorListener errorListener = new TransformerErrorListener();

    return withTransformer(xslt, transformer -> {
      bindParameters(transformer, contextProperties);

      // Set URI scheme correction resolver
      transformer.getUnderlyingController().setOutputURIResolver(new FileSchemeCorrectionOutputUriResolver());

      transformer.setErrorListener(errorListener);
      StringWriter writer = new StringWriter();
      Serializer out = (Serializer) transformer.getDestination();
      out.setOutputWriter(writer);
      out.setOutputProperty(Serializer.Property.ENCODING, outputEncoding);

      transformer.setSource(new DOMSource(node));
      transformer.transform();

      if (errorListener.getException().isPresent()) {
        throw errorListener.getException().get();
      }

      return writer.toString();
    });
  }

  private void bindParameters(XsltTransformer transformer, Map<String, Object> contextProperties) {
    contextProperties.forEach((key, value) -> {
      if (value == null) {
        throw new NullContextPropertyException(
                                               format("Context property '%s' resolved to a null value. Null values are not allowed here",
                                                      key));
      }

      XdmValue xdmValue;
      if (value instanceof String) {
        xdmValue = new XdmAtomicValue((String) value);
      } else if (value instanceof InputStream) {
        xdmValue = new XdmAtomicValue(IOUtils.toString((InputStream) value));
      } else if (value instanceof Integer) {
        xdmValue = new XdmAtomicValue((Long) value);
      } else if (value instanceof Boolean) {
        xdmValue = new XdmAtomicValue((Boolean) value);
      } else if (value instanceof Long) {
        xdmValue = new XdmAtomicValue((Long) value);
      } else if (value instanceof Double) {
        xdmValue = new XdmAtomicValue((Double) value);
      } else if (value instanceof Short) {
        xdmValue = new XdmAtomicValue((Short) value);
      } else if (value instanceof Byte) {
        xdmValue = new XdmAtomicValue((Byte) value);
      } else if (value instanceof Float) {
        xdmValue = new XdmAtomicValue((Float) value);
      } else {
        throw new TransformationException(
                                          format("Cannot bind value for key '%s' because type '%s' is not supported", key,
                                                 value.getClass().getName()));
      }

      transformer.setParameter(new QName(key), xdmValue);
    });
  }

  @Override
  protected BasePooledObjectFactory createPooledObjectFactory(String xslt) {
    return new BasePooledObjectFactory<XsltTransformer>() {

      @Override
      public XsltTransformer create() throws Exception {
        Processor proc = new Processor(false);
        XsltCompiler comp = proc.newXsltCompiler();
        XsltExecutable exp = comp.compile(new StreamSource(new StringReader(xslt)));
        Serializer out = proc.newSerializer();
        XsltTransformer trans = exp.load();
        trans.setDestination(out);

        return trans;
      }

      @Override
      public PooledObject<XsltTransformer> wrap(XsltTransformer transformer) {
        return new DefaultPooledObject(transformer);
      }

      @Override
      public void passivateObject(PooledObject<XsltTransformer> p) throws Exception {
        XsltTransformer transformer = p.getObject();

        transformer.clearParameters();
        transformer.setSource(null);
        Controller controller = transformer.getUnderlyingController();
        if (controller != null) {
          controller.reset();
        }
        ((Serializer) transformer.getDestination()).setOutputWriter(null);
      }
    };
  }
}
