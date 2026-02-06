/*
 * Copyright 2024-2026 Pavel Castornii.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.techsenger.tabshell.dialogs;

import com.techsenger.patternfx.core.ComponentName;

/**
 *
 * @author Pavel Castornii
 */
public interface DialogComponents {

    ComponentName ALERT_DIALOG = new ComponentName("AlertDialog");

    ComponentName YES_NO_DIALOG = new ComponentName("YesNoDialog");

    ComponentName FILE_CHOOSER_DIALOG = new ComponentName("FileChooserDialog");

    ComponentName NAME_VALUE_DIALOG = new ComponentName("NameValueDialog");
}
