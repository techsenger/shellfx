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

package com.techsenger.shellfx.demo.browser;

import com.techsenger.patternfx.mvp.ComponentDescriptor;
import com.techsenger.shellfx.core.MenuAwarePort;
import com.techsenger.shellfx.core.area.AbstractAreaPresenter;
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.popup.OverlayScope;
import com.techsenger.shellfx.core.settings.AppearanceSettings;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.demo.DemoComponents;
import com.techsenger.shellfx.demo.dialogs.DemoResultButtons;
import com.techsenger.shellfx.demo.main.DemoMenuAwarePort;

/**
 *
 * @author Pavel Castornii
 */
public class MenuAwareAreaPresenter extends AbstractAreaPresenter<MenuAwareAreaView>
        implements MenuAwarePort, DemoMenuAwarePort {

    private final AppearanceSettings settings;

    private boolean fooDisabled;

    private boolean barIncluded;

    private boolean barDisabled;

    public MenuAwareAreaPresenter(MenuAwareAreaView view, MenuAwareAreaParams params) {
        super(view, params);
        this.settings = params.getSettings();
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
        var params = new DialogParams(WindowType.NESTED, settings);
        var dialog = getView().getComposer().openDemoDialog(scope, true, params);
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
