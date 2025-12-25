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

package com.techsenger.tabshell.demos.full.hex;

import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.hex.editor.HexEditorTabHistory;
import com.techsenger.tabshell.hex.editor.HexEditorTabViewModel;
import com.techsenger.tabshell.storage.GenericFile;

/**
 *
 * @author Pavel Castornii
 */
public class DemoHexEditorTabViewModel extends HexEditorTabViewModel<DemoHexEditorTabMediator> {

    public DemoHexEditorTabViewModel(GenericFile file, HistoryManager historyManager) {
        super(file, historyManager);
        setHistoryPolicy(HistoryPolicy.ALL);
        setHistoryProvider(() -> historyManager
                .getOrCreateHistory(HexEditorTabHistory.class, HexEditorTabHistory::new));
    }
}
