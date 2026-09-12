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

import com.techsenger.shellfx.core.MenuAwarePort;
import com.techsenger.shellfx.core.area.AbstractAreaViewModel;
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.popup.OverlayScope;
import com.techsenger.shellfx.core.settings.AppearanceSettings;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.demo.main.DemoMenuAwarePort;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import com.techsenger.shellfx.demo.dialog.DemoDailogResultButtons;

/**
 *
 * @author Pavel Castornii
 */
public class MenuAwareAreaViewModel<C extends MenuAwareAreaComposer> extends AbstractAreaViewModel<C>
        implements MenuAwarePort, DemoMenuAwarePort {

    private final BooleanProperty fooDisabled = new SimpleBooleanProperty();

    private final BooleanProperty barIncluded = new SimpleBooleanProperty();

    private final BooleanProperty barDisabled = new SimpleBooleanProperty();

    private final AppearanceSettings settings;

    public MenuAwareAreaViewModel(MenuAwareAreaParams params) {
        super(params);
        this.settings = params.getSettings();
    }

    @Override
    public boolean isFooDisabled() {
        return fooDisabled.get();
    }

    public void setFooDisabled(boolean fooDisabled) {
        this.fooDisabled.set(fooDisabled);
    }

    @Override
    public BooleanProperty fooDisabledProperty() {
        return fooDisabled;
    }

    @Override
    public boolean isBarIncluded() {
        return barIncluded.get();
    }

    public void setBarIncluded(boolean barIncluded) {
        this.barIncluded.set(barIncluded);
    }

    @Override
    public BooleanProperty barIncludedProperty() {
        return barIncluded;
    }

    @Override
    public boolean isBarDisabled() {
        return barDisabled.get();
    }

    public void setBarDisabled(boolean barDisabled) {
        this.barDisabled.set(barDisabled);
    }

    @Override
    public BooleanProperty barDisabledProperty() {
        return barDisabled;
    }

    protected void onDialogOpen(OverlayScope scope) {
        var params = new DialogParams(WindowType.NESTED, settings);
        var dialog = getComposer().openDemoDialog(scope, true, params);
        dialog.setOnResult((name) -> {
            if (name == DemoDailogResultButtons.OK) {
                dialog.closeSafely();
            }
        });
    }

    protected void onPopupOpen(OverlayScope scope) {
        getComposer().openDemoPopup(scope);
    }
}
