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

package com.techsenger.shellfx.demo.page;

import com.techsenger.shellfx.core.dialog.AbstractDialogView;
import com.techsenger.shellfx.core.window.AbstractWindowView;
import com.techsenger.shellfx.layout.pagehost.AbstractPageHostView;
import com.techsenger.shellfx.layout.pagehost.PageHostParams;
import com.techsenger.shellfx.layout.pagehost.PageHostView;
import com.techsenger.shellfx.layout.pagehost.PageHostViewModel;
import com.techsenger.shellfx.layout.pagehost.TreePageHostParams;
import com.techsenger.shellfx.layout.pagehost.TreePageHostView;
import com.techsenger.shellfx.layout.pagehost.TreePageHostViewModel;
import com.techsenger.shellfx.material.button.ResultButton;
import javafx.geometry.Insets;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class PageDialogView<VM extends PageDialogViewModel<?>> extends AbstractDialogView<VM> {

    public class Composer extends AbstractWindowView<VM>.Composer {

        private AbstractPageHostView<?> pageHost;

        @Override
        public void compose() {
            super.compose();
            if (getViewModel().getMenuType() == PageMenuType.FLAT) {
                var pages = MenuFactory.createFlatMenu(PageHostParent.TAB);
                var params = new PageHostParams(() -> getViewModel().getHistory().getHostHistory());
                var pageHostViewModel = new PageHostViewModel<>(params);
                pageHostViewModel.setDividerPosition(0.275);
                var pageHost = new PageHostView<>(pageHostViewModel);
                pageHost.initialize();
                pageHost.getComposer().setPages(pages);
                getModifiableChildren().add(pageHost);
                getContentBox().getChildren().add(pageHost.getNode());
                VBox.setVgrow(pageHost.getNode(), Priority.ALWAYS);
                pageHostViewModel.selectPage(0);
                this.pageHost = pageHost;
            } else {
                var rootItem = MenuFactory.createTreeMenu(PageHostParent.TAB);
                var params = new TreePageHostParams(() -> getViewModel().getHistory().getHostHistory());
                var treeHostViewModel = new TreePageHostViewModel<>(params);
                treeHostViewModel.setDividerPosition(0.275);
                var pageHost = new TreePageHostView<>(treeHostViewModel);
                pageHost.initialize();
                pageHost.getComposer().setPages(rootItem, false);
                getModifiableChildren().add(pageHost);
                getContentBox().getChildren().add(pageHost.getNode());
                VBox.setVgrow(pageHost.getNode(), Priority.ALWAYS);
                treeHostViewModel.selectPage(rootItem.getChildren().getFirst()); // the root is not shown
                this.pageHost = pageHost;
            }
        }

        private AbstractPageHostView<?> getPageHost() {
            return pageHost;
        }
    }

    private final ResultButton okButton = new ResultButton(PageDialogButtons.OK, "OK");

    public PageDialogView(VM viewModel) {
        super(viewModel);
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
        return new PageDialogView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        registerButtons(okButton);
        getContentBox().setPadding(Insets.EMPTY);
    }
}
