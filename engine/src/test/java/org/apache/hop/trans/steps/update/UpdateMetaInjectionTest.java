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

package org.apache.hop.trans.steps.update;

import org.apache.hop.core.injection.BaseMetadataInjectionTest;
import org.apache.hop.junit.rules.RestoreHopEngineEnvironment;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class UpdateMetaInjectionTest extends BaseMetadataInjectionTest<UpdateMeta> {
  @ClassRule public static RestoreHopEngineEnvironment env = new RestoreHopEngineEnvironment();

  @Before
  public void setup() throws Exception {
    setup( new UpdateMeta() );
  }

  @Test
  public void test() throws Exception {
    check( "SCHEMA_NAME", new StringGetter() {
      public String get() {
        return meta.getSchemaName();
      }
    } );
    check( "TABLE_NAME", new StringGetter() {
      public String get() {
        return meta.getTableName();
      }
    } );
    check( "COMMIT_SIZE", new StringGetter() {
      public String get() {
        return meta.getCommitSizeVar();
      }
    } );
    check( "BATCH_UPDATE", new BooleanGetter() {
      public boolean get() {
        return meta.useBatchUpdate();
      }
    } );
    check( "SKIP_LOOKUP", new BooleanGetter() {
      public boolean get() {
        return meta.isSkipLookup();
      }
    } );
    check( "IGNORE_LOOKUP_FAILURE", new BooleanGetter() {
      public boolean get() {
        return meta.isErrorIgnored();
      }
    } );
    check( "FLAG_FIELD", new StringGetter() {
      public String get() {
        return meta.getIgnoreFlagField();
      }
    } );
    check( "KEY_STREAM", new StringGetter() {
      public String get() {
        return meta.getKeyStream()[ 0 ];
      }
    } );
    check( "KEY_LOOKUP", new StringGetter() {
      public String get() {
        return meta.getKeyLookup()[ 0 ];
      }
    } );
    check( "KEY_CONDITION", new StringGetter() {
      public String get() {
        return meta.getKeyCondition()[ 0 ];
      }
    } );
    check( "KEY_STREAM2", new StringGetter() {
      public String get() {
        return meta.getKeyStream2()[ 0 ];
      }
    } );
    check( "UPDATE_LOOKUP", new StringGetter() {
      public String get() {
        return meta.getUpdateLookup()[ 0 ];
      }
    } );
    check( "UPDATE_STREAM", new StringGetter() {
      public String get() {
        return meta.getUpdateStream()[ 0 ];
      }
    } );
    skipPropertyTest( "CONNECTIONNAME" );
  }
}
