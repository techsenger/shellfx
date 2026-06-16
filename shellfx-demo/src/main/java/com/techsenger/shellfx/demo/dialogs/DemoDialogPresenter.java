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

package com.techsenger.shellfx.demo.dialogs;

import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.shellfx.core.CloseCheckResult;
import com.techsenger.shellfx.core.ClosePreparationResult;
import com.techsenger.shellfx.core.dialog.AbstractDialogPresenter;
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.demo.DemoComponents;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class DemoDialogPresenter extends AbstractDialogPresenter<DemoDialogView> {

    public DemoDialogPresenter(DemoDialogView view, DialogParams params) {
        super(view, params);
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
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(DemoComponents.DEMO_DIALOG);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setResizable(true);
        setTitle("Demo Dialog");
        setRightButtons(DemoResultButtons.CANCEL, DemoResultButtons.OK);
    }

    @Override
    protected void applyAppearance() {
        super.applyAppearance();
        //setWidth(500);
    }


}
