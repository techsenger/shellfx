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

package com.techsenger.tabshell.demo.browser;

import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.tabshell.core.MenuAwarePort;
import com.techsenger.tabshell.core.area.AbstractAreaPresenter;
import com.techsenger.tabshell.core.area.AreaParams;
import com.techsenger.tabshell.core.popup.OverlayScope;
import com.techsenger.tabshell.demo.DemoComponents;
import com.techsenger.tabshell.demo.dialogs.DemoResultButtons;
import com.techsenger.tabshell.demo.main.DemoMenuAwarePort;

/**
 *
 * @author Pavel Castornii
 */
public class MenuAwareAreaPresenter extends AbstractAreaPresenter<MenuAwareAreaView>
        implements MenuAwarePort, DemoMenuAwarePort {

    private boolean fooDisabled;

    private boolean barIncluded;

    private boolean barDisabled;

    public MenuAwareAreaPresenter(MenuAwareAreaView view) {
        super(view, new AreaParams());
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
    protected ComponentDescriptor createDescriptor() {
        return new ComponentDescriptor(DemoComponents.MENU_AWARE_AREA);
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

    protected void onDialogOpen(OverlayScope scope) {
        var dialog = getView().getComposer().openDemoDialog(scope, true);
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
