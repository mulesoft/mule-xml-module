/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.internal.operation;

import static org.mule.module.xml.internal.util.XMLUtils.toDOMNode;
import static org.mule.runtime.api.meta.model.operation.ExecutionType.CPU_INTENSIVE;
import org.mule.module.xml.api.NamespaceMapping;
import org.mule.module.xml.internal.XmlModule;
import org.mule.module.xml.internal.error.XPathErrorTypeProvider;
import org.mule.module.xml.internal.xpath.XPathEvaluator;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.execution.Execution;
import org.mule.runtime.extension.api.annotation.param.Config;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.xpath.XPathFactory;

import net.sf.saxon.xpath.XPathFactoryImpl;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * Operation for extracting values from an XML document using XPath
 *
 * @since 1.0
 */
public class XPathOperation extends PooledTransformerOperation<XPathOperation.XPathKey, XPathEvaluator>
    implements Initialisable {

  private final XPathFactory xpathFactory = new XPathFactoryImpl();

  public XPathOperation() {}

  public XPathOperation(SecuritySettings securitySettings) {
    this.securitySettings = securitySettings;
  }

  /**
   * Evaluates an XPath expression the input content and returns the result.
   * <p>
   * Because XPath expressions can match any number of individual elements, this operation
   * returns a List of Strings. If no element matched the expression, an empty list will be
   * returned.
   * <p>
   * XPath expressions are also namespace aware, which is why this operation allows setting namespace
   * mappings. These mappings will be merged with those optionally defined in the config, meaning that the
   * evaluation will combine both sets of namespace mappings.
   *
   * @param content           the XML content on which the XPath is evaluated
   * @param xpath             the XPath script
   * @param contextProperties Properties that wil be made available to the transform context.
   * @param namespaces        namespace mappings that will be used in this evaluation. They will be combined with the ones in the config element
   * @param config            the config
   * @return a List of Strings with all the matching elements
   */
  @Execution(CPU_INTENSIVE)
  @Throws(XPathErrorTypeProvider.class)
  public List<String> xpathExtract(@Content(primary = true) InputStream content,
                                   String xpath,
                                   @Optional @Content @NullSafe Map<String, Object> contextProperties,
                                   @Optional @NullSafe List<NamespaceMapping> namespaces,
                                   @Config XmlModule config) {
    return withTransformer(new XPathKey(xpath, mergeNamespaces(namespaces, config)),
                           evaluator -> evaluator.evaluate(toDOMNode(content, documentBuilderFactory), contextProperties));
  }

  private Collection<NamespaceMapping> mergeNamespaces(List<NamespaceMapping> namespaces, XmlModule config) {
    Collection<NamespaceMapping> merge = new HashSet<>();
    if (namespaces != null) {
      merge.addAll(namespaces);
    }

    if (config.getNamespaces() != null) {
      merge.addAll(config.getNamespaces());
    }

    return merge;
  }

  @Override
  protected BasePooledObjectFactory<XPathEvaluator> createPooledObjectFactory(XPathKey key) {
    return new BasePooledObjectFactory<XPathEvaluator>() {

      @Override
      public XPathEvaluator create() throws Exception {
        return new XPathEvaluator(key.xpath, xpathFactory, key.namespaces);
      }

      @Override
      public void passivateObject(PooledObject<XPathEvaluator> p) throws Exception {
        p.getObject().reset();
      }

      @Override
      public PooledObject<XPathEvaluator> wrap(XPathEvaluator evaluator) {
        return new DefaultPooledObject(evaluator);
      }
    };
  }

  class XPathKey {

    private final String xpath;
    private final Collection<NamespaceMapping> namespaces;

    public XPathKey(String xpath, Collection<NamespaceMapping> namespaces) {
      this.xpath = xpath;
      this.namespaces = namespaces;
    }

    @Override
    public boolean equals(Object obj) {
      XPathKey other = (XPathKey) obj;
      return Objects.equals(xpath, other.xpath) && Objects.equals(namespaces, other.namespaces);
    }

    @Override
    public int hashCode() {
      return Objects.hash(xpath, namespaces);
    }
  }
}
