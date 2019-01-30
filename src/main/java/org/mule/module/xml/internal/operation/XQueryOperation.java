/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.internal.operation;

import static java.lang.String.format;
import static javax.xml.xquery.XQItemType.XQBASETYPE_BOOLEAN;
import static javax.xml.xquery.XQItemType.XQBASETYPE_BYTE;
import static javax.xml.xquery.XQItemType.XQBASETYPE_DOUBLE;
import static javax.xml.xquery.XQItemType.XQBASETYPE_FLOAT;
import static javax.xml.xquery.XQItemType.XQBASETYPE_INT;
import static javax.xml.xquery.XQItemType.XQBASETYPE_LONG;
import static javax.xml.xquery.XQItemType.XQBASETYPE_SHORT;
import static javax.xml.xquery.XQItemType.XQBASETYPE_STRING;
import static org.mule.module.xml.internal.util.XMLUtils.toDOMNode;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.runtime.api.meta.model.operation.ExecutionType.CPU_INTENSIVE;
import org.mule.module.xml.internal.XmlModule;
import org.mule.module.xml.internal.error.StandardXmlErrorTypeProvider;
import org.mule.module.xml.internal.error.TransformationException;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.execution.Execution;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.Text;

import com.saxonica.xqj.SaxonXQDataSource;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQItemType;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

import net.sf.saxon.Configuration;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operation to transform an XML document using XQuery
 *
 * @since 1.0
 */
public class XQueryOperation extends PooledTransformerOperation<String, XQPreparedExpression> {

  private static final String SOURCE_DOCUMENT_NAMESPACE = "document";
  private static final Logger LOGGER = LoggerFactory.getLogger(XQueryOperation.class);

  private XQConnection connection;
  private XQItemType stringType;
  private ThreadLocal<Map<String, Object>> currentContext = new ThreadLocal<>();

  @Override
  protected void doInitialise() throws InitialisationException {
    XQDataSource ds = new SaxonXQDataSource(new Configuration());
    try {
      connection = ds.getConnection();
      stringType = connection.createAtomicType(XQBASETYPE_STRING);
    } catch (Exception e) {
      throw new InitialisationException(createStaticMessage("Could not initialise XQuery DataSource"), e, this);
    }
  }

  /**
   * Uses XQuery to transform the input content. You can set transformation context properties which will be made available on the
   * XQuery execution
   *
   * @param content           the XML content to transform
   * @param xquery            The XQuery script definition
   * @param contextProperties Properties that wil be made available to the transform context.
   * @param config            the config object
   * @return the transformed document
   */
  @Execution(CPU_INTENSIVE)
  @Throws(StandardXmlErrorTypeProvider.class)
  public List<String> xqueryTransform(@Content(primary = true) InputStream content,
                                      @Text String xquery,
                                      @Optional @Content @NullSafe Map<String, Object> contextProperties,
                                      @Config XmlModule config) {

    return withTransformer(xquery, transformer -> {
      bindParameters(transformer, contextProperties);
      currentContext.set(contextProperties);
      transformer.bindNode(new QName(SOURCE_DOCUMENT_NAMESPACE), toDOMNode(content, documentBuilderFactory),
                           connection.createNodeType());
      XQResultSequence result = transformer.executeQuery();

      List<String> results = new LinkedList<>();

      Properties avoidNewLinesInXQItems = new Properties();
      avoidNewLinesInXQItems.setProperty("indent", "no");

      while (result.next()) {
        XQItem item = result.getItem();
        results.add(item.getItemAsString(avoidNewLinesInXQItems));
      }

      return results;
    });
  }

  private void unbindParameters(@Content @NullSafe Map<String, Object> contextProperties, XQPreparedExpression transformer) {
    contextProperties.forEach((key, value) -> {
      try {
        transformer.bindAtomicValue(new QName(key), "", stringType);
      } catch (XQException e) {
        throw new RuntimeException(e);
      }
    });
  }

  private void bindParameters(XQPreparedExpression transformer, Map<String, Object> contextProperties) {
    contextProperties.forEach((key, value) -> {
      QName paramKey = new QName(key);

      try {
        if (value instanceof InputStream) {
          bindStringParameter(transformer, IOUtils.toString((InputStream) value), paramKey);
        } else if (value instanceof String) {
          bindStringParameter(transformer, value, paramKey);
        } else if (value instanceof Boolean) {
          transformer
              .bindBoolean(paramKey, ((Boolean) value).booleanValue(),
                           connection.createAtomicType(XQBASETYPE_BOOLEAN));
        } else if (value instanceof Byte) {
          transformer.bindByte(paramKey, ((Byte) value).byteValue(), connection.createAtomicType(XQBASETYPE_BYTE));
        } else if (value instanceof Short) {
          transformer.bindShort(paramKey, ((Short) value).shortValue(), connection.createAtomicType(XQBASETYPE_SHORT));
        } else if (value instanceof Integer) {
          transformer.bindInt(paramKey, ((Integer) value).intValue(), connection.createAtomicType(XQBASETYPE_INT));
        } else if (value instanceof Long) {
          transformer.bindLong(paramKey, ((Long) value).longValue(), connection.createAtomicType(XQBASETYPE_LONG));
        } else if (value instanceof Float) {
          transformer.bindFloat(paramKey, ((Float) value).floatValue(), connection.createAtomicType(XQBASETYPE_FLOAT));
        } else if (value instanceof Double) {
          transformer
              .bindDouble(paramKey, ((Double) value).doubleValue(), connection.createAtomicType(XQBASETYPE_DOUBLE));
        } else {
          throw new TransformationException(
                                            format("Cannot bind value for key '%s' because type '%s' is not supported", key,
                                                   value.getClass().getName()));
        }
      } catch (Exception e) {
        throw new TransformationException("Failed to bind parameters for XQuery transformation. " + e.getMessage(), e);
      }
    });
  }

  private void bindStringParameter(XQPreparedExpression transformer, Object value, QName paramKey) throws XQException {
    transformer.bindAtomicValue(paramKey, value.toString(), connection.createAtomicType(XQBASETYPE_STRING));
  }

  @Override
  protected BasePooledObjectFactory<XQPreparedExpression> createPooledObjectFactory(String xquery) {
    return new BasePooledObjectFactory<XQPreparedExpression>() {

      @Override
      public XQPreparedExpression create() throws Exception {
        return connection.prepareExpression(xquery);
      }

      @Override
      public void destroyObject(PooledObject<XQPreparedExpression> p) throws Exception {
        p.getObject().close();
      }

      @Override
      public boolean validateObject(PooledObject<XQPreparedExpression> p) {
        try {
          Map<String, Object> contextProperties = currentContext.get();
          unbindParameters(contextProperties, p.getObject());
          return true;
        } catch (Exception e) {
          if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Found exception unbinding parameters. Transformer will be dropped", e);
          }
          return false;
        } finally {
          currentContext.remove();
        }
      }

      @Override
      public PooledObject<XQPreparedExpression> wrap(XQPreparedExpression obj) {
        return new DefaultPooledObject<>(obj);
      }
    };
  }

  @Override
  protected boolean testOnReturn() {
    return true;
  }
}
