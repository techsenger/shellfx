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

package com.techsenger.tabshell.demo.page;

import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.tab.AbstractTabFxView;
import com.techsenger.tabshell.layout.pagehost.AbstractPageHostFxView;
import com.techsenger.tabshell.layout.pagehost.PageHostFxView;
import com.techsenger.tabshell.layout.pagehost.PageHostParams;
import com.techsenger.tabshell.layout.pagehost.PageHostPresenter;
import com.techsenger.tabshell.layout.pagehost.TreePageHostFxView;
import com.techsenger.tabshell.layout.pagehost.TreePageHostParams;
import com.techsenger.tabshell.layout.pagehost.TreePageHostPresenter;
import com.techsenger.tabshell.material.style.StyleClasses;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class PageTabFxView extends AbstractTabFxView<PageTabPresenter> implements PageTabView {

    public class Composer extends AbstractTabFxView<PageTabPresenter>.Composer implements PageTabView.Composer {

        private PageMenuType menuType;

        private AbstractPageHostFxView<?> pageHost;

        @Override
        public void setMenuType(PageMenuType menuType) {
            this.menuType = menuType;
        }

        @Override
        public void compose() {
            super.compose();
            if (menuType == PageMenuType.FLAT) {
                var pages = MenuFactory.createMenu(PageHostParent.TAB);
                var pageHost = new PageHostFxView<>();
                var params = new PageHostParams(() -> getPresenter().getHistory().getHostHistory());
                var hostPresenter = new PageHostPresenter<>(pageHost, params);
                hostPresenter.initialize();
                hostPresenter.setDividerPosition(0.275);
                pageHost.getComposer().setPages(pages);
                getModifiableChildren().add(pageHost);
                getContentBox().getChildren().add(pageHost.getNode());
                VBox.setVgrow(pageHost.getNode(), Priority.ALWAYS);
                pageHost.getPresenter().selectPage(0);
                this.pageHost = pageHost;
            } else {
                var rootItem = MenuFactory.createTreeMenu(PageHostParent.TAB);
                var pageHost = new TreePageHostFxView<>();
                var params = new TreePageHostParams(() -> getPresenter().getHistory().getHostHistory());
                var hostPresenter = new TreePageHostPresenter<>(pageHost, params);
                hostPresenter.initialize();
                hostPresenter.setDividerPosition(0.275);
                pageHost.getComposer().setPages(rootItem, false);
                getModifiableChildren().add(pageHost);
                getContentBox().getChildren().add(pageHost.getNode());
                VBox.setVgrow(pageHost.getNode(), Priority.ALWAYS);
                pageHost.getPresenter().selectPage(rootItem.getChildren().getFirst()); // the root is not shown
                this.pageHost = pageHost;
            }
        }

        private AbstractPageHostFxView<?> getPageHost() {
            return pageHost;
        }
    }

    public PageTabFxView(ShellFxView<?> shell) {
        super(shell);
    }

    @Override
    public void requestFocus() {
         getComposer().getPageHost().requestFocus();
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new PageTabFxView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        var button = new Button("Test");
        var toolBar = new ToolBar(button);
        toolBar.getStyleClass().add(StyleClasses.BLEND);
        getContentBox().getChildren().add(toolBar);
    }
}
