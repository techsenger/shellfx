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

import com.techsenger.tabshell.core.dialog.AbstractDialogFxView;
import com.techsenger.tabshell.layout.pagehost.PageHostFxView;
import com.techsenger.tabshell.layout.pagehost.PageHostPresenter;
import com.techsenger.tabshell.material.button.ResultButton;
import javafx.geometry.Insets;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class PageDialogFxView extends AbstractDialogFxView<PageDialogPresenter> implements PageDialogView {

    public class Composer extends AbstractDialogFxView<PageDialogPresenter>.Composer {

        private PageHostFxView<?> pageHost;

        @Override
        public void compose() {
            super.compose();
            var rootItem = MenuFactory.create(MenuFactory.PageType.DIALOG);
            pageHost = new PageHostFxView<>();
            var hostPresenter = new PageHostPresenter<>(pageHost, () -> getPresenter().getHistory().getHostHistory());
            hostPresenter.initialize();
            hostPresenter.setDividerPosition(0.275);
            pageHost.getComposer().setPages(rootItem, false);

            getModifiableChildren().add(pageHost);
            getContentBox().getChildren().add(pageHost.getNode());
            VBox.setVgrow(pageHost.getNode(), Priority.ALWAYS);

            pageHost.getPresenter().selectPage(rootItem.getChildren().getFirst()); // the root is not shown
        }
    }

    private final ResultButton okButton = new ResultButton(PageDialogButtons.OK, "OK");

    public PageDialogFxView() {
        super();
    }

    @Override
    public void requestFocus() {
        getComposer().pageHost.requestFocus();
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new PageDialogFxView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        registerButtons(okButton);
        getContentBox().setPadding(Insets.EMPTY);
    }
}
