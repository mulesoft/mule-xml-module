/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.unit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static javax.xml.xpath.XPathConstants.NODESET;

import net.sf.saxon.xpath.XPathExpressionImpl;
import net.sf.saxon.xpath.XPathFactoryImpl;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import org.mockito.runners.MockitoJUnitRunner;
import org.mule.module.xml.api.NamespaceMapping;
import org.mule.module.xml.internal.error.TransformationException;
import org.mule.module.xml.internal.xpath.XPathEvaluator;
import org.w3c.dom.Node;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.Collection;

@RunWith(MockitoJUnitRunner.class)
public class XPathEvaluatorTestCase {

  private static XPathFactory xPathFactory;
  private static Collection<NamespaceMapping> namespaces;
  private static Node node;
  private static String expression;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @BeforeClass
  public static void init() {
    xPathFactory = mock(XPathFactoryImpl.class);
    namespaces = new ArrayList<>();
    node = mock(Node.class);
    expression = "/*";
  }

  @Test
  public void handleNullPointerException() throws XPathExpressionException {
    expectedException.expect(TransformationException.class);
    expectedException.expectMessage("Failed to evaluate XPath expression");

    net.sf.saxon.xpath.XPathEvaluator xPath = mock(net.sf.saxon.xpath.XPathEvaluator.class);
    XPathExpressionImpl xPathExpressionNullPointerException = mock(XPathExpressionImpl.class);
    when(xPathFactory.newXPath()).thenReturn(xPath);
    when(xPath.compile(expression)).thenReturn(xPathExpressionNullPointerException);
    when(xPathExpressionNullPointerException.evaluate(node, NODESET)).thenThrow(new NullPointerException());

    evaluate();
  }

  @Test
  public void handleUnexpectedXPathExpressionException() throws XPathExpressionException {
    expectedException.expect(TransformationException.class);
    expectedException.expectMessage("Failed to evaluate XPath expression");

    net.sf.saxon.xpath.XPathEvaluator xPath = mock(net.sf.saxon.xpath.XPathEvaluator.class);
    XPathExpressionImpl xPathExpressionUnexpectedXPathExpressionException = mock(XPathExpressionImpl.class);
    when(xPathFactory.newXPath()).thenReturn(xPath);
    when(xPath.compile(expression)).thenReturn(xPathExpressionUnexpectedXPathExpressionException);
    when(xPathExpressionUnexpectedXPathExpressionException.evaluate(node, NODESET))
        .thenThrow(new XPathExpressionException("Sarasa"));

    evaluate();

  }

  @Test
  public void handleExpectedXPathExpressionException() throws XPathExpressionException {
    expectedException.expect(TransformationException.class);
    expectedException.expectMessage("Unsupported XPath expression");

    net.sf.saxon.xpath.XPathEvaluator xPath = mock(net.sf.saxon.xpath.XPathEvaluator.class);
    XPathExpressionImpl xPathExpressionExpectedXPathExpressionException = mock(XPathExpressionImpl.class);
    when(xPathFactory.newXPath()).thenReturn(xPath);
    when(xPath.compile(expression)).thenReturn(xPathExpressionExpectedXPathExpressionException);
    when(xPathExpressionExpectedXPathExpressionException.evaluate(node, NODESET))
        .thenThrow(new XPathExpressionException("Cannot convert XPath value to Java object: required class is org.w3c.dom.NodeList"));

    evaluate();
  }

  private void evaluate() {
    XPathEvaluator xPathEvaluator = new XPathEvaluator(expression, xPathFactory, namespaces);
    xPathEvaluator.evaluate(node, null);
  }
}
