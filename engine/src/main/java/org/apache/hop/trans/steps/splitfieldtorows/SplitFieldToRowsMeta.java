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

package org.apache.hop.trans.steps.splitfieldtorows;

import org.apache.hop.core.CheckResult;
import org.apache.hop.core.CheckResultInterface;
import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopStepException;
import org.apache.hop.core.exception.HopXMLException;
import org.apache.hop.core.row.RowMetaInterface;
import org.apache.hop.core.row.ValueMetaInterface;
import org.apache.hop.core.row.value.ValueMetaInteger;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.VariableSpace;
import org.apache.hop.core.xml.XMLHandler;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.metastore.api.IMetaStore;
import org.apache.hop.trans.Trans;
import org.apache.hop.trans.TransMeta;
import org.apache.hop.trans.step.BaseStepMeta;
import org.apache.hop.trans.step.StepDataInterface;
import org.apache.hop.trans.step.StepInterface;
import org.apache.hop.trans.step.StepMeta;
import org.apache.hop.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

import java.util.List;

public class SplitFieldToRowsMeta extends BaseStepMeta implements StepMetaInterface {
  private static Class<?> PKG = SplitFieldToRowsMeta.class; // for i18n purposes, needed by Translator2!!

  /**
   * Field to split
   */
  private String splitField;

  /**
   * Split field based upon this delimiter.
   */
  private String delimiter;

  /**
   * New name of the split field
   */
  private String newFieldname;

  /**
   * Flag indicating that a row number field should be included in the output
   */
  private boolean includeRowNumber;

  /**
   * The name of the field in the output containing the row number
   */
  private String rowNumberField;

  /**
   * Flag indicating that we should reset RowNum for each file
   */
  private boolean resetRowNumber;

  /**
   * Flag indicating that the delimiter is a regular expression
   */
  private boolean isDelimiterRegex;

  public boolean isDelimiterRegex() {
    return isDelimiterRegex;
  }

  public void setDelimiterRegex( boolean isDelimiterRegex ) {
    this.isDelimiterRegex = isDelimiterRegex;
  }

  public SplitFieldToRowsMeta() {
    super(); // allocate BaseStepMeta
  }

  /**
   * @return Returns the delimiter.
   */
  public String getDelimiter() {
    return delimiter;
  }

  /**
   * @param delimiter The delimiter to set.
   */
  public void setDelimiter( String delimiter ) {
    this.delimiter = delimiter;
  }

  /**
   * @return Returns the splitField.
   */
  public String getSplitField() {
    return splitField;
  }

  /**
   * @param splitField The splitField to set.
   */
  public void setSplitField( String splitField ) {
    this.splitField = splitField;
  }

  public void loadXML( Node stepnode, IMetaStore metaStore ) throws HopXMLException {
    readData( stepnode );
  }

  private void readData( Node stepnode ) throws HopXMLException {
    try {
      splitField = XMLHandler.getTagValue( stepnode, "splitfield" );
      delimiter = XMLHandler.getTagValue( stepnode, "delimiter" );
      newFieldname = XMLHandler.getTagValue( stepnode, "newfield" );
      includeRowNumber = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "rownum" ) );
      resetRowNumber = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "resetrownumber" ) );
      rowNumberField = XMLHandler.getTagValue( stepnode, "rownum_field" );
      isDelimiterRegex = "Y".equalsIgnoreCase( XMLHandler.getTagValue( stepnode, "delimiter_is_regex" ) );
    } catch ( Exception e ) {
      throw new HopXMLException( BaseMessages.getString(
        PKG, "SplitFieldToRowsMeta.Exception.UnableToLoadStepInfoFromXML" ), e );
    }
  }

  public void setDefault() {
    splitField = "";
    delimiter = ";";
    newFieldname = "";
    includeRowNumber = false;
    isDelimiterRegex = false;
    rowNumberField = "";
    resetRowNumber = true;
  }

  public void getFields( RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
                         VariableSpace space, IMetaStore metaStore ) throws HopStepException {

    ValueMetaInterface v = new ValueMetaString( newFieldname );
    v.setOrigin( name );
    row.addValueMeta( v );

    // include row number
    if ( includeRowNumber ) {
      v = new ValueMetaInteger( space.environmentSubstitute( rowNumberField ) );
      v.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
      v.setOrigin( name );
      row.addValueMeta( v );
    }
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "   " + XMLHandler.addTagValue( "splitfield", splitField ) );
    retval.append( "   " + XMLHandler.addTagValue( "delimiter", delimiter ) );
    retval.append( "   " + XMLHandler.addTagValue( "newfield", newFieldname ) );
    retval.append( "   " + XMLHandler.addTagValue( "rownum", includeRowNumber ) );
    retval.append( "   " + XMLHandler.addTagValue( "rownum_field", rowNumberField ) );
    retval.append( "   " + XMLHandler.addTagValue( "resetrownumber", resetRowNumber ) );
    retval.append( "   " + XMLHandler.addTagValue( "delimiter_is_regex", isDelimiterRegex ) );

    return retval.toString();
  }

  public void check( List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
                     RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space,
                     IMetaStore metaStore ) {
    String error_message = "";
    CheckResult cr;

    // Look up fields in the input stream <prev>
    if ( prev != null && prev.size() > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SplitFieldToRowsMeta.CheckResult.StepReceivingFields", prev.size() + "" ), stepMeta );
      remarks.add( cr );

      error_message = "";

      ValueMetaInterface v = prev.searchValueMeta( splitField );
      if ( v == null ) {
        error_message =
          BaseMessages.getString(
            PKG, "SplitFieldToRowsMeta.CheckResult.FieldToSplitNotPresentInInputStream", splitField );
        cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
        remarks.add( cr );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "SplitFieldToRowsMeta.CheckResult.FieldToSplitFoundInInputStream", splitField ), stepMeta );
        remarks.add( cr );
      }
    } else {
      error_message =
        BaseMessages.getString( PKG, "SplitFieldToRowsMeta.CheckResult.CouldNotReadFieldsFromPreviousStep" )
          + Const.CR;
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta );
      remarks.add( cr );
    }

    // See if we have input streams leading to this step!
    if ( input.length > 0 ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
          PKG, "SplitFieldToRowsMeta.CheckResult.StepReceivingInfoFromOtherStep" ), stepMeta );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SplitFieldToRowsMeta.CheckResult.NoInputReceivedFromOtherStep" ), stepMeta );
      remarks.add( cr );
    }

    if ( Utils.isEmpty( newFieldname ) ) {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
          PKG, "SplitFieldToRowsMeta.CheckResult.NewFieldNameIsNull" ), stepMeta );
      remarks.add( cr );
    }
    if ( includeRowNumber ) {
      if ( Utils.isEmpty( transMeta.environmentSubstitute( rowNumberField ) ) ) {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(
            PKG, "SplitFieldToRowsMeta.CheckResult.RowNumberFieldMissing" ), stepMeta );
      } else {
        cr =
          new CheckResult( CheckResult.TYPE_RESULT_OK, BaseMessages.getString(
            PKG, "SplitFieldToRowsMeta.CheckResult.RowNumberFieldOk" ), stepMeta );
      }
      remarks.add( cr );
    }
  }

  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
                                TransMeta transMeta, Trans trans ) {
    return new SplitFieldToRows( stepMeta, stepDataInterface, cnr, transMeta, trans );
  }

  public StepDataInterface getStepData() {
    return new SplitFieldToRowsData();
  }

  /**
   * @return the newFieldname
   */
  public String getNewFieldname() {
    return newFieldname;
  }

  /**
   * @param newFieldname the newFieldname to set
   */
  public void setNewFieldname( String newFieldname ) {
    this.newFieldname = newFieldname;
  }

  /**
   * @return Returns the rowNumberField.
   */
  public String getRowNumberField() {
    return rowNumberField;
  }

  /**
   * @param rowNumberField The rowNumberField to set.
   */
  public void setRowNumberField( String rowNumberField ) {
    this.rowNumberField = rowNumberField;
  }

  /**
   * @return Returns the resetRowNumber.
   */
  public boolean resetRowNumber() {
    return resetRowNumber;
  }

  /**
   * @param resetRowNumber The resetRowNumber to set.
   */
  public void setResetRowNumber( boolean resetRowNumber ) {
    this.resetRowNumber = resetRowNumber;
  }

  /**
   * @param includeRowNumber The includeRowNumber to set.
   */
  public void setIncludeRowNumber( boolean includeRowNumber ) {
    this.includeRowNumber = includeRowNumber;
  }

  /**
   * @return Returns the includeRowNumber.
   */
  public boolean includeRowNumber() {
    return includeRowNumber;
  }
}
