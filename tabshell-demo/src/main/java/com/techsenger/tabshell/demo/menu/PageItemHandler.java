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
import com.techsenger.tabshell.demo.page.PageMenuType;
import com.techsenger.tabshell.demo.page.PageTabFxView;
import com.techsenger.tabshell.demo.page.PageTabHistory;
import com.techsenger.tabshell.demo.page.PageTabPresenter;
import com.techsenger.tabshell.material.menu.ManagedMenuItem;

/**
 *
 * @author Pavel Castornii
 */
public class PageItemHandler extends AbstractContainerItemHandler {

    private final PageMenuType menuType;

    public PageItemHandler(ManagedMenuItem item, ShellFxView<?> component, PageMenuType menuType) {
        super(item, component);
        this.menuType = menuType;
    }

    @Override
    public void onAction() {

        var tabView = new PageTabFxView(getComponent());
        var historyManager = getComponent().getPresenter().getContext().getHistoryManager();
        var tabPresenter = new PageTabPresenter(tabView,
                () -> historyManager.getOrCreateHistory(PageTabHistory.class, PageTabHistory::new), menuType);
        tabPresenter.initialize();
        resolveMainTabContainer().getComposer().addTab(tabView);
        tabView.requestFocus();
    }

}
