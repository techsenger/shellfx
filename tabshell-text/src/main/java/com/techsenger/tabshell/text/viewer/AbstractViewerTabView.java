/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.text.viewer;

import com.techsenger.mvvm4fx.core.ComponentMediator;
import com.techsenger.tabshell.core.ShellView;
import com.techsenger.tabshell.core.style.CoreIcons;
import com.techsenger.tabshell.core.style.SizeConstants;
import com.techsenger.tabshell.core.style.StyleClasses;
import com.techsenger.tabshell.core.style.StyleUtils;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.textarea.ExtendedTextArea;
import com.techsenger.tabshell.material.textarea.RichTextFxUtils;
import com.techsenger.tabshell.shared.workertab.AbstractWorkerTabView;
import com.techsenger.tabshell.text.style.TextIcons;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import com.techsenger.toolkit.fx.value.ValueUtils;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.Caret;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractViewerTabView<T extends AbstractViewerTabViewModel> extends AbstractWorkerTabView<T> {

    private final ToolBar toolBar = new ToolBar();

    private final ExtendedTextArea textArea;

    private final VirtualizedScrollPane textScrollPane;

    private final Button copyButton = new Button(null, new FontIconView(CoreIcons.COPY));

    private final Button findButton = new Button(null, new FontIconView(CoreIcons.FIND));

    private final ToggleButton wrapTextButton = new ToggleButton(null, new FontIconView(TextIcons.WRAP));

    private final ContextMenu textAreaMenu = new ContextMenu();

    private final MenuItem copyItem = new MenuItem("Copy", new FontIconView(CoreIcons.COPY));

    private final HBox statusBarLeftBox = new HBox();

    private InputMap<KeyEvent> inputMap;

    private DefaultFindPaneView find;

    private String fontStylesheet;

    private final ChangeListener<Number> tabSizeListener = (ov, oldV, newV) -> {
        if (oldV != null) {
            this.getTextArea().getStyleClass().remove("styled-text-area-tab" + oldV);
        }
        this.getTextArea().getStyleClass().add("styled-text-area-tab" + newV);
        this.updateInputMap(this.getViewModel());
    };

    private final ChangeListener<Boolean> tabUseSpacesListener = (ov, oldV, newV) -> {
        this.updateInputMap(this.getViewModel());
    };

    private final ChangeListener<Font> fontListener = (ov, oldV, newV) -> {
        if (fontStylesheet != null) {
            getTextArea().getStylesheets().remove(fontStylesheet);
            fontStylesheet = null;
        }
        if (newV != null) {
            fontStylesheet = "data:text/css, .styled-text-area {" + StyleUtils.toStyle(newV) + "}";
            fontStylesheet += ".styled-text-area .lineno {-fx-font-family:'" + newV.getFamily() + "'}";
            fontStylesheet += ".styled-text-area .lineno .text {-fx-font-family:'" + newV.getFamily() + "'}";
            getTextArea().getStylesheets().add(fontStylesheet);
        }
    };

    public AbstractViewerTabView(ShellView<?> shell, T viewModel, ExtendedTextArea textArea) {
        super(shell, viewModel);
        this.textArea = textArea;
        this.textScrollPane = new VirtualizedScrollPane(textArea);
    }

    public void copy() {
        getTextArea().copy();
    }

    public FindPaneView getFind() {
        return find;
    }

    @Override
    public void requestFocus() {
        NodeUtils.requestFocus(textArea);
    }

    @Override
    protected ComponentMediator createMediator() {
        return new ViewerTabMediator(this);
    }

    @Override
    protected void preInitialize(T viewModel) {
        super.preInitialize(viewModel);
        viewModel.undoManagerWrapper().set(this.textArea.getUndoManager());
    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        VBox.setVgrow(textScrollPane, Priority.ALWAYS);
        //in richtextfx padding via css doesn't work, so, we do this way
        textArea.setPadding(new Insets(0, 0, 0, 0));

        copyButton.setTooltip(new Tooltip("Copy"));
        copyButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        findButton.setTooltip(new Tooltip("Find"));
        findButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        wrapTextButton.setTooltip(new Tooltip("Line Wrap"));
        wrapTextButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);

        this.toolBar.getStyleClass().add(StyleClasses.BLEND);

        this.statusBarLeftBox.setPadding(new Insets(0, 0, 0, SizeConstants.INSET));
        this.statusBarLeftBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(this.statusBarLeftBox, Priority.NEVER);
        this.getStatusBar().getLeftItems().add(this.statusBarLeftBox);
        copyItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));
        textArea.setContextMenu(textAreaMenu);
    }

    @Override
    protected void bind(T viewModel) {
        super.bind(viewModel);
        wrapTextButton.selectedProperty().bindBidirectional(viewModel.wrapTextProperty());
        textArea.wrapTextProperty().bind(viewModel.wrapTextProperty());
        viewModel.textFocusedWrapper().bind(textArea.focusedProperty());
        viewModel.textWrapper().bind(textArea.textProperty());
        viewModel.selectedTextWrapper().bind(textArea.selectedTextProperty());
        viewModel.editableProperty().bind(textArea.editableProperty());
        viewModel.textLengthWrapper().bind(textArea.lengthProperty());
        viewModel.selectionWrapper().bind(textArea.selectionProperty());
        viewModel.caretPositionWrapper().bind(textArea.caretPositionProperty());
        copyButton.disableProperty().bind(
                Bindings.createBooleanBinding(() -> viewModel.selectedTextProperty().get() == null
                        || viewModel.selectedTextProperty().get().isEmpty(),
                viewModel.selectedTextProperty()
            )
        );
    }

    @Override
    protected void addListeners(T viewModel) {
        super.addListeners(viewModel);

        var viewerSettings = viewModel.getSettings();
        ValueUtils.callAndAddListener(viewerSettings.fontProperty(), fontListener);
        ValueUtils.callAndAddListener(viewerSettings.getTabSymbol().sizeProperty(), tabSizeListener);
        viewerSettings.getTabSymbol().useSpacesProperty().addListener(tabUseSpacesListener);

        textArea.showCaretProperty().addListener((ov, t, t1) -> {
            var caretVisible = (t1 != Caret.CaretVisibility.OFF && textArea.isEditable() && textArea.isFocused());
            viewModel.caretVisibleProperty().setValue(caretVisible);
        });

        viewModel.contentSource().addListener((t) -> {
            textArea.clear();
            //when text area appends a text it scrolls to bottom; to disable scrolling to bottom we use this hack
            textArea.appendText("\n"); //this \n will be deleted in change
            textArea.selectRange(0, 0);
            var changes = textArea.createMultiChange(2);
            changes.insertTextAbsolutely(0, 1, t);
            changes.deleteTextAbsolutely(0, 1);
            changes.commit();
            //we need to forget history, or we can go to history event when file is not appended
            textArea.getUndoManager().forgetHistory();
            //we mark position in undo manager. All positions after it are changes
            textArea.getUndoManager().mark();
        });
    }

    @Override
    protected void addHandlers(T viewModel) {
        super.addHandlers(viewModel);
        this.getTextArea().addEventHandler(KeyEvent.KEY_PRESSED, (event) -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                viewModel.removeFindPane();
                event.consume();
            }
        });
        this.copyButton.setOnAction(e -> copy());
        this.findButton.setOnAction(e -> viewModel.addFindPane(false));
        copyItem.setOnAction((t) -> textArea.copy());
        textArea.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
            copyItem.setDisable(!viewModel.isCopyItemValid());
        });
    }

    @Override
    protected void removeListeners(T viewModel) {
        super.removeListeners(viewModel);
        var viewerSettings = viewModel.getSettings();
        viewerSettings.fontProperty().removeListener(fontListener);
        viewerSettings.getTabSymbol().sizeProperty().removeListener(this.tabSizeListener);
        viewerSettings.getTabSymbol().useSpacesProperty().removeListener(this.tabUseSpacesListener);
    }

    protected ExtendedTextArea getTextArea() {
        return this.textArea;
    }

    protected ToolBar getToolBar() {
        return toolBar;
    }

    protected VirtualizedScrollPane getTextScrollPane() {
        return textScrollPane;
    }

    protected Button getCopyButton() {
        return copyButton;
    }

    protected Button getFindButton() {
        return findButton;
    }

    protected ToggleButton getWrapTextButton() {
        return wrapTextButton;
    }

    protected ContextMenu getTextAreaMenu() {
        return textAreaMenu;
    }

    protected MenuItem getCopyItem() {
        return copyItem;
    }

    void addFindPane(DefaultFindPaneView view) {
        this.find = view;
        var scrolledToBottom = RichTextFxUtils.isScrolledToBottom(textScrollPane);
        getTopPane().getChildren().add(this.find.getNode());
        if (scrolledToBottom) {
            RichTextFxUtils.scrollToBottom(textArea);
        }
        this.find.requestFocus();
    }

    void removeFindPane() {
        this.find.deinitialize();
        this.getTopPane().getChildren().remove(this.find.getNode());
        this.find = null;
    }

    private void updateInputMap(AbstractViewerTabViewModel viewModel) {
        var viewerSettings = viewModel.getSettings();
        InputMap inputMap = null;
        if (viewerSettings.getTabSymbol().isUseSpaces()) {
            String s = Stream
                    .generate(() -> String.valueOf(" "))
                    .limit(viewerSettings.getTabSymbol().getSize())
                    .collect(Collectors.joining());
            inputMap = InputMap.consume(EventPattern.keyPressed(KeyCode.TAB), e -> this.textArea.replaceSelection(s));
        }
        //removing an old one
        if (this.inputMap != null) {
            Nodes.removeInputMap(this.getTextArea(), this.inputMap);
        }
        //adding a new one
        this.inputMap = inputMap;
        if (this.inputMap != null) {
            Nodes.addInputMap(this.getTextArea(), this.inputMap);
        }
    }
}
