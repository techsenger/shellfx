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

package com.techsenger.tabshell.demo.ide;

import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.MenuAwarePort;
import com.techsenger.tabshell.core.popup.OverlayScope;
import com.techsenger.tabshell.core.tab.AbstractTabPresenter;
import com.techsenger.tabshell.demo.DemoComponents;
import com.techsenger.tabshell.demo.dialogs.DemoResultButtons;
import com.techsenger.tabshell.demo.main.DemoMenuAwarePort;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class IdeMainTabPresenter<V extends IdeMainTabView> extends AbstractTabPresenter<V>
        implements MenuAwarePort, DemoMenuAwarePort {

    private boolean fooDisabled;

    private boolean barIncluded;

    private boolean barDisabled;

    public IdeMainTabPresenter(V view) {
        super(view);
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
    public boolean isFooDisabled() {
        return fooDisabled;
    }

    @Override
    public boolean isBarIncluded() {
        return barIncluded;
    }

    @Override
    public boolean isBarDisabled() {
        return barDisabled;
    }

    protected void onFooDisabledSelected(boolean value) {
        this.fooDisabled = value;
    }

    protected void onBarDisabledSelected(boolean value) {
        this.barDisabled = value;
    }

    protected void onBarIncludedSelected(boolean value) {
        this.barIncluded = value;
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        getView().setTitle("Main Tab");
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(DemoComponents.MAIN_TAB);
    }

    protected void onDialogOpen() {
        var dialog = getView().getComposer().addDemoDialog(true);
        dialog.setResultAction((name) -> {
            if (name == DemoResultButtons.OK) {
                dialog.requestClose();
            }
        });
    }

    protected void onPopupOpen(OverlayScope scope) {
        getView().getComposer().addDemoPopup(scope);
    }
}
