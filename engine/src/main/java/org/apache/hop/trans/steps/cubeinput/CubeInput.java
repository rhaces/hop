/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.apache.hop.trans.steps.cubeinput;

import org.apache.hop.core.Const;
import org.apache.hop.core.ResultFile;
import org.apache.hop.core.exception.HopEOFException;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopFileException;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.vfs.HopVFS;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.trans.Trans;
import org.apache.hop.trans.TransMeta;
import org.apache.hop.trans.step.BaseStep;
import org.apache.hop.trans.step.StepDataInterface;
import org.apache.hop.trans.step.StepInterface;
import org.apache.hop.trans.step.StepMeta;
import org.apache.hop.trans.step.StepMetaInterface;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.zip.GZIPInputStream;

public class CubeInput extends BaseStep implements StepInterface {
  private static Class<?> PKG = CubeInputMeta.class; // for i18n purposes, needed by Translator2!!

  private CubeInputMeta meta;
  private CubeInputData data;
  private int realRowLimit;

  public CubeInput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws HopException {

    if ( first ) {
      first = false;
      meta = (CubeInputMeta) smi;
      data = (CubeInputData) sdi;
      realRowLimit = Const.toInt( environmentSubstitute( meta.getRowLimit() ), 0 );
    }


    try {
      Object[] r = data.meta.readData( data.dis );
      putRow( data.meta, r ); // fill the rowset(s). (sleeps if full)
      incrementLinesInput();

      if ( realRowLimit > 0 && getLinesInput() >= realRowLimit ) { // finished!
        setOutputDone();
        return false;
      }
    } catch ( HopEOFException eof ) {
      setOutputDone();
      return false;
    } catch ( SocketTimeoutException e ) {
      throw new HopException( e ); // shouldn't happen on files
    }

    if ( checkFeedback( getLinesInput() ) ) {
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "CubeInput.Log.LineNumber" ) + getLinesInput() );
      }
    }

    return true;
  }

  @Override public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (CubeInputMeta) smi;
    data = (CubeInputData) sdi;

    if ( super.init( smi, sdi ) ) {
      try {
        String filename = environmentSubstitute( meta.getFilename() );

        // Add filename to result filenames ?
        if ( meta.isAddResultFile() ) {
          ResultFile resultFile =
            new ResultFile(
              ResultFile.FILE_TYPE_GENERAL, HopVFS.getFileObject( filename, getTransMeta() ),
              getTransMeta().getName(), toString() );
          resultFile.setComment( "File was read by a Cube Input step" );
          addResultFile( resultFile );
        }

        data.fis = HopVFS.getInputStream( filename, this );
        data.zip = new GZIPInputStream( data.fis );
        data.dis = new DataInputStream( data.zip );

        try {
          data.meta = new RowMeta( data.dis );
          return true;
        } catch ( HopFileException kfe ) {
          logError( BaseMessages.getString( PKG, "CubeInput.Log.UnableToReadMetadata" ), kfe );
          return false;
        }
      } catch ( Exception e ) {
        logError( BaseMessages.getString( PKG, "CubeInput.Log.ErrorReadingFromDataCube" ), e );
      }
    }
    return false;
  }

  @Override public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (CubeInputMeta) smi;
    data = (CubeInputData) sdi;

    try {
      if ( data.dis != null ) {
        data.dis.close();
        data.dis = null;
      }
      if ( data.zip != null ) {
        data.zip.close();
        data.zip = null;
      }
      if ( data.fis != null ) {
        data.fis.close();
        data.fis = null;
      }
    } catch ( IOException e ) {
      logError( BaseMessages.getString( PKG, "CubeInput.Log.ErrorClosingCube" ) + e.toString() );
      setErrors( 1 );
      stopAll();
    }

    super.dispose( smi, sdi );
  }
}
