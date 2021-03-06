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

package org.apache.hop.core.util.serialization;

import org.apache.hop.core.exception.HopXMLException;
import org.apache.hop.core.variables.VariableSpace;
import org.apache.hop.metastore.api.IMetaStore;
import org.apache.hop.trans.step.BaseStepMeta;
import org.apache.hop.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

import static org.apache.hop.core.util.serialization.MetaXmlSerializer.deserialize;
import static org.apache.hop.core.util.serialization.MetaXmlSerializer.serialize;
import static org.apache.hop.core.util.serialization.StepMetaProps.from;

/**
 * Handles serialization of meta by implementing getXML/loadXML, readRep/saveRep.
 * <p>
 * Uses {@link MetaXmlSerializer} for generically
 * handling child classes meta.
 */
public abstract class BaseSerializingMeta extends BaseStepMeta implements StepMetaInterface {

  @Override public String getXML() {
    return serialize( from( this ) );
  }

  @Override public void loadXML(
    Node stepnode, IMetaStore metaStore ) throws HopXMLException {
    deserialize( stepnode ).to( this );
  }

  /**
   * Creates a copy of this stepMeta with variables globally substituted.
   */
  public StepMetaInterface withVariables( VariableSpace variables ) {
    return StepMetaProps
      .from( this )
      .withVariables( variables )
      .to( (StepMetaInterface) this.copyObject() );
  }

  /**
   * This is intended to act the way clone should (return a fully independent copy of the original).  This method
   * name was chosen in order to force any subclass that wants to use withVariables to implement a proper clone
   * override, but let others ignore it.
   *
   * @return a copy of this object
   */
  public BaseSerializingMeta copyObject() {
    throw new UnsupportedOperationException( "This method must be overridden if you use withVariables." );
  }
}
