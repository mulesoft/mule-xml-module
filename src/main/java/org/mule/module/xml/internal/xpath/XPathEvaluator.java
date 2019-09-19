/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.internal.xpath;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static javax.xml.transform.OutputKeys.INDENT;
import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;
import static javax.xml.xpath.XPathConstants.NODESET;
import static org.w3c.dom.Node.ATTRIBUTE_NODE;

import org.mule.module.xml.api.NamespaceMapping;
import org.mule.module.xml.internal.error.InvalidXPathExpressionException;
import org.mule.module.xml.internal.error.TransformationException;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Evaluates XPath expressions using the JAXP API (JSR-206).
 * <p>
 * Each instance accepts one XPath expression and keeps a compiled version of it for better performance.
 * <p>
 * This evaluator is reusable, as long as the {@link #reset()} method is invoked in between evaluations. It is not thread-safe
 * though.
 * <p>
 * It also implements the {@link XPathVariableResolver} interface in order to bind {@link #contextProperties} to variables defined
 * in the XPath script.
 *
 * @since 1.0.
 */
public class XPathEvaluator implements XPathVariableResolver {

  private static final boolean DEFAULT_KEEP_TRAILING_NEWLINES = false;
  private final XPathExpression xpathExpression;
  private Map<String, Object> contextProperties;
  private Transformer toString;
  private boolean keepTrailingNewlines;

  /**
   * Creates a new instance
   *
   * @param expression   the xpath expression
   * @param xpathFactory the {@link XPathFactory} used to compile the expression
   * @param namespaces   namespace mappings
   */
  public XPathEvaluator(String expression, XPathFactory xpathFactory, Collection<NamespaceMapping> namespaces) {
    XPath xpath = xpathFactory.newXPath();
    xpath.setXPathVariableResolver(this);
    xpath.setNamespaceContext(new XPathNamespaceContext(namespaces.stream()
        .collect(toMap(ns -> ns.getPrefix(), ns -> ns.getUri()))));
    try {
      xpathExpression = xpath.compile(expression);
    } catch (Exception e) {
      throw new InvalidXPathExpressionException("Could not compile xpath expression " + expression, e);
    }

    try {
      toString = TransformerFactory.newInstance().newTransformer();
    } catch (TransformerConfigurationException e) {
      e.printStackTrace();
    }

    toString.setOutputProperty(OMIT_XML_DECLARATION, "yes");
    toString.setOutputProperty(INDENT, "yes");

    keepTrailingNewlines = DEFAULT_KEEP_TRAILING_NEWLINES;

    reset();
  }

  /**
   * Evaluates the expression on the {@code input} node, using the given {@code contextProperties}.
   * <p>
   * After invoking this method, the consumer <b>MUST</b> invoke the {@link #reset()} method in order to reuse this evaluator.
   *
   * @param input             the base node of the evaluation
   * @param contextProperties context properties
   * @return a List of strings with the matching elements
   */
  public List<String> evaluate(Node input, Map<String, Object> contextProperties) {
    this.contextProperties = contextProperties;

    try {
      return toStringList((NodeList) xpathExpression.evaluate(input, NODESET));
    } catch (Exception e) {
      throw new TransformationException(
                                        format("Failed to evaluate XPath expression '%s'. %s", xpathExpression.toString(),
                                               e.getMessage()),
                                        e);
    }
  }

  private List<String> toStringList(NodeList nodeList) throws TransformerException {
    final int size = nodeList.getLength();
    List<String> strings = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      Node item = nodeList.item(i);
      if (item.getNodeType() == ATTRIBUTE_NODE) {
        strings.add(item.getTextContent());
      } else {
        StringWriter sw = new StringWriter();
        toString.transform(new DOMSource(item), new StreamResult(sw));
        // Since Saxon 9.9.1-1, the "indented" serializer options seems to add a new-line character
        // after each DOM Node. Could not found a property to avoid this behaviour.
        strings.add(cleanTrailingNewlineIfNecessary(sw.toString()));
      }
    }

    return strings;
  }

  private String cleanTrailingNewlineIfNecessary(String line) {
    if (!keepTrailingNewlines && line.endsWith("\n")) {
      line = line.substring(0, line.lastIndexOf('\n'));
    }
    return line;
  }

  /**
   * Resolves the given variable against the context properties passed on the {@link #evaluate(Node, Map)} method
   *
   * @param variableName the variable name
   * @return the variable value. Might be {@code null}
   */
  @Override
  public Object resolveVariable(QName variableName) {
    return contextProperties.get(variableName.getLocalPart());
  }


  /**
   * Invoke this method in between invocations of the {@link #evaluate(Node, Map)} method in order to reuse this instance
   */
  public void reset() {
    contextProperties = emptyMap();
  }

  public XPathEvaluator keepingTrailingNewlines(boolean shouldKeepTrailingNewlines) {
    keepTrailingNewlines = shouldKeepTrailingNewlines;
    return this;
  }
}
