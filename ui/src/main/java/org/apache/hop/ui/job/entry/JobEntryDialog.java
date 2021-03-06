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

package org.apache.hop.ui.job.entry;

import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.logging.LoggingObjectInterface;
import org.apache.hop.core.logging.LoggingObjectType;
import org.apache.hop.core.logging.SimpleLoggingObject;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.job.JobMeta;
import org.apache.hop.job.entry.JobEntryInterface;
import org.apache.hop.metastore.api.IMetaStore;
import org.apache.hop.trans.step.StepInterface;
import org.apache.hop.ui.core.PropsUI;
import org.apache.hop.ui.core.database.dialog.DatabaseDialog;
import org.apache.hop.ui.core.widget.MetaSelectionManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Shell;

/**
 * The JobEntryDialog class is responsible for constructing and opening the settings dialog for the job entry. Whenever
 * the user opens the job entry settings in Spoon, it will instantiate the dialog class passing in the JobEntryInterface
 * object and call the
 *
 * <pre>
 * open()
 * </pre>
 * <p>
 * method on the dialog. SWT is the native windowing environment of Spoon, and it is typically the framework used for
 * implementing job entry dialogs.
 */
public class JobEntryDialog extends Dialog {

  /**
   * The package name, used for internationalization.
   */
  private static Class<?> PKG = StepInterface.class; // for i18n purposes, needed by Translator2!!

  /**
   * The loggingObject for the dialog
   */
  public static final LoggingObjectInterface loggingObject = new SimpleLoggingObject(
    "Job entry dialog", LoggingObjectType.JOBENTRYDIALOG, null );

  /**
   * A reference to the job entry interface
   */
  protected JobEntryInterface jobEntryInt;

  /**
   * the MetaStore
   */
  protected IMetaStore metaStore;

  /**
   * The job metadata object.
   */
  protected JobMeta jobMeta;

  /**
   * A reference to the shell object
   */
  protected Shell shell;

  /**
   * A reference to the properties user interface
   */
  protected PropsUI props;

  /**
   * A reference to the parent shell
   */
  protected Shell parent;

  /**
   * A reference to a database dialog
   */
  protected DatabaseDialog databaseDialog;

  /**
   * Instantiates a new job entry dialog.
   *
   * @param parent   the parent shell
   * @param jobEntry the job entry interface
   * @param jobMeta  the job metadata object
   */
  public JobEntryDialog( Shell parent, JobEntryInterface jobEntry, JobMeta jobMeta ) {
    super( parent, SWT.NONE );
    props = PropsUI.getInstance();

    this.jobEntryInt = jobEntry;
    this.jobMeta = jobMeta;
    this.shell = parent;
  }

  /**
   * Adds the connection line for the given parent and previous control, and returns a meta selection manager control
   *
   * @param parent   the parent composite object
   * @param previous the previous control
   * @param
   * @return the combo box UI component
   */
  public MetaSelectionManager<DatabaseMeta> addConnectionLine( Composite parent, Control previous, DatabaseMeta selected, ModifyListener lsMod ) {

    final MetaSelectionManager<DatabaseMeta> wConnection = new MetaSelectionManager<>(
      jobMeta,
      metaStore,
      DatabaseMeta.class, parent, SWT.NONE,
      BaseMessages.getString( PKG, "BaseStepDialog.Connection.Label" ),
      "Select the relational database connection to use" // TODO : i18n
    );
    wConnection.addToConnectionLine( parent, previous, selected, lsMod );
    return wConnection;
  }

  public IMetaStore getMetaStore() {
    return metaStore;
  }

  public void setMetaStore( IMetaStore metaStore ) {
    this.metaStore = metaStore;
  }


}
