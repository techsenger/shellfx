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

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.core.area.AbstractAreaFxView;
import com.techsenger.tabshell.core.dialog.DialogPort;
import com.techsenger.tabshell.core.popup.OverlayScope;
import com.techsenger.tabshell.core.popup.PopupParams;
import com.techsenger.tabshell.core.popup.PopupPort;
import com.techsenger.tabshell.core.tab.TabFxView;
import com.techsenger.tabshell.demo.dialogs.DemoDialogFxView;
import com.techsenger.tabshell.demo.dialogs.DemoDialogPresenter;
import com.techsenger.tabshell.demo.popup.DemoPopupFxView;
import com.techsenger.tabshell.demo.popup.DemoPopupPresenter;
import com.techsenger.tabshell.material.Anchors;
import com.techsenger.tabshell.material.style.Spacing;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class MenuAwareAreaFxView extends AbstractAreaFxView<MenuAwareAreaPresenter> implements MenuAwareAreaView {

    public class Composer extends AbstractAreaFxView<MenuAwareAreaPresenter>.Composer
            implements MenuAwareAreaView.Composer {

        private final MenuAwareAreaFxView view = MenuAwareAreaFxView.this;

        private TabFxView<?> mainTab;

        @Override
        public DialogPort addDemoDialog(OverlayScope scope, boolean resizable) {
            var v = new DemoDialogFxView();
            var p = new DemoDialogPresenter(v);
            p.initialize();
            p.setResizable(resizable);
            if (scope == OverlayScope.WINDOW) {
                var shell = mainTab.getComposer().getShell();
                shell.getComposer().addDialog(v);
            } else {
                mainTab.getComposer().addDialog(v);
            }
            return p;
        }

        @Override
        public PopupPort addDemoPopup(OverlayScope scope) {
            var view = new DemoPopupFxView();
            var params = new PopupParams(false);
            var presenter = new DemoPopupPresenter(view, params);
            presenter.initialize();
            if (scope == OverlayScope.WINDOW) {
                var shell = mainTab.getComposer().getShell();
                shell.getComposer().addPopup(view, Anchors.topRight(40, 20));
            } else {
                mainTab.getComposer().addPopup(view, Anchors.bottomRight(20, 20));
            }
            return presenter;
        }

        private void setMainTab(TabFxView<?> mainTab) {
            this.mainTab = mainTab;
        }
    }

    private final CheckBox fooDisabledCheckBox = new CheckBox("Foo Item Disabled");

    private final CheckBox barIncludedCheckBox = new CheckBox("Bar Item Included");

    private final CheckBox barDisabledCheckBox = new CheckBox("Bar Item Disabled");

    private final Button shellDialogButton = new Button("Shell");

    private final Button tabDialogButton = new Button("Tab");

    private final HBox dialogHBox = new HBox(shellDialogButton, tabDialogButton);

    private final Button shellPopupButton = new Button("Shell");

    private final Button tabPopupButton = new Button("Tab");

    private final HBox popupHBox = new HBox(shellPopupButton, tabPopupButton);

    private final VBox vBox = new VBox(new Label("Main Menu -> Extra:"), fooDisabledCheckBox, barIncludedCheckBox,
            barDisabledCheckBox, new Label("Dialogs:"), dialogHBox, new Label("Popups:"), popupHBox);

    private final StackPane stackPane = new StackPane(vBox);

    public MenuAwareAreaFxView(TabFxView<?> mainTab) {
        super();
        getComposer().setMainTab(mainTab);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public Region getNode() {
        return stackPane;
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new MenuAwareAreaFxView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        StackPane.setAlignment(vBox, Pos.CENTER);
        setupButton(shellDialogButton);
        setupButton(tabDialogButton);
        setupButton(shellPopupButton);
        setupButton(tabPopupButton);
        vBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        vBox.setSpacing(Spacing.VERTICAL);
        dialogHBox.setSpacing(Spacing.HORIZONTAL);
        popupHBox.setSpacing(Spacing.HORIZONTAL);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        this.fooDisabledCheckBox.selectedProperty()
                .addListener((ov, oldV, newV) -> getPresenter().onFooDisabledSelected(newV));
        this.barDisabledCheckBox.selectedProperty()
                .addListener((ov, oldV, newV) -> getPresenter().onBarDisabledSelected(newV));
        this.barIncludedCheckBox.selectedProperty()
                .addListener((ov, oldV, newV) -> getPresenter().onBarIncludedSelected(newV));
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        // in order for the shell to determine the focused component, you need to manually set focus
        // on the container when the user clicks on it with the mouse.
        stackPane.setOnMouseClicked(e -> stackPane.requestFocus());
        this.shellDialogButton.setOnAction(e -> getPresenter().onDialogOpen(OverlayScope.WINDOW));
        this.tabDialogButton.setOnAction(e -> getPresenter().onDialogOpen(OverlayScope.TAB));
        this.shellPopupButton.setOnAction(e -> getPresenter().onPopupOpen(OverlayScope.WINDOW));
        this.tabPopupButton.setOnAction(e -> getPresenter().onPopupOpen(OverlayScope.TAB));
    }

    private void setupButton(Button button) {
        button.getStyleClass().add(Styles.DENSE);
        HBox.setHgrow(button, Priority.ALWAYS);
        button.setMaxWidth(Double.MAX_VALUE);
    }
}
