/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.apache.hop.trans.steps.rest;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;
import org.apache.hop.core.HopClientEnvironment;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopStepException;
import org.apache.hop.core.logging.LoggingObjectInterface;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.core.row.value.ValueMetaInteger;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.core.util.Assert;
import org.apache.hop.trans.Trans;
import org.apache.hop.trans.TransMeta;
import org.apache.hop.trans.step.StepDataInterface;
import org.apache.hop.trans.step.StepMeta;
import org.apache.hop.trans.steps.mock.StepMockHelper;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.MultivaluedMap;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * User: Dzmitry Stsiapanau Date: 11/29/13 Time: 3:42 PM
 */
public class RestIT {

  public static final String HTTP_LOCALHOST_9998 = "http://localhost:9998/";

  private class RestHandler extends Rest {

    Object[] row = new Object[] { "anyData" };
    Object[] outputRow;
    boolean override;

    public RestHandler( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                        Trans trans, boolean override ) {
      super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
      this.override = override;
    }

    @SuppressWarnings( "unused" )
    public void setRow( Object[] row ) {
      this.row = row;
    }

    /**
     * In case of getRow, we receive data from previous steps through the input rowset. In case we split the stream, we
     * have to copy the data to the alternate splits: rowsets 1 through n.
     */
    @Override
    public Object[] getRow() throws HopException {
      return row;
    }

    /**
     * putRow is used to copy a row, to the alternate rowset(s) This should get priority over everything else!
     * (synchronized) If distribute is true, a row is copied only once to the output rowsets, otherwise copies are sent
     * to each rowset!
     *
     * @param row The row to put to the destination rowset(s).
     * @throws org.apache.hop.core.exception.HopStepException
     */
    @Override
    public void putRow( RowMetaInterface rowMeta, Object[] row ) throws HopStepException {
      outputRow = row;
    }

    @Override
    protected MultivaluedMap<String, String> searchForHeaders( ClientResponse response ) {
      if ( override ) {
        String host = "host";
        List<String> localhost = new ArrayList<String>();
        localhost.add( "localhost" );
        Map.Entry<String, List<String>> entry = Mockito.mock( Map.Entry.class );
        when( entry.getKey() ).thenReturn( host );
        when( entry.getValue() ).thenReturn( localhost );
        Set<Map.Entry<String, List<String>>> set = new HashSet<Map.Entry<String, List<String>>>();
        set.add( entry );
        MultivaluedMap<String, String> test = Mockito.mock( MultivaluedMap.class );
        when( test.entrySet() ).thenReturn( set );
        return test;
      } else {
        return super.searchForHeaders( response );
      }
    }

    public Object[] getOutputRow() {
      return outputRow;
    }

  }

  private StepMockHelper<RestMeta, RestData> stepMockHelper;
  private HttpServer server;

  @BeforeClass
  public static void setupBeforeClass() throws HopException {
    HopClientEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    // This is only here because everything blows up due to the version
    // of Java we're running -vs- the version of ASM we rely on thanks to the
    // version of Jetty we need. When we upgrade to Jetty 8, this NPE will go away.
    // I'm only catching the NPE to allow the test to work as much as it can with
    // the version of Jetty we use.
    // Makes me wonder if we can change the version of jetty used in test cases to
    // the later one to avoid this before we have to go and change the version of
    // Jetty for the rest of the platform.
    //
    // MB - 5/2016
    Assume.assumeTrue( !System.getProperty( "java.version" ).startsWith( "1.8" ) );
    stepMockHelper = new StepMockHelper<RestMeta, RestData>( "REST CLIENT TEST", RestMeta.class, RestData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    verify( stepMockHelper.trans, never() ).stopAll();
    server = HttpServerFactory.create( HTTP_LOCALHOST_9998 );
    server.start();
  }

  @After
  public void tearDown() throws Exception {
    if ( server != null ) {
      server.stop( 0 );
    }
  }

  @Test
  public void testNoContent() throws Exception {
    RestData data = new RestData();
    int[] index = { 0, 1 };
    RowMeta meta = new RowMeta();
    meta.addValueMeta( new ValueMetaString( "fieldName" ) );
    meta.addValueMeta( new ValueMetaInteger( "codeFieldName" ) );
    Object[] expectedRow = new Object[] { "", 204L };
    Rest rest =
      new RestHandler( stepMockHelper.stepMeta, data, 0, stepMockHelper.transMeta, stepMockHelper.trans, false );
    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
    rest.setInputRowMeta( inputRowMeta );
    when( inputRowMeta.clone() ).thenReturn( inputRowMeta );
    when( stepMockHelper.processRowsStepMetaInterface.getUrl() ).thenReturn(
      HTTP_LOCALHOST_9998 + "restTest/restNoContentAnswer" );
    when( stepMockHelper.processRowsStepMetaInterface.getMethod() ).thenReturn( RestMeta.HTTP_METHOD_GET );
    rest.init( stepMockHelper.processRowsStepMetaInterface, data );
    data.resultFieldName = "ResultFieldName";
    data.resultCodeFieldName = "ResultCodeFieldName";
    Assert.assertTrue( rest.processRow( stepMockHelper.processRowsStepMetaInterface, data ) );
    Object[] out = ( (RestHandler) rest ).getOutputRow();
    Assert.assertTrue( meta.equals( out, expectedRow, index ) );
  }

  @Test
  public void testResponseHeader() throws Exception {
    try {
      RestData data = new RestData();
      int[] index = { 0, 1, 3 };
      RowMeta meta = new RowMeta();
      meta.addValueMeta( new ValueMetaString( "fieldName" ) );
      meta.addValueMeta( new ValueMetaInteger( "codeFieldName" ) );
      meta.addValueMeta( new ValueMetaInteger( "responseTimeFieldName" ) );
      meta.addValueMeta( new ValueMetaString( "headerFieldName" ) );
      Object[] expectedRow =
        new Object[] { "", 204L, 0L, "{\"host\":\"localhost\"}" };
      Rest rest =
        new RestHandler( stepMockHelper.stepMeta, data, 0, stepMockHelper.transMeta, stepMockHelper.trans, true );
      RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );
      rest.setInputRowMeta( inputRowMeta );
      when( inputRowMeta.clone() ).thenReturn( inputRowMeta );
      when( stepMockHelper.processRowsStepMetaInterface.getUrl() ).thenReturn(
        HTTP_LOCALHOST_9998 + "restTest/restNoContentAnswer" );
      when( stepMockHelper.processRowsStepMetaInterface.getMethod() ).thenReturn( RestMeta.HTTP_METHOD_GET );
      when( stepMockHelper.processRowsStepMetaInterface.getResponseTimeFieldName() ).thenReturn(
        "ResponseTimeFieldName" );
      when( stepMockHelper.processRowsStepMetaInterface.getResponseHeaderFieldName() ).thenReturn(
        "ResponseHeaderFieldName" );
      rest.init( stepMockHelper.processRowsStepMetaInterface, data );
      data.resultFieldName = "ResultFieldName";
      data.resultCodeFieldName = "ResultCodeFieldName";
      Assert.assertTrue( rest.processRow( stepMockHelper.processRowsStepMetaInterface, data ) );
      Object[] out = ( (RestHandler) rest ).getOutputRow();
      Assert.assertTrue( meta.equals( out, expectedRow, index ) );
    } catch ( ArrayIndexOutOfBoundsException ex ) {
      // This is only here because everything blows up due to the version
      // of Java we're running -vs- the version of ASM we rely on thanks to the
      // version of Jetty we need. When we upgrade to Jetty 8, this NPE will go away.
      // I'm only catching the NPE to allow the test to work as much as it can with
      // the version of Jetty we use.
      // Makes me wonder if we can change the version of jetty used in test cases to
      // the later one to avoid this before we have to go and change the version of
      // Jetty for the rest of the platform.
      //
      // MB - 5/2016
      org.junit.Assert.assertTrue( System.getProperty( "java.version" ).startsWith( "1.8" ) );
    }
  }
}
