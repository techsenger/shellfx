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

import com.techsenger.patternfx.mvp.ComposeParameters;
import com.techsenger.tabshell.core.dialog.AbstractDialogFxView;
import com.techsenger.tabshell.demo.DemoComponents;
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
public class PagedDialogFxView extends AbstractDialogFxView<PagedDialogPresenter> implements PagedDialogView {

    public class Composer extends AbstractDialogFxView<PagedDialogPresenter>.Composer {

        @Override
        public void compose(ComposeParameters params) {
            super.compose(params);
            var rootItem = MenuFactory.create(MenuFactory.PageType.DIALOG);
            pageHost = new PageHostFxView<>(rootItem);
            var hostPresenter = new PageHostPresenter<>(pageHost, () -> getPresenter().getHistory().getHostHistory());
            hostPresenter.initialize();

            getModifiableChildren().add(pageHost);
            getContentBox().getChildren().add(pageHost.getNode());
            VBox.setVgrow(pageHost.getNode(), Priority.ALWAYS);

            pageHost.getComposer().selectPage(DemoComponents.PAGE_1);

        }
    }

    private PageHostFxView<?> pageHost;

    private final ResultButton okButton = new ResultButton(PagedDialogButtons.OK, "OK");

    public PagedDialogFxView() {
        super();
    }

    @Override
    public void requestFocus() {
        pageHost.requestFocus();
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new PagedDialogFxView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        registerButtons(okButton);
        getContentBox().setPadding(Insets.EMPTY);
    }
}
