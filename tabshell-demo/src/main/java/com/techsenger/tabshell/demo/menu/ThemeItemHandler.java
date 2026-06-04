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

package com.techsenger.tabshell.demo.menu;

import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.menu.AbstractMenuItemHandler;
import com.techsenger.tabshell.demo.Density;
import com.techsenger.tabshell.demo.theme.ThemeDialogFxView;
import com.techsenger.tabshell.demo.theme.ThemeDialogParams;
import com.techsenger.tabshell.demo.theme.ThemeDialogPresenter;
import com.techsenger.tabshell.material.menu.ManagedMenuItem;

/**
 *
 * @author Pavel Castornii
 */
public class ThemeItemHandler extends AbstractMenuItemHandler<ShellFxView<?>> {

    public ThemeItemHandler(ManagedMenuItem item, ShellFxView<?> component) {
        super(item, component);
    }

    @Override
    public void onAction() {
        var shell = getComponent();
        var appearance = shell.getPresenter().getContext().getSettings().getAppearance();
        var view = new ThemeDialogFxView();
        view.getNode().getStyleClass().add(Density.STYLE_CLASS); // see Density javadoc
        var params = new ThemeDialogParams(appearance);
        var presenter = new ThemeDialogPresenter(view, params);
        presenter.initialize();
        shell.getComposer().addDialog(view);
    }
}
