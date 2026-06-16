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

package com.techsenger.shellfx.demo.menu.file;

import com.techsenger.shellfx.core.ShellFxView;
import com.techsenger.shellfx.demo.menu.AbstractContainerItemHandler;
import com.techsenger.shellfx.demo.page.PageMenuType;
import com.techsenger.shellfx.demo.page.PageTabFxView;
import com.techsenger.shellfx.demo.page.PageTabHistory;
import com.techsenger.shellfx.demo.page.PageTabParams;
import com.techsenger.shellfx.demo.page.PageTabPresenter;
import com.techsenger.shellfx.material.menu.ManagedMenuItem;

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
        var params = new PageTabParams(() -> historyManager
                .getOrCreateHistory(PageTabHistory.class, PageTabHistory::new), menuType);
        var tabPresenter = new PageTabPresenter(tabView, params);
        tabPresenter.initialize();
        resolveMainTabContainer().getComposer().addTab(tabView);
        tabView.requestFocus();
    }

}
