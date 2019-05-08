/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.xml.xslt;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.mule.module.xml.XmlTestCase;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.core.api.util.UUID;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class XsltResultDocumentTestCase extends XmlTestCase {

  private static final String FILE = "file:///";
  private static final String INPUT_FILE = "cities.xml";
  private static final String OUTPUT_FILE_PROPERTY = "outputFile";
  private static final String FLOW_NAME = "listCities";
  private static final String EXPECTED_OUTPUT =
      "italy - milan - 5 | france - paris - 7 | germany - munich - 4 | france - lyon - 2 | italy - venice - 1 | ";

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private String cities;
  private File outputFile;

  @Override
  protected String getConfigFile() {
    return "xsl/xslt-result-document-config.xml";
  }

  @Before
  public void setUp() throws Exception {
    cities = IOUtils.getResourceAsString(INPUT_FILE, getClass());
    outputFile = temporaryFolder.newFile(UUID.getUUID());
  }

  @Test
  public void writeToFileOneTime() throws Exception {
    executeFlowAndValidateOutput(cities, outputFile);
  }

  @Test
  public void writeToSameFileSeveralTimes() throws Exception {
    executeFlowAndValidateOutput(cities, outputFile);
    executeFlowAndValidateOutput(cities, outputFile);
  }

  private void executeFlowAndValidateOutput(String payload, File outputFile) throws Exception {
    outputFile.delete();
    flowRunner(FLOW_NAME).withPayload(payload)
        .withVariable(OUTPUT_FILE_PROPERTY, FILE + outputFile.getAbsolutePath())
        .run();

    assertThat(FileUtils.readFileToString(outputFile), is(EXPECTED_OUTPUT));
  }

}
