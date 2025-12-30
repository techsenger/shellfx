/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.web;

import com.techsenger.patternfx.core.Name;
import com.techsenger.tabshell.core.ShellComponent;
import com.techsenger.tabshell.core.tab.AbstractShellTabComponent;


/**
 *
 * @author Pavel Castornii
 */
public class WebBrowserTabComponent<T extends WebBrowserTabView<?, ?>> extends AbstractShellTabComponent<T> {

    protected class Mediator extends AbstractShellTabComponent.Mediator implements WebBrowserTabMediator {

        @Override
        public WebToolBarViewModel getToolBar() {
            return toolBar.getView().getViewModel();
        }
    }

    private WebToolBarComponent<?> toolBar = createToolBar();

    public WebBrowserTabComponent(T view, ShellComponent<?> shell) {
        super(view, shell);
    }

    @Override
    public Name getName() {
        return WebComponentNames.WEB_BROWSER_TAB;
    }

    public WebToolBarComponent<?> getToolBar() {
        return toolBar;
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        getView().addToolBar(toolBar.getView().getNode());
    }

    @Override
    protected Mediator createMediator() {
        return new Mediator();
    }

    protected WebToolBarComponent<?> createToolBar() {
        var vm = new WebToolBarViewModel();
        var v = new WebToolBarView(vm, getView());
        var c = new WebToolBarComponent<>(v);
        c.initialize();
        getModifiableChildren().add(c);
        return c;
    }
}
