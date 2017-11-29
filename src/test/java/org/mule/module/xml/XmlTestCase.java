/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml;

import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.module.xml.api.XmlError;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

@ArtifactClassLoaderRunnerConfig(exportPluginClasses = {XmlError.class})
public abstract class XmlTestCase extends MuleArtifactFunctionalTestCase {

  protected static final String ERROR_NAMESPACE = "XML-MODULE";
}
