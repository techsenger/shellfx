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

package com.techsenger.tabshell.demo.page;

import com.techsenger.patternfx.core.HistoryProvider;
import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.dialog.AbstractDialogPresenter;
import com.techsenger.tabshell.demo.DemoComponents;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class PageDialogPresenter extends AbstractDialogPresenter<PageDialogView> {

    public PageDialogPresenter(PageDialogView view, HistoryProvider<PageDialogHistory> hp,
            PageMenuType menuType) {
        super(view);
        setHistoryProvider(hp);
        getView().getComposer().setMenuType(menuType);
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(DemoComponents.PAGE_DIALOG);
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
    protected void postInitialize() {
        super.postInitialize();
        getView().setTitle("Page Dialog");
        setResultAction((button) -> requestClose());
        setRightButtons(PageDialogButtons.OK);
        setButtonDefault(PageDialogButtons.OK, true);
    }

    @Override
    protected PageDialogHistory getHistory() {
        return (PageDialogHistory) super.getHistory();
    }
}
