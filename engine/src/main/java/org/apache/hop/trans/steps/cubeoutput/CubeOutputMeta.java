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

package org.apache.hop.trans.steps.cubeoutput;

import org.apache.commons.vfs2.FileObject;
import org.apache.hop.core.CheckResult;
import org.apache.hop.core.CheckResultInterface;
import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopXMLException;
import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.core.variables.VariableSpace;
import org.apache.hop.core.vfs.HopVFS;
import org.apache.hop.core.xml.XMLHandler;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.metastore.api.IMetaStore;
import org.apache.hop.resource.ResourceDefinition;
import org.apache.hop.resource.ResourceNamingInterface;
import org.apache.hop.trans.Trans;
import org.apache.hop.trans.TransMeta;
import org.apache.hop.trans.step.BaseStepMeta;
import org.apache.hop.trans.step.StepDataInterface;
import org.apache.hop.trans.step.StepInterface;
import org.apache.hop.trans.step.StepMeta;
import org.apache.hop.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;

/*
 * Created on 4-apr-2003
 *
 */
public class CubeOutputMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = CubeOutputMeta.class; // for i18n purposes, needed by Translator2!!

  private String filename;
  /**
   * Flag: add the filenames to result filenames
   */
  private boolean addToResultFilenames;

  /**
   * Flag : Do not open new file when transformation start
   */
  private boolean doNotOpenNewFileInit;

  public CubeOutputMeta() {
    super(); // allocate BaseStepMeta
  }

  public void loadXML( Node stepnode, IMetaStore metaStore ) throws HopXMLException {
    readData( stepnode );
  }

  /**
   * @param filename The filename to set.
   */
  public void setFilename( String filename ) {
    this.filename = filename;
  }

  /**
   * @return Returns the filename.
   */
  public String getFilename() {
    return filename;
  }

  /**
   * @return Returns the add to result filesname.
   */
  public boolean isAddToResultFiles() {
    return addToResultFilenames;
  }

  /**
   * @param addtoresultfilenamesin The addtoresultfilenames to set.
   */
  public void setAddToResultFiles( boolean addtoresultfilenamesin ) {
    this.addToResultFilenames = addtoresultfilenamesin;
  }

  /**
   * @return Returns the "do not open new file at init" flag.
   */
  public boolean isDoNotOpenNewFileInit() {
    return doNotOpenNewFileInit;
  }

  /**
   * @param doNotOpenNewFileInit The "do not open new file at init" flag to set.
   */
  public void setDoNotOpenNewFileInit( boolean doNotOpenNewFileInit ) {
    this.doNotOpenNewFileInit = doNotOpenNewFileInit;
  }

  public Object clone() {
    CubeOutputMeta retval = (CubeOutputMeta) super.clone();

    return retval;
  }

  private void readData( Node stepnode ) throws HopXMLException {
    try {
      filename = XMLHandler.getTagValue( stepnode, "file", "name" );
      addToResultFilenames =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "add_to_result_filenames" ) );
      doNotOpenNewFileInit =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "file", "do_not_open_newfile_init" ) );

    } catch ( Exception e ) {
      throw new HopXMLException(
        BaseMessages.getString( PKG, "CubeOutputMeta.Exception.UnableToLoadStepInfo" ), e );
    }
  }

  public void setDefault() {
    filename = "file.cube";
    addToResultFilenames = false;
    doNotOpenNewFileInit = false;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 300 );

    retval.append( "    <file>" ).append( Const.CR );
    retval.append( "      " ).append( XMLHandler.addTagValue( "name", filename ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "add_to_result_filenames", addToResultFilenames ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "do_not_open_newfile_init", doNotOpenNewFileInit ) );

    retval.append( "    </file>" ).append( Const.CR );

    return retval.toString();
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
                     RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
                     IMetaStore metaStore ) {
    CheckResult cr;

    // Check output fields
    if ( prev != null && prev.size() > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "CubeOutputMeta.CheckResult.ReceivingFields", String.valueOf( prev.size() ) ), stepMeta );
      remarks.add( cr );
    }

    cr =
      new CheckResult( CheckResult.TYPE_RESULT_COMMENT, BaseMessages.getString(
        PKG, "CubeOutputMeta.CheckResult.FileSpecificationsNotChecked" ), stepMeta );
    remarks.add( cr );
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
                                TransMeta transMeta, Trans trans ) {
    return new CubeOutput( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new CubeOutputData();
  }

  /**
   * @param space                   the variable space to use
   * @param definitions
   * @param resourceNamingInterface
   * @param metaStore               the metaStore in which non-kettle metadata could reside.
   * @return the filename of the exported resource
   */
  public String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
                                 ResourceNamingInterface resourceNamingInterface, IMetaStore metaStore ) throws HopException {
    try {
      // The object that we're modifying here is a copy of the original!
      // So let's change the filename from relative to absolute by grabbing the file object...
      //
      // From : ${Internal.Transformation.Filename.Directory}/../foo/bar.data
      // To : /home/matt/test/files/foo/bar.data
      //
      FileObject fileObject = HopVFS.getFileObject( space.environmentSubstitute( filename ), space );

      // If the file doesn't exist, forget about this effort too!
      //
      if ( fileObject.exists() ) {
        // Convert to an absolute path...
        //
        filename = resourceNamingInterface.nameResource( fileObject, space, true );

        return filename;
      }
      return null;
    } catch ( Exception e ) {
      throw new HopException( e );
    }
  }
}
