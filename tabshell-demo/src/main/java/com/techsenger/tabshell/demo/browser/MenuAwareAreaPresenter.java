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
import com.techsenger.tabshell.core.menu.MenuDelegate;
import com.techsenger.tabshell.core.menu.MenuDelegates;
import com.techsenger.tabshell.core.menu.MenuItemDelegate;
import com.techsenger.tabshell.core.menu.SimpleMenuDelegate;
import com.techsenger.tabshell.core.menu.SimpleMenuItemDelegate;
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
public class MenuAwareAreaPresenter extends AbstractAreaPresenter<MenuAwareAreaView, MenuAwareAreaComposer>
        implements MenuAwarePort {

    private final MenuDelegates menuDelegates = new MenuDelegates();

    public MenuAwareAreaPresenter(MenuAwareAreaView view) {
        super(view);
    }

    @Override
    public MenuDelegate getMenuDelegate(MenuName menuName) {
        return this.menuDelegates.getMenuDelegatesByName().get(menuName);
    }

    @Override
    public MenuItemDelegate getMenuItemDelegate(MenuItemName menuItemName) {
        return this.menuDelegates.getMenuItemDelegatesByName().get(menuItemName);
    }

    @Override
    public void onMenuShowing(MenuName menuName) {

    }

    @Override
    public void onMenuHiding(MenuName menuName) {

    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(DemoComponents.MENU_AWARE_AREA);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
                this.menuDelegates.addAll(
            new SimpleMenuDelegate(ExtraMenu.NAME, true) // extra menu is included
        );
        this.menuDelegates.addAll(
            new SimpleMenuItemDelegate(ExtraMenu.FOO_ITEM) {
                @Override
                public Boolean getItemValid() {
                    return getView().isFooValid();
                }
            },
            new SimpleMenuItemDelegate(ExtraMenu.BAR_ITEM) {
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
