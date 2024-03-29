/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.api;

import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static org.mule.runtime.api.meta.model.display.PathModel.Location.EXTERNAL;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Path;

import java.util.Objects;

/**
 * Maps a prefix to a namespace URI
 *
 * @since 1.0
 */
@Alias("namespace")
public class NamespaceMapping {

  /**
   * The prefix used in the XML document
   */
  @Parameter
  @Expression(NOT_SUPPORTED)
  private String prefix;

  /**
   * The namespace URI
   */
  @Parameter
  @Expression(NOT_SUPPORTED)
  @Path(acceptsUrls = true, location = EXTERNAL)
  private String uri;

  public NamespaceMapping() {}

  public NamespaceMapping(final String prefix, final String uri) {
    this.prefix = prefix;
    this.uri = uri;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getPrefix() {
    return prefix;
  }

  public String getUri() {
    return uri;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof NamespaceMapping)) {
      return false;
    }
    NamespaceMapping other = (NamespaceMapping) obj;
    return Objects.equals(prefix, other.prefix) && Objects.equals(uri, other.uri);
  }

  @Override
  public int hashCode() {
    return Objects.hash(prefix, uri);
  }
}
