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

package com.techsenger.shellfx.demo.area;

import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.core.area.AbstractAreaView;
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.dialog.DialogPort;
import com.techsenger.shellfx.core.popup.OverlayScope;
import com.techsenger.shellfx.core.popup.PopupContainerView;
import com.techsenger.shellfx.core.popup.PopupParams;
import com.techsenger.shellfx.core.popup.PopupPort;
import com.techsenger.shellfx.core.window.WindowContainerView;
import com.techsenger.shellfx.demo.dialog.DemoDialogView;
import com.techsenger.shellfx.demo.dialog.DemoDialogViewModel;
import com.techsenger.shellfx.demo.popup.DemoPopupView;
import com.techsenger.shellfx.demo.popup.DemoPopupViewModel;
import com.techsenger.shellfx.layout.dockhost.DockHostPort;
import com.techsenger.shellfx.layout.dockhost.DockUserPermission;
import com.techsenger.shellfx.material.Anchors;
import com.techsenger.shellfx.material.style.Spacing;
import com.techsenger.shellfx.material.style.StyleClasses;
import java.util.EnumMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * The area with the demo dialogs and popups and the checkboxes of the items of the Extra menu. It is placed in a
 * container of popups, for example a tab; if the container also hosts windows, the area can open dialogs in it, too.
 *
 * @author Pavel Castornii
 */
public class DemoAreaView<VM extends DemoAreaViewModel<?>> extends AbstractAreaView<VM> {

    public class Composer extends AbstractAreaView<VM>.Composer implements DemoAreaComposer {

        private final DemoAreaView<VM> view = DemoAreaView.this;

        private ShellView<?> shell;

        private PopupContainerView<?> container;

        private DockHostPort dockHostPort;

        @Override
        public DialogPort openDemoDialog(OverlayScope scope, boolean resizable, DialogParams params) {
            var viewModel = new DemoDialogViewModel<>(params);
            var dialogView = new DemoDialogView<>(viewModel, shell.getContext().getStylesheetRegistry());
            dialogView.initialize();
            viewModel.setResizable(resizable);
            if (scope == OverlayScope.WINDOW) {
                shell.getComposer().addDialog(dialogView);
            } else {
                ((WindowContainerView<?>) container).getComposer().addDialog(dialogView);
            }
            return viewModel;
        }

        @Override
        public PopupPort openDemoPopup(OverlayScope scope) {
            var params = new PopupParams(null, false);
            var viewModel = new DemoPopupViewModel<>(params);
            var popupView = new DemoPopupView<>(viewModel);
            popupView.initialize();
            if (scope == OverlayScope.WINDOW) {
                shell.getComposer().addPopup(popupView, Anchors.topRight(20, 20));
            } else {
                container.getComposer().addPopup(popupView, Anchors.bottomRight(20, 20));
            }
            return viewModel;
        }

        @Override
        public DockHostPort getDockHostPort() {
            return dockHostPort;
        }

        private void setContainer(ShellView<?> shell, PopupContainerView<?> container) {
            this.shell = shell;
            this.container = container;
        }

        private void setDockHostPort(DockHostPort dockHostPort) {
            this.dockHostPort = dockHostPort;
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

    private final ScrollPane scrollPane = new ScrollPane(stackPane);

    private final Separator userPermissionsSeparator = new Separator();

    private final Label userPermissionsLabel = new Label("Dock User Permissions:");

    private final GridPane userPermissionsGridPane = new GridPane();

    private final Map<DockUserPermission, CheckBox> checkBoxesByPermission = new EnumMap<>(DockUserPermission.class);

    /**
     * Creates a view.
     *
     * @param viewModel the view model
     * @param shell the shell the dialogs and popups of the window scope are opened in
     * @param container the container the popups of the tab scope are opened in; the dialogs of that scope are offered
     *     only if it is a {@link WindowContainerView}
     * @param dockHostPort the dock host the area is placed in, whose user permissions the area can change
     */
    public DemoAreaView(VM viewModel, ShellView<?> shell, PopupContainerView<?> container,
            DockHostPort dockHostPort) {
        super(viewModel);
        getComposer().setContainer(shell, container);
        getComposer().setDockHostPort(dockHostPort);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public Region getNode() {
        return scrollPane;
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new DemoAreaView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        StackPane.setAlignment(vBox, Pos.CENTER);
        // the content stays centered while it fits and is scrolled when it does not
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        stackPane.setPadding(new Insets(Spacing.getVertical(), 0, Spacing.getVertical(), 0));
        setupButton(shellDialogButton);
        setupButton(tabDialogButton);
        this.tabDialogButton.setVisible(getComposer().container instanceof WindowContainerView<?>);
        this.tabDialogButton.setManaged(getComposer().container instanceof WindowContainerView<?>);
        setupButton(shellPopupButton);
        setupButton(tabPopupButton);
        vBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        vBox.setSpacing(Spacing.getVertical());
        dialogHBox.setSpacing(Spacing.getHorizontal());
        popupHBox.setSpacing(Spacing.getHorizontal());
        // two permissions in a row, so the block is not wider than the others
        userPermissionsGridPane.setHgap(Spacing.getHorizontal());
        userPermissionsGridPane.setVgap(Spacing.getVertical());
        for (var permission : DockUserPermission.values()) {
            var checkBox = new CheckBox(permission.name());
            checkBoxesByPermission.put(permission, checkBox);
            var index = permission.ordinal();
            userPermissionsGridPane.add(checkBox, index % 2, index / 2);
        }
        vBox.getChildren().addAll(userPermissionsSeparator, userPermissionsLabel, userPermissionsGridPane);
    }

    @Override
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
        this.fooDisabledCheckBox.selectedProperty().bindBidirectional(viewModel.fooDisabledProperty());
        this.barIncludedCheckBox.selectedProperty().bindBidirectional(viewModel.barIncludedProperty());
        this.barDisabledCheckBox.selectedProperty().bindBidirectional(viewModel.barDisabledProperty());
        viewModel.getGrantedByPermission().forEach((permission, granted) ->
                checkBoxesByPermission.get(permission).selectedProperty().bindBidirectional(granted));
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        // in order for the shell to determine the focused component, you need to manually set focus on the container
        // when the user clicks on it with the mouse. When a Pane inside a Tab requests focus, it receives it, but
        // immediately loses it because TabPane captures the focus, so the focus is requested on the next pulse.
        stackPane.setOnMouseClicked(e -> Platform.runLater(() -> stackPane.requestFocus()));
        this.shellDialogButton.setOnAction(e -> getViewModel().onDialogOpen(OverlayScope.WINDOW));
        this.tabDialogButton.setOnAction(e -> getViewModel().onDialogOpen(OverlayScope.TAB));
        this.shellPopupButton.setOnAction(e -> getViewModel().onPopupOpen(OverlayScope.WINDOW));
        this.tabPopupButton.setOnAction(e -> getViewModel().onPopupOpen(OverlayScope.TAB));
    }

    private void setupButton(Button button) {
        button.getStyleClass().add(StyleClasses.SIZE_L);
        HBox.setHgrow(button, Priority.ALWAYS);
        button.setMaxWidth(Double.MAX_VALUE);
    }
}
