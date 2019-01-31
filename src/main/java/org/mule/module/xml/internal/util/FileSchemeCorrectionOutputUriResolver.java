/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.internal.util;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.Result;

import net.sf.saxon.lib.StandardOutputResolver;
import net.sf.saxon.trans.XPathException;

public class FileSchemeCorrectionOutputUriResolver extends StandardOutputResolver {

  private static final String PATH_URI_SCHEME = "file:";

  @Override
  public Result resolve(String href, String base) throws XPathException {
    // If neither baseUri is not null, nor the href has an URI scheme, the href will be
    // considered as an absolute file path
    if (base == null && !hasUriScheme(href)) {
      href = PATH_URI_SCHEME.concat(href);
    }
    return super.resolve(href, base);
  }

  protected boolean hasUriScheme(String href) {
    URI parsedUri;
    try {
      parsedUri = new URI(href);
      return parsedUri.getScheme() != null;
    } catch (URISyntaxException e) {
      return false;
    }
  }
}
