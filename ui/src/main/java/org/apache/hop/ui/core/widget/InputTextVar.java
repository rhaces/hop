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

package org.apache.hop.ui.core.widget;

import org.apache.hop.core.variables.VariableSpace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class InputTextVar extends Input<TextVar> {
  public InputTextVar( VariableSpace space, Composite composite, int width1, int width2 ) {
    super( space, composite, width1, width2 );
  }

  @Override
  protected void initText( VariableSpace space, Composite composite, int flags ) {
    input = new TextVar( space, this, SWT.LEFT | SWT.SINGLE | SWT.BORDER );
  }
}
