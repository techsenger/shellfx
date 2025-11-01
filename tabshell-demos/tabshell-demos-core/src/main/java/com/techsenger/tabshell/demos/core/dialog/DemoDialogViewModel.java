/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.demos.core.dialog;

import com.techsenger.mvvm4fx.core.ComponentDescriptor;
import com.techsenger.mvvm4fx.core.ComponentName;
import com.techsenger.tabshell.core.DefaultComponentName;
import com.techsenger.tabshell.core.dialog.AbstractDialogViewModel;
import com.techsenger.tabshell.core.dialog.DialogScope;

/**
 *
 * @author Pavel Castornii
 */
public class DemoDialogViewModel extends AbstractDialogViewModel {

    private static final ComponentName DEMO_DIALOG = new DefaultComponentName("Demo Dialog");

    public DemoDialogViewModel(DialogScope scope, boolean resizable) {
        super(scope, resizable);
        setPrefWidth(500);
        setTitle("Demo Dialog");
        setButtonWidthEqual(true); //cancel button width will be equal to ok button width
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(DEMO_DIALOG);
    }
}
