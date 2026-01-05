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

package com.techsenger.tabshell.demos.full.dock;

import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.history.HistoryManager;
import com.techsenger.tabshell.core.tab.AbstractShellTabViewModel;
import com.techsenger.tabshell.core.tab.ShellTabMediator;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class DockLayoutTabViewModel extends AbstractShellTabViewModel<ShellTabMediator> {

    public DockLayoutTabViewModel(HistoryManager historyManager) {
        super();
        setTitle("Dock Layout Tab");
        setHistoryPolicy(HistoryPolicy.ALL);
        setHistoryProvider(() -> historyManager
                .getOrCreateHistory(DockLayoutTabHistory.class, DockLayoutTabHistory::new));
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected DockLayoutTabHistory getHistory() {
        return (DockLayoutTabHistory) super.getHistory();
    }
}
