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

import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.MenuAwarePort;
import com.techsenger.tabshell.core.area.AbstractAreaPresenter;
import com.techsenger.tabshell.core.menu.MenuHelper;
import com.techsenger.tabshell.core.menu.MenuItemHelper;
import com.techsenger.tabshell.core.menu.SimpleMenuHelper;
import com.techsenger.tabshell.core.menu.SimpleMenuItemHelper;
import com.techsenger.tabshell.core.popup.OverlayScope;
import com.techsenger.tabshell.demo.DemoComponents;
import com.techsenger.tabshell.demo.dialogs.DemoResultButtons;
import com.techsenger.tabshell.demo.menu.ExtraMenu;
import com.techsenger.tabshell.material.menu.MenuItemName;
import com.techsenger.tabshell.material.menu.MenuName;

/**
 *
 * @author Pavel Castornii
 */
public class MenuAwareAreaPresenter extends AbstractAreaPresenter<MenuAwareAreaView, MenuAwareAreaComposer> {

    protected class Port extends AbstractAreaPresenter<MenuAwareAreaView, MenuAwareAreaComposer>.Port
            implements MenuAwarePort {

        private final MenuAwareAreaPresenter presenter = MenuAwareAreaPresenter.this;

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

        }

        @Override
        public void onMenuHiding(MenuName menuName) {

        }
    }

    public MenuAwareAreaPresenter(MenuAwareAreaView view) {
        super(view);
        getMenuHelpers().addAll(
                new SimpleMenuHelper(ExtraMenu.NAME, true) // extra menu is included
        );
    }

    @Override
    public Port getPort() {
        return (Port) super.getPort();
    }

    @Override
    protected Port createPort() {
        return new MenuAwareAreaPresenter.Port();
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(DemoComponents.MENU_AWARE_AREA);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        getMenuHelpers().addAll(
            new SimpleMenuItemHelper(ExtraMenu.FOO_ITEM) {
                @Override
                public Boolean getItemValid() {
                    return getView().isFooValid();
                }
            },
            new SimpleMenuItemHelper(ExtraMenu.BAR_ITEM) {
                @Override
                public Boolean getItemIncluded() {
                    return getView().isBarIncluded();
                }

                @Override
                public Boolean getItemValid() {
                    return getView().isBarValid();
                }
            }
        );
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
}
