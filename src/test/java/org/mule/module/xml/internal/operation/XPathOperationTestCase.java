/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.internal.operation;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mule.module.xml.api.NamespaceDirectory;
import org.mule.module.xml.api.NamespaceMapping;

import java.util.Collection;

public class XPathOperationTestCase {

  private static final NamespaceMapping NAMESPACE = new NamespaceMapping("prefix", "uri");
  @Rule
  public MockitoRule rule = MockitoJUnit.rule();
  private XPathOperation operation;
  @Mock
  private NamespaceDirectory directory;

  @Before
  public void setUp() {
    operation = new XPathOperation();
  }

  @Test
  public void mergeWithNullNamespace() {
    when(directory.getNamespaces()).thenReturn(ImmutableList.of(NAMESPACE));

    final Collection<NamespaceMapping> namespaces = operation.mergeNamespaces(null, directory);

    assertThat(namespaces, containsInAnyOrder(NAMESPACE));
  }

  @Test
  public void mergeWithNullDirectory() {

    final Collection<NamespaceMapping> namespaces = operation.mergeNamespaces(ImmutableList.of(NAMESPACE), null);

    assertThat(namespaces, containsInAnyOrder(NAMESPACE));
  }

  @Test
  public void mergeWithNullDirectoryNamespaces() {
    when(directory.getNamespaces()).thenReturn(null);

    final Collection<NamespaceMapping> namespaces = operation.mergeNamespaces(ImmutableList.of(NAMESPACE), directory);

    assertThat(namespaces, containsInAnyOrder(NAMESPACE));
  }

  @Test
  public void mergeNamespacesWithDuplicates() {
    // Create separate instances to test equals.
    final NamespaceMapping otherNamespace = new NamespaceMapping("prefix", "uri");
    when(directory.getNamespaces()).thenReturn(ImmutableList.of(otherNamespace));

    final Collection<NamespaceMapping> namespaces = operation.mergeNamespaces(ImmutableList.of(NAMESPACE), directory);

    assertThat(namespaces, containsInAnyOrder(NAMESPACE));
  }

  @Test
  public void mergeNamespacesWithNoDuplicates() {
    final NamespaceMapping otherNamespace = new NamespaceMapping("foo", "bar");

    when(directory.getNamespaces()).thenReturn(ImmutableList.of(otherNamespace));

    final Collection<NamespaceMapping> namespaces = operation.mergeNamespaces(ImmutableList.of(NAMESPACE), directory);

    assertThat(namespaces, containsInAnyOrder(NAMESPACE, otherNamespace));
  }
}
