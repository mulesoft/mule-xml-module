/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.internal.error;

import static java.util.Optional.ofNullable;
import org.mule.runtime.extension.api.exception.ModuleException;

import java.util.Optional;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link ErrorListener} which transforms generic exceptions into module exceptions
 *
 * @since 1.0
 */
public class TransformerErrorListener implements ErrorListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransformerErrorListener.class);

  private ModuleException e = null;

  public Optional<ModuleException> getException() {
    return ofNullable(e);
  }

  @Override
  public void error(TransformerException exception) throws TransformerException {
    e = new TransformationException(exception.getMessage(), exception);
  }

  @Override
  public void fatalError(TransformerException exception) throws TransformerException {
    e = new TransformationException(exception.getMessage(), exception);
  }

  @Override
  public void warning(TransformerException exception) throws TransformerException {
    LOGGER.warn(exception.getMessage());
  }
}
