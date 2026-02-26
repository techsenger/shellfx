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

package com.techsenger.tabshell.demo.tab;

import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.MenuAwarePort;
import com.techsenger.tabshell.core.menu.MenuHelper;
import com.techsenger.tabshell.core.menu.MenuItemHelper;
import com.techsenger.tabshell.core.menu.SimpleMenuItemHelper;
import com.techsenger.tabshell.core.popup.OverlayScope;
import com.techsenger.tabshell.core.tab.AbstractTabPresenter;
import com.techsenger.tabshell.demo.DemoComponents;
import com.techsenger.tabshell.demo.dialogs.DemoResultButtons;
import com.techsenger.tabshell.material.menu.MenuItemName;
import com.techsenger.tabshell.material.menu.MenuName;
import java.util.function.Consumer;
import com.techsenger.tabshell.demo.menu.CoreMenus;

/**
 *
 * @author Pavel Castornii
 */
public class DemoTabPresenter<V extends DemoTabView, C extends DemoTabComposer>
        extends AbstractTabPresenter<V, C> {

    protected class Port extends AbstractTabPresenter<V, C>.Port implements MenuAwarePort {

        private final DemoTabPresenter<V, C> presenter = DemoTabPresenter.this;

        @Override
        public MenuHelper getMenuHelper(MenuName menuName) {
            return presenter.getMenuHelpers().getMenuHelpersByName().get(menuName);
        }

        @Override
        public MenuItemHelper getMenuItemHelper(MenuItemName menuItemName) {
            return presenter.getMenuHelpers().getMenuItemHelpersByName().get(menuItemName);
        }

        @Override
        public void onMenuShowing(MenuName menuName) {
            // empty
        }

        @Override
        public void onMenuHiding(MenuName menuName) {
            // empty
        }
    }

    public DemoTabPresenter(V view) {
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
    public Port getPort() {
        return (Port) super.getPort();
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        getView().setTitle("Tab");
        getMenuHelpers().addAll(new SimpleMenuItemHelper(CoreMenus.NEW) {
                @Override
                public Boolean getItemValid() {
                    return getView().isNewValid();
                }
            },
            new SimpleMenuItemHelper(CoreMenus.EXIT) {
                @Override
                public Boolean getItemIncluded() {
                    return getView().isExitIncluded();
                }

                @Override
                public Boolean getItemValid() {
                    return getView().isExitValid();
                }
            }
        );
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(DemoComponents.DEMO_TAB);
    }

    protected void onDialogOpen(OverlayScope scope) {
        var dialog = getComposer().addDemoDialog(scope, true);
        dialog.setResultAction((name) -> {
            if (name == DemoResultButtons.OK) {
                dialog.requestClose();
            }
        });
    }

    protected void onPopupOpen(OverlayScope scope) {
        getComposer().addDemoPopup(scope);
    }

    @Override
    protected Port createPort() {
        return new DemoTabPresenter.Port();
    }
}
