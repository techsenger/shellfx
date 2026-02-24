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

package com.techsenger.tabshell.demos.full.page;

import com.techsenger.patternfx.core.HistoryPolicy;
import com.techsenger.patternfx.core.HistoryProvider;
import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.shelltab.AbstractShellTabPresenter;
import com.techsenger.tabshell.core.shelltab.ShellTabComposer;
import com.techsenger.tabshell.demos.full.DemoComponents;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class PagedTabPresenter extends AbstractShellTabPresenter<PagedTabView, ShellTabComposer> {

    public PagedTabPresenter(PagedTabView view, HistoryProvider<PagedTabHistory> historyProvider) {
        super(view);
        setHistoryProvider(historyProvider);
        setHistoryPolicy(HistoryPolicy.ALL);
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(DemoComponents.PAGED_TAB);
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
    protected PagedTabHistory getHistory() {
        return (PagedTabHistory) super.getHistory();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        getView().setTitle("Paged Tab");
    }
}
