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

package com.techsenger.tabshell.devtools.node;

import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.dialog.AbstractDialogFxView;
import com.techsenger.tabshell.material.button.ResultButton;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.style.SizeConstants;
import com.techsenger.tabshell.shared.style.SharedIcons;
import com.techsenger.tabshell.web.WebBrowserTabFxView;
import com.techsenger.tabshell.web.WebBrowserTabPresenter;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class PropertyDialogFxView<P extends PropertyDialogPresenter<?, ?>>  extends AbstractDialogFxView<P>
        implements PropertyDialogView {

    public class Composer extends AbstractDialogFxView<P>.Composer implements PropertyDialogComposer {

        @Override
        public void addBrowser(String url) {
            var browser = createBrowser(url);
            browser.getPresenter().initialize();
            shell.getComposer().addTab(browser);
        }

        protected WebBrowserTabFxView<?> createBrowser(String url) {
            var view = new WebBrowserTabFxView<>(shell);
            var presenter = new WebBrowserTabPresenter<>(view, url);
            return view;
        }
    }

    private final GridPane gridPane = new GridPane();

    private final Label nameLabel = new Label();

    private final HBox nameBox = new HBox(nameLabel);

    private final TextArea valueTextArea = new TextArea();

    private final Label cssLabel = new Label("Css Property");

    private final HBox cssBox = new HBox(cssLabel);

    private final TextField cssTextField = new TextField();

    private final Label stateLabel = new Label("State");

    private final TextField stateTextField = new TextField();

    private final ResultButton okButton = new ResultButton(PropertyDialogButtons.OK, "OK");

    private final ShellFxView<?> shell;

    public PropertyDialogFxView(boolean resizable, ShellFxView<?> shell) {
        super(resizable);
        this.shell = shell;
    }

    @Override
    public void requestFocus() {
        gridPane.requestFocus();
    }

    @Override
    public void setName(String name) {
        nameLabel.setText(name);
    }

    @Override
    public void addNameUrl(String url) {
        var link = new Hyperlink(null, new FontIconView(SharedIcons.OPEN_IN_NEW));
        link.setTooltip(new Tooltip(url));
        nameBox.getChildren().add(link);
        nameBox.setSpacing(SizeConstants.THIRD_INSET);
        link.setOnAction(e -> {
            getPresenter().onFollowLink(url);
        });
    }

    @Override
    public void setValue(String value) {
        valueTextArea.setText(value);
    }

    @Override
    public void setCss(String css) {
        cssTextField.setText(css);
    }

    @Override
    public void addCssUrl(String url) {
        var link = new Hyperlink(null, new FontIconView(SharedIcons.OPEN_IN_NEW));
        link.setTooltip(new Tooltip(url));
        cssBox.getChildren().add(link);
        cssBox.setSpacing(SizeConstants.THIRD_INSET);
        link.setOnAction(e -> {
            getPresenter().onFollowLink(url);
        });
    }

    @Override
    public void setState(String state) {
        stateTextField.setText(state);
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new PropertyDialogFxView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        setPrefWidth(650);
        setPrefHeight(400);
        setTitle("Property Dialog");

        nameLabel.setMinWidth(Label.USE_PREF_SIZE);
        nameBox.setAlignment(Pos.TOP_LEFT);
        gridPane.add(nameBox, 0, 0);
        GridPane.setValignment(nameBox, VPos.TOP);
        valueTextArea.setEditable(false);
        valueTextArea.setWrapText(true);
        gridPane.add(valueTextArea, 1, 0);
        GridPane.setVgrow(valueTextArea, Priority.ALWAYS);
        GridPane.setHgrow(valueTextArea, Priority.ALWAYS);

        cssLabel.setMinWidth(Label.USE_PREF_SIZE);
        cssBox.setAlignment(Pos.CENTER_LEFT);
        gridPane.add(cssBox, 0, 1);
        cssTextField.setEditable(false);
        GridPane.setHgrow(cssTextField, Priority.ALWAYS);
        gridPane.add(cssTextField, 1, 1);

        stateLabel.setMinWidth(Hyperlink.USE_PREF_SIZE);
        gridPane.add(stateLabel, 0, 2);
        stateTextField.setEditable(false);
        GridPane.setHgrow(stateTextField, Priority.ALWAYS);
        gridPane.add(stateTextField, 1, 2);

        gridPane.setVgap(SizeConstants.INSET);
        gridPane.setHgap(SizeConstants.INSET);
        VBox.setVgrow(gridPane, Priority.ALWAYS);
        getContentBox().getChildren().addAll(gridPane);

        registerButtons(okButton);
        okButton.setDefaultButton(true);
        addRightButtons(okButton.getName());
    }
}

