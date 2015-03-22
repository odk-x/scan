/*
 * Copyright (C) 2014 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.scan.android;

import org.opendatakit.common.android.application.CommonApplication;

public class Scan extends CommonApplication {

  public static final String t = "Scan";

  private static Scan singleton = null;

  public static Scan getInstance() {
    return singleton;
  }

  @Override
  public void onCreate() {
    singleton = this;

    super.onCreate();
  }

  @Override
  public int getApkDisplayNameResourceId() {
    return R.string.app_name;
  }

  @Override
  public int getAssetZipResourceId() {
    // unused -- modify InitializationTask to handle this tool
    return -1;
  }

  @Override
  public int getFrameworkZipResourceId() {
    // unused -- modify InitializationTask to handle this tool
    return -1;
  }

  @Override
  public int getWebKitResourceId() {
    return -1;
  }

}

