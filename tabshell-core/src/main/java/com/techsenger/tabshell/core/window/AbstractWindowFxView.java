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

package com.techsenger.tabshell.core.window;

import atlantafx.base.theme.Styles;
import com.techsenger.annotations.Unmodifiable;
import com.techsenger.patternfx.mvp.AbstractParentFxView;
import com.techsenger.patternfx.mvp.FxViewUtils;
import com.techsenger.patternfx.mvp.ParentFxView;
import com.techsenger.tabshell.core.dialog.DefaultDialogManager;
import com.techsenger.tabshell.core.dialog.DialogFxView;
import com.techsenger.tabshell.core.dialog.DialogManager;
import com.techsenger.tabshell.core.dialog.DialogPort;
import com.techsenger.tabshell.core.popup.PopupFxView;
import com.techsenger.tabshell.core.popup.PopupPort;
import com.techsenger.tabshell.core.style.CoreIcons;
import com.techsenger.tabshell.core.style.CssAnchor;
import com.techsenger.tabshell.material.Anchors;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.icon.Icon;
import com.techsenger.tabshell.material.icon.IconViewBox;
import com.techsenger.tabshell.material.style.Spacing;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.material.style.Stylesheet;
import com.techsenger.tabshell.material.theme.AtlantaFxTheme;
import com.techsenger.tabshell.material.theme.JavaFxTheme;
import com.techsenger.tabshell.material.theme.Theme;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractWindowFxView<P extends AbstractWindowPresenter<?>> extends AbstractParentFxView<P>
        implements WindowFxView<P> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractWindowFxView.class);

    private static final PseudoClass MAXIMIZED_PSEUDO_CLASS = PseudoClass.getPseudoClass("maximized");

    private static final PseudoClass UNFOCUSED_PSEUDO_CLASS = PseudoClass.getPseudoClass("unfocused");

    public class Composer extends AbstractParentFxView<P>.Composer implements WindowFxView.Composer {

        private final ReadOnlyObjectWrapper<ParentFxView<?>> focused = new ReadOnlyObjectWrapper<>();

        private final AbstractWindowFxView<P> view = AbstractWindowFxView.this;

        /**
         * PauseTransition used to implement debounce on the JavaFX thread with duration.
         */
        private PauseTransition focusDebouncePause = new PauseTransition(Duration.millis(250));

        public Composer() {

        }

        @Override
        public void compose() {
            super.compose();
            this.focused.addListener((ov, oldV, newV) -> {
                logger.debug("{} Focused component: {}", getDescriptor().getLogPrefix(),
                        (newV == null) ? null : newV.getDescriptor().getFullName());
            });
            view.window.getScene().focusOwnerProperty().addListener((ov, oldV, newV) -> {
                this.focusDebouncePause.stop();
                this.focusDebouncePause.playFromStart();
            });
            this.focusDebouncePause.setOnFinished((e) -> onFocusPauseFinished());
        }

        @Override
        public void addDialog(DialogFxView<?> dialog) {
            view.dialogManager.addDialog(dialog);
            view.getModifiableChildren().add(dialog);
        }

        @Override
        public void removeDialog(DialogFxView<?> dialog) {
            view.dialogManager.removeDialog(dialog);
            view.getModifiableChildren().remove(dialog);
        }

        @Override
        public void closeDialog(DialogFxView<?> dialog) {
            removeDialog(dialog);
            dialog.getPresenter().deinitializeTree();
        }

        @Override
        public @Unmodifiable List<? extends DialogPort> getDialogPorts() {
            return view.getDialogManager().getDialogs().stream().map(d -> d.getPresenter()).toList();
        }

        @Override
        public @Unmodifiable List<? extends DialogFxView<?>> getDialogs() {
            return view.getDialogManager().getDialogs();
        }

        @Override
        public void addPopup(PopupFxView<?> popup, Anchors anchors) {
            view.dialogManager.addPopup(popup, anchors);
            view.getModifiableChildren().add(popup);
        }

        @Override
        public void removePopup(PopupFxView<?> popup) {
            view.dialogManager.removePopup(popup);
            view.getModifiableChildren().remove(popup);
        }

        @Override
        public void closePopup(PopupFxView<?> popup) {
            removePopup(popup);
            popup.getPresenter().deinitializeTree();
        }

        @Override
        public @Unmodifiable List<? extends PopupPort> getPopupPorts() {
            return view.getDialogManager().getPopups().stream().map(d -> d.getPresenter()).toList();
        }

        @Override
        public @Unmodifiable List<? extends PopupFxView<?>> getPopups() {
            return view.getDialogManager().getPopups();
        }

        @Override
        public ReadOnlyObjectProperty<ParentFxView<?>> focusedProperty() {
            return this.focused.getReadOnlyProperty();
        }

        @Override
        public ParentFxView<?> getFocused() {
            return this.focused.get();
        }

        protected void onFocusPauseFinished() {
            var newNode = view.window.getScene().getFocusOwner();
            if (newNode == null) {
                setFocused(null);
                return;
            }
            var component = FxViewUtils.findView(newNode, ParentFxView.class);
            setFocused(component);
        }

        private void setFocused(ParentFxView<?> focused) {
            this.focused.set(focused);
        }
    }

    private class WindowDialogManager extends DefaultDialogManager {

        WindowDialogManager(StackPane stackPane, VBox mainPane) {
            super(stackPane, mainPane);
        }

        @Override
        public void addDialog(DialogFxView<?> dialog) {
            super.addDialog(dialog);
            updateUnfocused();
        }

        @Override
        public void removeDialog(DialogFxView<?> dialog) {
            super.removeDialog(dialog);
            updateUnfocused();
        }
    }

    private final Stage window;

    private final ObservableList<Stylesheet> stylesheets;

    private final IconViewBox iconViewBox = new IconViewBox();

    private final HBox leftBox = new HBox(iconViewBox);

    private final Label titleLabel = new Label();

    private final Button closeButton = new Button(null, new FontIconView(CoreIcons.WINDOW_CLOSE));

    private final Button minimizeButton = new Button(null, new FontIconView(CoreIcons.WINDOW_MINIMIZE));

    private final FontIconView maximizeIconView = new FontIconView(CoreIcons.WINDOW_MAXIMIZE);

    private final Button maximizeButton = new Button(null, maximizeIconView);

    private final HBox rightBox = new HBox(minimizeButton, maximizeButton, closeButton);

    private final HeaderBar titleBar = new HeaderBar(leftBox, titleLabel, rightBox);

    private final VBox contentBox = new VBox();

    private final VBox windowBox = new VBox(titleBar, contentBox);

    private final StackPane stackPane = new StackPane(windowBox);

    private Pane blockPane;

    private final DialogManager dialogManager = new WindowDialogManager(stackPane, contentBox);

    private ThemeApplier themeApplier;

    private FontApplier fontApplier;

    public AbstractWindowFxView() {
        this(null, null);
    }

    public AbstractWindowFxView(Stage stage) {
        this(stage, null);
    }

    public AbstractWindowFxView(List<Stylesheet> stylesheets) {
        this(null, stylesheets);
    }

    public AbstractWindowFxView(Stage stage, List<Stylesheet> stylesheets) {
        if (stage == null) {
            stage = new Stage();
        }
        this.window = stage;
        this.stylesheets = FXCollections.observableArrayList(createDefaultStylesheets());
        if (stylesheets != null) {
            this.stylesheets.addAll(stylesheets);
        }
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public void setMaximized(boolean value) {
        this.window.setMaximized(value);
    }

    @Override
    public void setMaximizable(boolean maximizable) {
        if (maximizable) {
            if (this.maximizeButton.getParent() == null) {
                rightBox.getChildren().add(getMaximizeButtonIndex(), this.maximizeButton);
            }
        } else {
            if (this.maximizeButton.getParent() != null) {
                rightBox.getChildren().remove(this.maximizeButton);
            }
        }
    }

    @Override
    public void setMinimized(boolean minimized) {
        this.window.setIconified(minimized);
    }

    @Override
    public void setMinimizable(boolean minimizable) {
        if (minimizable) {
            if (this.minimizeButton.getParent() == null) {
                rightBox.getChildren().add(getMinimizeButtonIndex(), this.minimizeButton);
            }
        } else {
            if (this.minimizeButton.getParent() != null) {
                rightBox.getChildren().remove(this.minimizeButton);
            }
        }
    }

    @Override
    public void setClosable(boolean closable) {
        this.closeButton.setDisable(!closable);
    }
    @Override
    public void setWidth(double value) {
        this.window.setWidth(value);
    }

    @Override
    public void setHeight(double value) {
        this.window.setHeight(value);
    }

    @Override
    public void setIcon(Icon<?> icon) {
        iconViewBox.setIcon(icon);
    }

    @Override
    public void setTitle(String title) {
        this.titleLabel.setText(title);
    }

    @Override
    public Stage getWindow() {
        return this.window;
    }

    @Override
    public void closeWindow() {
        this.window.close();
    }

    @Override
    public void addStylesheets(List<Stylesheet> sheets) {
        this.stylesheets.addAll(sheets);
    }

    @Override
    public void removeStylesheets(List<Stylesheet> sheets) {
        this.stylesheets.removeAll(sheets);
    }

    @Override
    public @Unmodifiable List<Stylesheet> getStylesheets() {
        return Collections.unmodifiableList(stylesheets);
    }

    @Override
    public void setTheme(Theme theme) {
        this.themeApplier.setTheme(theme);
    }

    @Override
    public void setRegularFont(Font font) {
        this.fontApplier.setRegularFont(font);
    }

    @Override
    public void setMonospaceFont(Font font) {
        this.fontApplier.setMonospaceFont(font);
    }

    @Override
    public void setBlocked(boolean blocked) {
        if (blocked) {
            this.blockPane = new Pane();
            this.blockPane.setMouseTransparent(false);
            this.stackPane.getChildren().add(this.blockPane);
        } else {
            this.stackPane.getChildren().remove(this.blockPane);
            this.blockPane = null;
        }
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return (Composer) super.createComposer();
    }

    protected DialogManager getDialogManager() {
        return this.dialogManager;
    }

    @Override
    protected void build() {
        super.build();
        this.closeButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICON_BUTTON, StyleClasses.SIZE_S);
        this.minimizeButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICON_BUTTON, StyleClasses.SIZE_S);
        this.maximizeButton.getStyleClass().addAll(Styles.FLAT, StyleClasses.ICON_BUTTON, StyleClasses.SIZE_S);
        this.windowBox.getStyleClass().add("window-box");
        this.leftBox.getStyleClass().add("left-box");
        this.titleBar.getStyleClass().add("title-bar");
        this.rightBox.getStyleClass().add("right-box");
        this.rightBox.setSpacing(Spacing.getHorizontal() + Spacing.getHorizontalThird());
        VBox.setVgrow(contentBox, Priority.ALWAYS);
        this.contentBox.getStyleClass().add("content-box");
        HeaderBar.setPrefButtonHeight(window, 0); // to hide default buttons
        window.initStyle(StageStyle.EXTENDED);
        var scene = new Scene(stackPane);
        FxViewUtils.setView(scene, this);
        window.setScene(scene);
        //we add stackpane behind stage root
        stackPane.getStyleClass().add("root-stack-pane");
        themeApplier = new ThemeApplier(scene, this.stylesheets, getDescriptor());
        this.fontApplier = new FontApplier(stackPane);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        this.window.maximizedProperty().addListener((ov, oldV, newV) -> {
            this.windowBox.pseudoClassStateChanged(MAXIMIZED_PSEUDO_CLASS, newV);
            if (newV) {
                this.maximizeIconView.setIcon(CoreIcons.WINDOW_RESTORE);
            } else {
                this.maximizeIconView.setIcon(CoreIcons.WINDOW_MAXIMIZE);
            }
        });
        this.window.widthProperty().addListener((ov, oldV, newV) -> getPresenter().onWidthChanged(newV.doubleValue()));
        this.window.heightProperty()
                .addListener((ov, oldV, newV) -> getPresenter().onHeightChanged(newV.doubleValue()));
        this.window.maximizedProperty().addListener((ov, oldV, newV) -> getPresenter().onMaximized(newV));
        this.window.focusedProperty().addListener((ov, oldV, newV) -> updateUnfocused());
        this.closeButton.getStyleClass().add("close-button");
        this.minimizeButton.getStyleClass().add("minimize-button");
        this.maximizeButton.getStyleClass().add("maximize-button");
    }

    @Override
    protected void addHandlers() {
        super.addHandlers();
        this.closeButton.setOnAction(e -> getPresenter().onCloseRequest());
        this.maximizeButton.setOnAction(e -> window.setMaximized(!window.isMaximized()));
        this.minimizeButton.setOnAction(e -> window.setIconified(true));
        this.window.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::fixAcceleratorKeyPressed);
//        var viewModel = getViewModel();
//        stage.addEventHandler(StageResizeEvent.STAGE_RESIZE_FINISHED, e -> {
//            if (!stage.isMaximized()) {
//                viewModel.setDefaultWidth(stage.getWidth());
//                viewModel.setDefaultHeight(stage.getHeight());
//            }
//        });
        window.setOnCloseRequest(event -> {
            event.consume();
            getPresenter().onCloseRequest();
        });
    }

    protected IconViewBox getIconViewBox() {
        return iconViewBox;
    }

    protected HBox getLeftBox() {
        return leftBox;
    }

    protected Label getTitleLabel() {
        return titleLabel;
    }

    protected Button getCloseButton() {
        return closeButton;
    }

    protected int getCloseButtonIndex() {
        return rightBox.getChildren().size();
    }

    protected Button getMinimizeButton() {
        return minimizeButton;
    }

    protected int getMinimizeButtonIndex() {
        var index = rightBox.getChildren().size();
        if (this.maximizeButton.getParent() != null) {
            index--;
        }
        if (this.closeButton.getParent() != null) {
            index--;
        }
        return index;
    }

    protected FontIconView getMaximizeIconView() {
        return maximizeIconView;
    }

    protected Button getMaximizeButton() {
        return maximizeButton;
    }

    protected int getMaximizeButtonIndex() {
        var index = rightBox.getChildren().size();
        if (this.closeButton.getParent() != null) {
            index--;
        }
        return index;
    }

    protected HBox getRightBox() {
        return rightBox;
    }

    protected HeaderBar getTitleBar() {
        return titleBar;
    }

    protected VBox getContentBox() {
        return contentBox;
    }

    protected VBox getWindowBox() {
        return windowBox;
    }

    protected StackPane getStackPane() {
        return stackPane;
    }

    protected Pane getBlockPane() {
        return blockPane;
    }

    /**
     * JavaFX doesn't support the same accelerator to be installed in multiple MenuItems. See this bug:
     * https://bugs.openjdk.org/browse/JDK-8088068 . This method is a workaround for this problem.
     *
     * @param e
     */
    protected void fixAcceleratorKeyPressed(KeyEvent e) {
        Node focusedNode = window.getScene().getFocusOwner();
        if (focusedNode != null && focusedNode instanceof Control) {
            Control c = (Control) focusedNode;
            if (c.getContextMenu() != null) {
                for (MenuItem item : c.getContextMenu().getItems()) {
                    if (item.getAccelerator() != null && item.getAccelerator().match(e)) {
                        item.fire();
                        e.consume();
                    }
                }
            }
        }
    }

    protected List<Stylesheet> createDefaultStylesheets() {
        Set<Theme> allThemes = Stream.concat(
                Arrays.stream(AtlantaFxTheme.values()),
                Arrays.stream(JavaFxTheme.values()))
                .collect(Collectors.toSet());
        return List.of(
                new Stylesheet(CssAnchor.class.getResource("core.css"), Set.of(AtlantaFxTheme.values())),
                new Stylesheet(StyleClasses.class.getResource("material.css"), allThemes));
    }

    private void updateUnfocused() {
        if (this.window.isFocused()) {
            var unfocused = this.dialogManager.getDialogs().size() > 0;
            windowBox.pseudoClassStateChanged(UNFOCUSED_PSEUDO_CLASS, unfocused);
        } else {
            windowBox.pseudoClassStateChanged(UNFOCUSED_PSEUDO_CLASS, true);
        }
    }
}
