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

package com.techsenger.tabshell.dialogs.simple;

import com.techsenger.tabshell.core.dialog.DialogView;
import com.techsenger.tabshell.material.button.ButtonName;

/**
 *
 * @author Pavel Castornii
 */
public interface SimpleDialogView extends DialogView {

    void addButtons(ButtonName... name);

    void removeButtons(ButtonName... name);

    void setButtonVisible(ButtonName name, boolean value);

    boolean isButtonVisible(ButtonName name);

    void setButtonDisabled(ButtonName name, boolean value);

    boolean isButtonDisabled(ButtonName name);

    void setButtonText(ButtonName name, String value);

    String getButtonText(ButtonName name);

    void setButtonDefault(ButtonName name, boolean value);

    boolean isButtonDefault(ButtonName name);
}
