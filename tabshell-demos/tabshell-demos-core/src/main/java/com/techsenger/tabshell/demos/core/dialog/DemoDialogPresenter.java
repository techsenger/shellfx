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

package com.techsenger.tabshell.demos.core.dialog;

import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.dialog.AbstractDialogPresenter;
import com.techsenger.tabshell.core.dialog.DialogComposer;
import com.techsenger.tabshell.core.popup.OverlayScope;
import java.util.function.Consumer;
import com.techsenger.tabshell.demos.core.DemoComponents;

/**
 *
 * @author Pavel Castornii
 */
public class DemoDialogPresenter extends AbstractDialogPresenter<DemoDialogView, DialogComposer> {

    public DemoDialogPresenter(DemoDialogView view, OverlayScope scope) {
        super(view, scope);
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
    protected Descriptor createDescriptor() {
        return new Descriptor(DemoComponents.DEMO_DIALOG);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        var v = getView();
        v.setPrefWidth(500);
        v.setTitle("Demo Dialog");
        v.setButtonWidthEqual(true); //cancel button width will be equal to ok button width
    }

    @Override
    protected Port createPort() {
        return new AbstractDialogPresenter.Port();
    }
}
