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

package com.techsenger.tabshell.devtools.node;

import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.tabshell.core.dialog.DialogParams;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.settings.AppearanceSettings;
import com.techsenger.tabshell.core.window.WindowType;

/**
 *
 * @author Pavel Castornii
 */
public class EditorDialogParams extends DialogParams {

    private EditPropertyTask<?> task;

    public EditorDialogParams(WindowType windowType, AppearanceSettings settings, EditPropertyTask<?> task,
            HistoryManager historyManager) {
        super(windowType, settings);
        this.task = task;
        setHistoryPolicy(HistoryPolicy.APPEARANCE);
        setHistoryProvider(() -> historyManager.getOrCreateHistory(EditorDialogHistory.class,
                EditorDialogHistory::new));
    }

    public EditPropertyTask<?> getTask() {
        return task;
    }
}
