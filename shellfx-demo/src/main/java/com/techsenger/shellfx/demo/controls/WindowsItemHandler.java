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

package com.techsenger.shellfx.demo.controls;

import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.core.window.WindowArrangement;
import com.techsenger.shellfx.core.window.WindowParams;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.demo.mdi.DemoWindowView;
import com.techsenger.shellfx.demo.mdi.DemoWindowViewModel;
import com.techsenger.shellfx.material.menu.AbstractMenuItemHandler;
import com.techsenger.shellfx.material.menu.ManagedMenuItem;
import com.techsenger.toolkit.fx.utils.NodeUtils;

/**
 *
 * @author Pavel Castornii
 */
public class WindowsItemHandler extends AbstractMenuItemHandler<ShellView<?>, ManagedMenuItem> {

    public WindowsItemHandler(ShellView<?> component, ManagedMenuItem item) {
        super(component, item);
    }

    @Override
    public void onAction() {
        DemoWindowView view = null;
        for (var i = 0; i < 6; i++) {
            var settings = getComponent().getViewModel().getContext().getSettings().getAppearance();
            var params = new WindowParams(WindowType.NESTED, false, settings);
            var viewModel = new DemoWindowViewModel<>(params, i);
            view = new DemoWindowView<>(viewModel);
            view.initialize();
            viewModel.setTitle("Window " + i);
            getComponent().getComposer().addWindow(view);
        }
        getComponent().getComposer().arrangeWindows(WindowArrangement.CASCADE);
        NodeUtils.requestFocus(view.getNode());
    }
}
