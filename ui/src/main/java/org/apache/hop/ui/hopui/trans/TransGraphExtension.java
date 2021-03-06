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

package org.apache.hop.ui.hopui.trans;

import org.apache.hop.core.gui.Point;
import org.eclipse.swt.events.MouseEvent;

public class TransGraphExtension {

  private TransGraph transGraph;
  private MouseEvent event;
  private Point point;
  private boolean preventDefault;

  public TransGraphExtension( TransGraph transGraph, MouseEvent event, Point point ) {
    this.transGraph = transGraph;
    this.event = event;
    this.point = point;
  }

  public TransGraph getTransGraph() {
    return transGraph;
  }

  public void setTransGraph( TransGraph transGraph ) {
    this.transGraph = transGraph;
  }

  public MouseEvent getEvent() {
    return event;
  }

  public void setEvent( MouseEvent event ) {
    this.event = event;
  }

  public Point getPoint() {
    return point;
  }

  public void setPoint( Point point ) {
    this.point = point;
  }

  public boolean isPreventDefault() {
    return preventDefault;
  }

  public void setPreventDefault( boolean preventDefault ) {
    this.preventDefault = preventDefault;
  }
}
