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

package org.apache.hop.ui.core.database;

import org.apache.commons.lang.StringUtils;
import org.apache.hop.core.Const;
import org.apache.hop.core.HopClientEnvironment;
import org.apache.hop.core.HopEnvironment;
import org.apache.hop.core.Props;
import org.apache.hop.core.database.DatabaseMeta;
import org.apache.hop.core.database.DatabaseTestResults;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.gui.plugin.GuiPlugin;
import org.apache.hop.core.plugins.DatabasePluginType;
import org.apache.hop.core.plugins.PluginInterface;
import org.apache.hop.core.plugins.PluginRegistry;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.metastore.api.IMetaStore;
import org.apache.hop.metastore.api.dialog.IMetaStoreDialog;
import org.apache.hop.ui.core.PropsUI;
import org.apache.hop.ui.core.dialog.ShowMessageDialog;
import org.apache.hop.ui.core.gui.GUIResource;
import org.apache.hop.ui.core.gui.GuiElementWidgets;
import org.apache.hop.ui.core.gui.WindowProperty;
import org.apache.hop.ui.core.widget.ColumnInfo;
import org.apache.hop.ui.core.widget.ComboVar;
import org.apache.hop.ui.core.widget.TableView;
import org.apache.hop.ui.core.widget.TextVar;
import org.apache.hop.ui.trans.step.BaseStepDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@GuiPlugin(
  id = "DatabaseConnection",
  description = "This is the dialog for database connection metadata"
)
/**
 * The dialog for IMetaStore element DatabaseMeta
 * Don't move this class around as it's sync'ed with the DatabaseMeta package to find the dialog.
 *
 */
public class DatabaseMetaDialog extends Dialog implements IMetaStoreDialog {
  private static Class<?> PKG = DatabaseMetaDialog.class; // for i18n purposes, needed by Translator2!!
  private Shell parent;
  private Shell shell;
  private IMetaStore metaStore;
  private DatabaseMeta databaseMeta;
  private DatabaseMeta workingMeta;

  private CTabFolder wTabFolder;

  private CTabItem wGeneralTab;
  private Composite wGeneralComp;
  private FormData fdGeneralComp;
  private Text wName;
  private ComboVar wConnectionType;
  private Button wODBC;
  private Label wlOdbcDsn;
  private TextVar wOdbcDsn;
  private Label wlManualUrl;
  private TextVar wManualUrl;
  private TextVar wUsername;
  private TextVar wPassword;

  private Composite wDatabaseSpecificComp;
  private GuiElementWidgets guiElementWidgets;

  private CTabItem wAdvancedTab;
  private Composite wAdvancedComp;
  private FormData fdAdvancedComp;
  private Button wSupportsBoolean;
  private Button wSupportsTimestamp;
  private Button wQuoteAll;
  private Button wForceLowercase;
  private Button wForceUppercase;
  private Button wPreserveCase;
  private TextVar wPreferredSchema;
  private TextVar wSQLStatements;

  private CTabItem wOptionsTab;
  private Composite wOptionsComp;
  private FormData fdOptionsComp;
  private TableView wOptions;

  private final PropsUI props;
  private int middle;
  private int margin;

  private String returnValue;

  /**
   * @param parent       The parent shell
   * @param databaseMeta The object to edit
   */
  public DatabaseMetaDialog( Shell parent, DatabaseMeta databaseMeta ) {
    this( parent, null, databaseMeta );
  }

  /**
   * @param parent       The parent shell
   * @param metaStore    metaStore
   * @param databaseMeta The object to edit
   */
  public DatabaseMetaDialog( Shell parent, IMetaStore metaStore, DatabaseMeta databaseMeta ) {
    super( parent, SWT.NONE );
    this.parent = parent;
    this.metaStore = metaStore;
    this.databaseMeta = databaseMeta;
    this.workingMeta = new DatabaseMeta( databaseMeta );
    props = PropsUI.getInstance();
    returnValue = null;
  }

  public String open() {
    // Create a tabbed interface instead of the confusing left hand side options
    // This will make it more conforming the rest.
    //
    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    shell.setImage( GUIResource.getInstance().getImageConnection() );

    middle = props.getMiddlePct();
    margin = Const.MARGIN;

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setText( BaseMessages.getString( PKG, "DatabaseDialog.Shell.title" ) );
    shell.setLayout( formLayout );

    // Add buttons at the bottom
    Button wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wOK.addListener( SWT.Selection, this::ok );

    Button wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
    wCancel.addListener( SWT.Selection, this::cancel );

    Button wTest = new Button( shell, SWT.PUSH );
    wTest.setText( BaseMessages.getString( PKG, "System.Button.Test" ) );
    wTest.addListener( SWT.Selection, this::test );

    Button[] buttons = new Button[] { wOK, wTest, wCancel };
    BaseStepDialog.positionBottomButtons( shell, buttons, margin, null );

    // Now create the tabs above the buttons...

    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );

    addGeneralTab();
    addAdvancedTab();
    addOptionsTab();

    getData();

    wConnectionType.addModifyListener( e -> changeConnectionType() );

    // Select the general tab
    //
    wTabFolder.setSelection( 0 );
    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( 0, 0 );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    fdTabFolder.bottom = new FormAttachment( wOK, -margin * 3 );
    wTabFolder.setLayoutData( fdTabFolder );

    BaseStepDialog.setSize( shell );

    shell.open();
    Display display = parent.getDisplay();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return returnValue;
  }

  private void addGeneralTab() {

    wGeneralTab = new CTabItem( wTabFolder, SWT.NONE );
    wGeneralTab.setText( "   " + BaseMessages.getString( PKG, "DatabaseDialog.DbTab.title" ) + "   " );

    wGeneralComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wGeneralComp );

    FormLayout genLayout = new FormLayout();
    genLayout.marginWidth = Const.FORM_MARGIN * 2;
    genLayout.marginHeight = Const.FORM_MARGIN * 2;
    wGeneralComp.setLayout( genLayout );

    // What's the name
    //
    Label wlName = new Label( wGeneralComp, SWT.RIGHT );
    props.setLook( wlName );
    wlName.setText( BaseMessages.getString( PKG, "DatabaseDialog.label.ConnectionName" ) );
    FormData fdlName = new FormData();
    fdlName.top = new FormAttachment( 0, 0 );
    fdlName.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlName.right = new FormAttachment( middle, 0 );
    wlName.setLayoutData( fdlName );
    wName = new Text( wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wName );
    FormData fdName = new FormData();
    fdName.top = new FormAttachment( wlName, 0, SWT.CENTER );
    fdName.left = new FormAttachment( middle, margin ); // To the right of the label
    fdName.right = new FormAttachment( 100, 0 );
    wName.setLayoutData( fdName );
    Control lastControl = wName;

    // What's the type of database access?
    //
    Label wlConnectionType = new Label( wGeneralComp, SWT.RIGHT );
    props.setLook( wlConnectionType );
    wlConnectionType.setText( BaseMessages.getString( PKG, "DatabaseDialog.label.ConnectionType" ) );
    FormData fdlConnectionType = new FormData();
    fdlConnectionType.top = new FormAttachment( lastControl, margin );
    fdlConnectionType.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlConnectionType.right = new FormAttachment( middle, 0 );
    wlConnectionType.setLayoutData( fdlConnectionType );
    wConnectionType = new ComboVar( databaseMeta, wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wConnectionType );
    wConnectionType.setItems( getConnectionTypes() );
    FormData fdConnectionType = new FormData();
    fdConnectionType.top = new FormAttachment( wlConnectionType, 0, SWT.CENTER );
    fdConnectionType.left = new FormAttachment( middle, margin ); // To the right of the label
    fdConnectionType.right = new FormAttachment( 100, 0 );
    wConnectionType.setLayoutData( fdConnectionType );
    lastControl = wConnectionType;


    // What's the type of database connection?
    //
    Label wlODBC = new Label( wGeneralComp, SWT.RIGHT );
    props.setLook( wlODBC );
    wlODBC.setText( BaseMessages.getString( PKG, "DatabaseDialog.label.ODBC" ) );
    FormData fdlODBC = new FormData();
    fdlODBC.top = new FormAttachment( lastControl, margin * 2 );
    fdlODBC.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlODBC.right = new FormAttachment( middle, 0 );
    wlODBC.setLayoutData( fdlODBC );
    wODBC = new Button( wGeneralComp, SWT.CHECK | SWT.LEFT );
    props.setLook( wODBC );
    FormData fdODBC = new FormData();
    fdODBC.top = new FormAttachment( wlODBC, 0, SWT.CENTER );
    fdODBC.left = new FormAttachment( middle, margin ); // To the right of the label
    fdODBC.right = new FormAttachment( 100, 0 );
    wODBC.setLayoutData( fdODBC );
    lastControl = wODBC;

    // What's the ODBC DSN Name
    //
    wlOdbcDsn = new Label( wGeneralComp, SWT.RIGHT );
    props.setLook( wlOdbcDsn );
    wlOdbcDsn.setText( BaseMessages.getString( PKG, "DatabaseDialog.label.OdbcDsn" ) );
    FormData fdlOdbcDsn = new FormData();
    fdlOdbcDsn.top = new FormAttachment( lastControl, margin * 2 );
    fdlOdbcDsn.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlOdbcDsn.right = new FormAttachment( middle, 0 );
    wlOdbcDsn.setLayoutData( fdlOdbcDsn );
    wOdbcDsn = new TextVar( databaseMeta, wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wOdbcDsn );
    FormData fdOdbcDsn = new FormData();
    fdOdbcDsn.top = new FormAttachment( wlOdbcDsn, 0, SWT.CENTER );
    fdOdbcDsn.left = new FormAttachment( middle, margin ); // To the right of the label
    fdOdbcDsn.right = new FormAttachment( 100, 0 );
    wOdbcDsn.setLayoutData( fdOdbcDsn );
    lastControl = wOdbcDsn;

    wlManualUrl = new Label( wGeneralComp, SWT.RIGHT );
    props.setLook( wlManualUrl );
    wlManualUrl.setText( BaseMessages.getString( PKG, "DatabaseDialog.label.ManualUrl" ) );
    FormData fdlManualUrl = new FormData();
    fdlManualUrl.bottom = new FormAttachment( 100, -margin ); // At the bottom of this tab
    fdlManualUrl.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlManualUrl.right = new FormAttachment( middle, 0 );
    wlManualUrl.setLayoutData( fdlManualUrl );
    wManualUrl = new TextVar( databaseMeta, wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wManualUrl );
    FormData fdManualUrl = new FormData();
    fdManualUrl.top = new FormAttachment( wlManualUrl, 0, SWT.CENTER );
    fdManualUrl.left = new FormAttachment( middle, margin ); // To the right of the label
    fdManualUrl.right = new FormAttachment( 100, 0 );
    wManualUrl.setLayoutData( fdManualUrl );
    wManualUrl.addModifyListener( e -> {
      enableFields();
    } );

    Label wlPassword = new Label( wGeneralComp, SWT.RIGHT );
    props.setLook( wlPassword );
    wlPassword.setText( BaseMessages.getString( PKG, "DatabaseDialog.label.Password" ) );
    FormData fdlPassword = new FormData();
    fdlPassword.bottom = new FormAttachment( wManualUrl, -margin * 5 ); // At the bottom of this tab
    fdlPassword.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlPassword.right = new FormAttachment( middle, 0 );
    wlPassword.setLayoutData( fdlPassword );
    wPassword = new TextVar( databaseMeta, wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wPassword.setEchoChar( '*' );
    props.setLook( wPassword );
    FormData fdPassword = new FormData();
    fdPassword.top = new FormAttachment( wlPassword, 0, SWT.CENTER );
    fdPassword.left = new FormAttachment( middle, margin ); // To the right of the label
    fdPassword.right = new FormAttachment( 100, 0 );
    wPassword.setLayoutData( fdPassword );

    Label wlUsername = new Label( wGeneralComp, SWT.RIGHT );
    props.setLook( wlUsername );
    wlUsername.setText( BaseMessages.getString( PKG, "DatabaseDialog.label.Username" ) );
    FormData fdlUsername = new FormData();
    fdlUsername.bottom = new FormAttachment( wPassword, -margin ); // At the bottom of this tab
    fdlUsername.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlUsername.right = new FormAttachment( middle, 0 );
    wlUsername.setLayoutData( fdlUsername );
    wUsername = new TextVar( databaseMeta, wGeneralComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wUsername );
    FormData fdUsername = new FormData();
    fdUsername.top = new FormAttachment( wlUsername, 0, SWT.CENTER );
    fdUsername.left = new FormAttachment( middle, margin ); // To the right of the label
    fdUsername.right = new FormAttachment( 100, 0 );
    wUsername.setLayoutData( fdUsername );

    wODBC.addListener( SWT.Selection, event -> enableFields() );

    // Add a composite area
    //
    wDatabaseSpecificComp = new Composite( wGeneralComp, SWT.BACKGROUND );
    props.setLook( wDatabaseSpecificComp );
    wDatabaseSpecificComp.setLayout( new FormLayout() );
    FormData fdDatabaseSpecificComp = new FormData();
    fdDatabaseSpecificComp.left = new FormAttachment( 0, 0 );
    fdDatabaseSpecificComp.right = new FormAttachment( 100, 0 );
    fdDatabaseSpecificComp.top = new FormAttachment( lastControl, 3 * margin );
    fdDatabaseSpecificComp.bottom = new FormAttachment( wUsername, -150 ); // Take a large margin to have room when switching types
    wDatabaseSpecificComp.setLayoutData( fdDatabaseSpecificComp );

    // Now add the database plugin specific widgets
    //
    guiElementWidgets = new GuiElementWidgets( databaseMeta );
    guiElementWidgets.createWidgets( workingMeta.getDatabaseInterface(), wDatabaseSpecificComp, DatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID, null );

    fdGeneralComp = new FormData();
    fdGeneralComp.left = new FormAttachment( 0, 0 );
    fdGeneralComp.top = new FormAttachment( 0, 0 );
    fdGeneralComp.right = new FormAttachment( 100, 0 );
    fdGeneralComp.bottom = new FormAttachment( 100, 0 );
    wGeneralComp.setLayoutData( fdGeneralComp );

    wGeneralComp.layout();
    wGeneralTab.setControl( wGeneralComp );
  }

  private AtomicBoolean busyChangingConnectionType = new AtomicBoolean( false );

  private void changeConnectionType() {

    if ( busyChangingConnectionType.get() ) {
      return;
    }
    busyChangingConnectionType.set( true );

    // Capture any information on the widgets
    //
    getInfo( workingMeta );

    // Remove existing children
    //
    for ( Control child : wDatabaseSpecificComp.getChildren() ) {
      child.dispose();
    }

    // Re-add the widgets
    //
    guiElementWidgets = new GuiElementWidgets( databaseMeta );
    guiElementWidgets.createWidgets( workingMeta.getDatabaseInterface(), wDatabaseSpecificComp, DatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID, null );

    // Put the data back
    //
    getData();

    wGeneralComp.layout( true, true );

    busyChangingConnectionType.set( false );
  }

  private void addAdvancedTab() {

    wAdvancedTab = new CTabItem( wTabFolder, SWT.NONE );
    wAdvancedTab.setText( "   " + BaseMessages.getString( PKG, "DatabaseDialog.AdvancedTab.title" ) + "   " );

    wAdvancedComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wAdvancedComp );

    FormLayout advancedLayout = new FormLayout();
    advancedLayout.marginWidth = Const.FORM_MARGIN * 2;
    advancedLayout.marginHeight = Const.FORM_MARGIN * 2;
    wAdvancedComp.setLayout( advancedLayout );

    // Supports the Boolean data type?
    //
    Label wlSupportsBoolean = new Label( wAdvancedComp, SWT.RIGHT );
    props.setLook( wlSupportsBoolean );
    wlSupportsBoolean.setText( BaseMessages.getString( PKG, "DatabaseDialog.label.ConnectionSupportsBoolean" ) );
    FormData fdlSupportsBoolean = new FormData();
    fdlSupportsBoolean.top = new FormAttachment( 0, 0 );
    fdlSupportsBoolean.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlSupportsBoolean.right = new FormAttachment( middle, 0 );
    wlSupportsBoolean.setLayoutData( fdlSupportsBoolean );
    wSupportsBoolean = new Button( wAdvancedComp, SWT.CHECK | SWT.LEFT );
    props.setLook( wSupportsBoolean );
    FormData fdSupportsBoolean = new FormData();
    fdSupportsBoolean.top = new FormAttachment( wlSupportsBoolean, 0, SWT.CENTER );
    fdSupportsBoolean.left = new FormAttachment( middle, margin ); // To the right of the label
    fdSupportsBoolean.right = new FormAttachment( 100, 0 );
    wSupportsBoolean.setLayoutData( fdSupportsBoolean );
    Control lastControl = wSupportsBoolean;

    // Supports the Timestamp data type?
    //
    Label wlSupportsTimestamp = new Label( wAdvancedComp, SWT.RIGHT );
    props.setLook( wlSupportsTimestamp );
    wlSupportsTimestamp.setText( BaseMessages.getString( PKG, "DatabaseDialog.label.ConnectionSupportsTimestamp" ) );
    FormData fdlSupportsTimestamp = new FormData();
    fdlSupportsTimestamp.top = new FormAttachment( lastControl, margin );
    fdlSupportsTimestamp.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlSupportsTimestamp.right = new FormAttachment( middle, 0 );
    wlSupportsTimestamp.setLayoutData( fdlSupportsTimestamp );
    wSupportsTimestamp = new Button( wAdvancedComp, SWT.CHECK | SWT.LEFT );
    props.setLook( wSupportsTimestamp );
    FormData fdSupportsTimestamp = new FormData();
    fdSupportsTimestamp.top = new FormAttachment( wlSupportsTimestamp, 0, SWT.CENTER );
    fdSupportsTimestamp.left = new FormAttachment( middle, margin ); // To the right of the label
    fdSupportsTimestamp.right = new FormAttachment( 100, 0 );
    wSupportsTimestamp.setLayoutData( fdSupportsTimestamp );
    lastControl = wSupportsTimestamp;

    // Quote all in database?
    //
    Label wlQuoteAll = new Label( wAdvancedComp, SWT.RIGHT );
    props.setLook( wlQuoteAll );
    wlQuoteAll.setText( BaseMessages.getString( PKG, "DatabaseDialog.label.AdvancedQuoteAllFields" ) );
    FormData fdlQuoteAll = new FormData();
    fdlQuoteAll.top = new FormAttachment( lastControl, margin );
    fdlQuoteAll.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlQuoteAll.right = new FormAttachment( middle, 0 );
    wlQuoteAll.setLayoutData( fdlQuoteAll );
    wQuoteAll = new Button( wAdvancedComp, SWT.CHECK | SWT.LEFT );
    props.setLook( wQuoteAll );
    FormData fdQuoteAll = new FormData();
    fdQuoteAll.top = new FormAttachment( wlQuoteAll, 0, SWT.CENTER );
    fdQuoteAll.left = new FormAttachment( middle, margin ); // To the right of the label
    fdQuoteAll.right = new FormAttachment( 100, 0 );
    wQuoteAll.setLayoutData( fdQuoteAll );
    lastControl = wQuoteAll;

    // Force all identifiers to lowercase?
    //
    Label wlForceLowercase = new Label( wAdvancedComp, SWT.RIGHT );
    props.setLook( wlForceLowercase );
    wlForceLowercase.setText( BaseMessages.getString( PKG, "DatabaseDialog.label.AdvancedForceIdentifiersLowerCase" ) );
    FormData fdlForceLowercase = new FormData();
    fdlForceLowercase.top = new FormAttachment( lastControl, margin );
    fdlForceLowercase.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlForceLowercase.right = new FormAttachment( middle, 0 );
    wlForceLowercase.setLayoutData( fdlForceLowercase );
    wForceLowercase = new Button( wAdvancedComp, SWT.CHECK | SWT.LEFT );
    props.setLook( wForceLowercase );
    FormData fdForceLowercase = new FormData();
    fdForceLowercase.top = new FormAttachment( wlForceLowercase, 0, SWT.CENTER );
    fdForceLowercase.left = new FormAttachment( middle, margin ); // To the right of the label
    fdForceLowercase.right = new FormAttachment( 100, 0 );
    wForceLowercase.setLayoutData( fdForceLowercase );
    lastControl = wForceLowercase;

    // Force all identifiers to uppercase?
    //
    Label wlForceUppercase = new Label( wAdvancedComp, SWT.RIGHT );
    props.setLook( wlForceUppercase );
    wlForceUppercase.setText( BaseMessages.getString( PKG, "DatabaseDialog.label.AdvancedForceIdentifiersUpperCase" ) );
    FormData fdlForceUppercase = new FormData();
    fdlForceUppercase.top = new FormAttachment( lastControl, margin );
    fdlForceUppercase.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlForceUppercase.right = new FormAttachment( middle, 0 );
    wlForceUppercase.setLayoutData( fdlForceUppercase );
    wForceUppercase = new Button( wAdvancedComp, SWT.CHECK | SWT.LEFT );
    props.setLook( wForceUppercase );
    FormData fdForceUppercase = new FormData();
    fdForceUppercase.top = new FormAttachment( wlForceUppercase, 0, SWT.CENTER );
    fdForceUppercase.left = new FormAttachment( middle, margin ); // To the right of the label
    fdForceUppercase.right = new FormAttachment( 100, 0 );
    wForceUppercase.setLayoutData( fdForceUppercase );
    lastControl = wForceUppercase;

    // Preserve case of reserved keywords?
    //
    Label wlPreserveCase = new Label( wAdvancedComp, SWT.RIGHT );
    props.setLook( wlPreserveCase );
    wlPreserveCase.setText( BaseMessages.getString( PKG, "DatabaseDialog.label.ConnectionPreserveCase" ) );
    FormData fdlPreserveCase = new FormData();
    fdlPreserveCase.top = new FormAttachment( lastControl, margin );
    fdlPreserveCase.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlPreserveCase.right = new FormAttachment( middle, 0 );
    wlPreserveCase.setLayoutData( fdlPreserveCase );
    wPreserveCase = new Button( wAdvancedComp, SWT.CHECK | SWT.LEFT );
    props.setLook( wPreserveCase );
    FormData fdPreserveCase = new FormData();
    fdPreserveCase.top = new FormAttachment( wlPreserveCase, 0, SWT.CENTER );
    fdPreserveCase.left = new FormAttachment( middle, margin ); // To the right of the label
    fdPreserveCase.right = new FormAttachment( 100, 0 );
    wPreserveCase.setLayoutData( fdPreserveCase );
    lastControl = wPreserveCase;

    // The preferred schema to use
    //
    Label wlPreferredSchema = new Label( wAdvancedComp, SWT.RIGHT );
    props.setLook( wlPreferredSchema );
    wlPreferredSchema.setText( BaseMessages.getString( PKG, "DatabaseDialog.label.PreferredSchemaName" ) );
    FormData fdlPreferredSchema = new FormData();
    fdlPreferredSchema.top = new FormAttachment( lastControl, margin );
    fdlPreferredSchema.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlPreferredSchema.right = new FormAttachment( middle, 0 );
    wlPreferredSchema.setLayoutData( fdlPreferredSchema );
    wPreferredSchema = new TextVar( databaseMeta, wAdvancedComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wPreferredSchema );
    FormData fdPreferredSchema = new FormData();
    fdPreferredSchema.top = new FormAttachment( wlPreferredSchema, 0, SWT.CENTER );
    fdPreferredSchema.left = new FormAttachment( middle, margin ); // To the right of the label
    fdPreferredSchema.right = new FormAttachment( 100, 0 );
    wPreferredSchema.setLayoutData( fdPreferredSchema );
    lastControl = wPreferredSchema;

    // SQL Statements to run after connecting
    //
    Label wlSQLStatements = new Label( wAdvancedComp, SWT.LEFT );
    props.setLook( wlSQLStatements );
    wlSQLStatements.setText( BaseMessages.getString( PKG, "DatabaseDialog.label.ConnectionSQLStatements" ) );
    FormData fdlSQLStatements = new FormData();
    fdlSQLStatements.top = new FormAttachment( lastControl, margin );
    fdlSQLStatements.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlSQLStatements.right = new FormAttachment( 100, 0 );
    wlSQLStatements.setLayoutData( fdlSQLStatements );
    wSQLStatements = new TextVar( databaseMeta, wAdvancedComp, SWT.MULTI | SWT.LEFT | SWT.BORDER );
    props.setLook( wSQLStatements );
    FormData fdSQLStatements = new FormData();
    fdSQLStatements.top = new FormAttachment( wlSQLStatements, margin );
    fdSQLStatements.bottom = new FormAttachment( 100, 0 );
    fdSQLStatements.left = new FormAttachment( 0, 0 ); // To the right of the label
    fdSQLStatements.right = new FormAttachment( 100, 0 );
    wSQLStatements.setLayoutData( fdSQLStatements );
    // lastControl = wSQLStatements;

    fdAdvancedComp = new FormData();
    fdAdvancedComp.left = new FormAttachment( 0, 0 );
    fdAdvancedComp.top = new FormAttachment( 0, 0 );
    fdAdvancedComp.right = new FormAttachment( 100, 0 );
    fdAdvancedComp.bottom = new FormAttachment( 100, 0 );
    wAdvancedComp.setLayoutData( fdAdvancedComp );

    wAdvancedComp.layout();
    wAdvancedTab.setControl( wAdvancedComp );
  }

  private void addOptionsTab() {

    wOptionsTab = new CTabItem( wTabFolder, SWT.NONE );
    wOptionsTab.setText( "   " + BaseMessages.getString( PKG, "DatabaseDialog.OptionsTab.title" ) + "   " );

    wOptionsComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wOptionsComp );

    FormLayout optionsLayout = new FormLayout();
    optionsLayout.marginWidth = Const.FORM_MARGIN * 2;
    optionsLayout.marginHeight = Const.FORM_MARGIN * 2;
    wOptionsComp.setLayout( optionsLayout );

    ColumnInfo[] optionsColumns = new ColumnInfo[] {
      new ColumnInfo( BaseMessages.getString( PKG, "DatabaseDialog.column.Parameter" ), ColumnInfo.COLUMN_TYPE_TEXT, false ),
      new ColumnInfo( BaseMessages.getString( PKG, "DatabaseDialog.column.Value" ), ColumnInfo.COLUMN_TYPE_TEXT, false ),
    };
    optionsColumns[ 0 ].setUsingVariables( true );
    optionsColumns[ 1 ].setUsingVariables( true );

    // Options?
    //
    Label wlOptions = new Label( wOptionsComp, SWT.LEFT );
    props.setLook( wlOptions );
    wlOptions.setText( BaseMessages.getString( PKG, "DatabaseDialog.label.Options" ) );
    FormData fdlOptions = new FormData();
    fdlOptions.top = new FormAttachment( 0, 0 );
    fdlOptions.left = new FormAttachment( 0, 0 ); // First one in the left top corner
    fdlOptions.right = new FormAttachment( 100, 0 );
    wlOptions.setLayoutData( fdlOptions );
    wOptions = new TableView( databaseMeta, wOptionsComp, SWT.NONE, optionsColumns, workingMeta.getExtraOptions().size(), null, props );
    props.setLook( wOptions );
    FormData fdOptions = new FormData();
    fdOptions.top = new FormAttachment( wlOptions, margin * 2 );
    fdOptions.bottom = new FormAttachment( 100, 0 );
    fdOptions.left = new FormAttachment( 0, 0 ); // To the right of the label
    fdOptions.right = new FormAttachment( 100, 0 );
    wOptions.setLayoutData( fdOptions );

    fdOptionsComp = new FormData();
    fdOptionsComp.left = new FormAttachment( 0, 0 );
    fdOptionsComp.top = new FormAttachment( 0, 0 );
    fdOptionsComp.right = new FormAttachment( 100, 0 );
    fdOptionsComp.bottom = new FormAttachment( 100, 0 );
    wOptionsComp.setLayoutData( fdOptionsComp );

    wOptionsComp.layout();
    wOptionsTab.setControl( wOptionsComp );
  }

  private void enableFields() {
    boolean odbc = wODBC.getSelection();
    boolean manualUrl = StringUtils.isNotEmpty( wManualUrl.getText() ) && StringUtils.isNotBlank( wManualUrl.getText() );

    wlOdbcDsn.setEnabled( odbc );
    wOdbcDsn.setEnabled( odbc );

    wlManualUrl.setEnabled( !odbc );
    wManualUrl.setEnabled( !odbc );

    // Also enable/disable the custom native fields
    //
    guiElementWidgets.enableWidgets( workingMeta.getDatabaseInterface(), DatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID, !odbc && !manualUrl );
  }

  private void ok( Event event ) {
    getInfo( databaseMeta );
    returnValue = databaseMeta.getName();
    dispose();
  }

  private void cancel( Event event ) {
    dispose();
  }

  private void dispose() {
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }

  private void test( Event event ) {
    testConnection( shell, getInfo( new DatabaseMeta() ) );
  }

  /**
   * Copy data from the metadata into the dialog.
   */
  private void getData() {

    System.out.println( "DMD getData()  START" );
    wName.setText( Const.NVL( workingMeta.getName(), "" ) );
    wODBC.setSelection( workingMeta.getAccessType() == DatabaseMeta.TYPE_ACCESS_ODBC );
    wConnectionType.setText( Const.NVL( workingMeta.getPluginName(), "" ) );

    wUsername.setText( Const.NVL( workingMeta.getUsername(), "" ) );
    wPassword.setText( Const.NVL( workingMeta.getPassword(), "" ) );

    System.out.println( "DMD getData()  getWidgetsContents" );
    guiElementWidgets.setWidgetsContents( workingMeta.getDatabaseInterface(), wDatabaseSpecificComp, DatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID );

    wSupportsBoolean.setSelection( workingMeta.supportsBooleanDataType() );
    wSupportsTimestamp.setSelection( workingMeta.supportsTimestampDataType() );
    wQuoteAll.setSelection( workingMeta.isQuoteAllFields() );
    wForceLowercase.setSelection( workingMeta.isForcingIdentifiersToLowerCase() );
    wForceUppercase.setSelection( workingMeta.isForcingIdentifiersToUpperCase() );
    wPreserveCase.setSelection( workingMeta.preserveReservedCase() );
    wPreferredSchema.setText( Const.NVL( workingMeta.getPreferredSchemaName(), "" ) );
    wSQLStatements.setText( Const.NVL( workingMeta.getConnectSQL(), "" ) );

    wOptions.clearAll( false );
    Map<String, String> optionsMap = workingMeta.getExtraOptionsMap();
    List<String> options = new ArrayList<>( optionsMap.keySet() );
    Collections.sort( options );
    for ( String option : options ) {
      String value = optionsMap.get( option );
      TableItem item = new TableItem( wOptions.table, SWT.NONE );
      item.setText( 1, Const.NVL( option, "" ) );
      item.setText( 2, Const.NVL( value, "" ) );
    }
    wOptions.removeEmptyRows();
    wOptions.setRowNums();
    wOptions.optWidth( true );

    enableFields();

    System.out.println( "DMD getData()  END" );
  }

  private DatabaseMeta getInfo( DatabaseMeta meta ) {

    meta.setName( wName.getText() );
    meta.setDatabaseType( wConnectionType.getText() );
    meta.setAccessType( wODBC.getSelection() ? DatabaseMeta.TYPE_ACCESS_ODBC : DatabaseMeta.TYPE_ACCESS_NATIVE );
    meta.setOdbcDsn( wOdbcDsn.getText() );
    meta.setManualUrl( wManualUrl.getText() );
    meta.setUsername( wUsername.getText() );
    meta.setPassword( wPassword.getText() );

    meta.setSupportsBooleanDataType( wSupportsBoolean.getSelection() );
    meta.setSupportsTimestampDataType( wSupportsTimestamp.getSelection() );
    meta.setQuoteAllFields( wQuoteAll.getSelection() );
    meta.setForcingIdentifiersToLowerCase( wForceLowercase.getSelection() );
    meta.setForcingIdentifiersToUpperCase( wForceUppercase.getSelection() );
    meta.setPreserveReservedCase( wPreserveCase.getSelection() );
    meta.setPreferredSchemaName( wPreferredSchema.getText() );
    meta.setConnectSQL( wSQLStatements.getText() );

    meta.getExtraOptions().clear();
    for ( int i = 0; i < wOptions.nrNonEmpty(); i++ ) {
      TableItem item = wOptions.getNonEmpty( i );
      String option = item.getText( 1 );
      String value = item.getText( 2 );
      meta.addExtraOption( meta.getPluginId(), option, value );
    }

    // Finally, get the database specific information
    //
    guiElementWidgets.getWidgetsContents( meta.getDatabaseInterface(), DatabaseMeta.GUI_PLUGIN_ELEMENT_PARENT_ID );

    return meta;
  }


  /**
   * Test the database connection
   */
  public static final void testConnection( Shell shell, DatabaseMeta databaseMeta ) {
    String[] remarks = databaseMeta.checkParameters();
    if ( remarks.length == 0 ) {
      // Get a "test" report from this database
      DatabaseTestResults databaseTestResults = databaseMeta.testConnectionSuccess();
      String message = databaseTestResults.getMessage();
      boolean success = databaseTestResults.isSuccess();
      String title = success ? BaseMessages.getString( PKG, "DatabaseDialog.DatabaseConnectionTestSuccess.title" )
        : BaseMessages.getString( PKG, "DatabaseDialog.DatabaseConnectionTest.title" );
      if ( success && message.contains( Const.CR ) ) {
        message = message.substring( 0, message.indexOf( Const.CR ) )
          + Const.CR + message.substring( message.indexOf( Const.CR ) );
        message = message.substring( 0, message.lastIndexOf( Const.CR ) );
      }
      ShowMessageDialog msgDialog = new ShowMessageDialog( shell, SWT.ICON_INFORMATION | SWT.OK,
        title, message, message.length() > 300 );
      msgDialog.setType( success ? Const.SHOW_MESSAGE_DIALOG_DB_TEST_SUCCESS
        : Const.SHOW_MESSAGE_DIALOG_DB_TEST_DEFAULT );
      msgDialog.open();
    } else {
      String message = "";
      for ( int i = 0; i < remarks.length; i++ ) {
        message += "    * " + remarks[ i ] + Const.CR;
      }

      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setText( BaseMessages.getString( PKG, "DatabaseDialog.ErrorParameters2.title" ) );
      mb.setMessage( BaseMessages.getString( PKG, "DatabaseDialog.ErrorParameters2.description", message ) );
      mb.open();
    }
  }

  private String[] getConnectionTypes() {
    PluginRegistry registry = PluginRegistry.getInstance();
    List<PluginInterface> plugins = registry.getPlugins( DatabasePluginType.class );
    String[] types = new String[ plugins.size() ];
    for ( int i = 0; i < types.length; i++ ) {
      types[ i ] = plugins.get( i ).getName();
    }
    Arrays.sort( types );
    return types;
  }

  /**
   * Gets databaseMeta
   *
   * @return value of databaseMeta
   */
  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  /**
   * @param databaseMeta The databaseMeta to set
   */
  public void setDatabaseMeta( DatabaseMeta databaseMeta ) {
    this.databaseMeta = databaseMeta;
  }

  public static void main( String[] args ) throws HopException {
    System.out.println( ">>>>>>>>>>>>>>>> DatabaseMetaDialog START" );
    Display display = new Display();
    Shell shell = new Shell( display, SWT.MIN | SWT.MAX | SWT.RESIZE );
    // shell.setSize( 500, 500 );
    // shell.open();

    System.out.println( ">>>>>>>>>>>>>>>> Main shell opened" );

    HopClientEnvironment.init();
    System.out.println( ">>>>>>>>>>>>>>>> Hop client environment initialized" );

    List<PluginInterface> plugins = PluginRegistry.getInstance().getPlugins( DatabasePluginType.class );
    System.out.println( ">>>>>>>>>>>>>>>> Nr of database plugins found: " + plugins.size() );

    PropsUI.init( display );
    System.out.println( ">>>>>>>>>>>>>>>> PropsUI initialized" );

    HopEnvironment.init();
    System.out.println( ">>>>>>>>>>>>>>>> Hop Environment initialized" );

    DatabaseMeta databaseMeta = new DatabaseMeta( "Test", "MYSQL", "Native", "localhost", "samples", "3306", "username", "password" );

    System.out.println( ">>>>>>>>>>>>>>>> DatabaseMetaDialog created" );
    DatabaseMetaDialog dialog = new DatabaseMetaDialog( shell, null, databaseMeta );
    System.out.println( ">>>>>>>>>>>>>>>> DatabaseMetaDialog created" );
    String name = dialog.open();
    System.out.println( ">>>>>>>>>>>>>>>> DatabaseMetaDialog opened, name=" + name );

    System.out.println( ">>>>>>>>>>>>>>>> DatabaseMetaDialog hostname = " + databaseMeta.getHostname() );
    System.out.println( ">>>>>>>>>>>>>>>> DatabaseMetaDialog port     = " + databaseMeta.getPort() );
    System.out.println( ">>>>>>>>>>>>>>>> DatabaseMetaDialog db name  = " + databaseMeta.getDatabaseName() );
    System.out.println( ">>>>>>>>>>>>>>>> DatabaseMetaDialog username = " + databaseMeta.getUsername() );
    System.out.println( ">>>>>>>>>>>>>>>> DatabaseMetaDialog password = " + databaseMeta.getPassword() );

    // Re-open with a new dialog...
    //
    DatabaseMetaDialog newDialog = new DatabaseMetaDialog( shell, null, databaseMeta );
    newDialog.open();

    // while ( shell != null && !shell.isDisposed() ) {
    //   if ( !display.readAndDispatch() ) {
    //    display.sleep();
    //  }
    // }
    display.dispose();
  }
}
