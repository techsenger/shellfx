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

package com.techsenger.shellfx.demo.ide;

import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.shellfx.core.CloseCheckResult;
import com.techsenger.shellfx.core.ClosePreparationResult;
import com.techsenger.shellfx.core.MenuAwarePort;
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.popup.OverlayScope;
import com.techsenger.shellfx.core.popup.PopupContainerPresenter;
import com.techsenger.shellfx.core.tab.AbstractTabPresenter;
import com.techsenger.shellfx.core.tab.TabParams;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.demo.DemoComponents;
import com.techsenger.shellfx.demo.dialogs.DemoResultButtons;
import com.techsenger.shellfx.demo.main.DemoMenuAwarePort;
import com.techsenger.shellfx.material.icon.PlainFontIcon;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class IdeMainTabPresenter<V extends IdeMainTabView> extends AbstractTabPresenter<V>
        implements MenuAwarePort, DemoMenuAwarePort, PopupContainerPresenter<V>, IdeMainTabPort {

    private boolean fooDisabled;

    private boolean barIncluded;

    private boolean barDisabled;

    public IdeMainTabPresenter(V view) {
        super(view, new TabParams());
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

    @Override
    public IdeMainTabPort.ViewAccess getViewAccess() {
        return getView();
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
        setTitle("Main Tab");
        setIcon(new PlainFontIcon(0xF04E9));
    }

    @Override
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(DemoComponents.MAIN_TAB);
    }

    protected void onDialogOpen() {
        var settings = getShellContext().getSettings().getAppearance();
        var params = new DialogParams(WindowType.NESTED, settings);
        var dialog = getView().getComposer().openDemoDialog(true, params);
        dialog.setOnResult((name) -> {
            if (name == DemoResultButtons.OK) {
                dialog.closeSafely();
            }
        });
    }

    protected void onPopupOpen(OverlayScope scope) {
        getView().getComposer().openDemoPopup(scope);
    }
}
