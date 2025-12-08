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

package com.techsenger.tabshell.text.editor;

import com.techsenger.tabshell.core.ShellView;
import com.techsenger.tabshell.core.style.CoreIcons;
import com.techsenger.tabshell.core.style.SizeConstants;
import com.techsenger.tabshell.material.style.StyleClasses;
import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.textarea.ExtendedTextArea;
import com.techsenger.tabshell.text.viewer.AbstractViewerTabView;
import com.techsenger.toolkit.core.StringUtils;
import java.util.function.IntConsumer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import org.fxmisc.richtext.Caret;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.TwoDimensional;

/**
 * Abstract class for editors.
 *
 * @author Pavel Castornii
 */
public abstract class AbstractEditorTabView<T extends AbstractEditorTabViewModel> extends AbstractViewerTabView<T> {

    private final Button newButton = new Button(null, new FontIconView(CoreIcons.ADD));

    private final Button clearButton = new Button(null, new FontIconView(CoreIcons.CLEAR));

    private final Button cutButton = new Button(null, new FontIconView(CoreIcons.CUT));

    private final Button pasteButton = new Button(null, new FontIconView(CoreIcons.PASTE));

    private final Button undoButton = new Button(null, new FontIconView(CoreIcons.UNDO));

    private final Button redoButton = new Button(null, new FontIconView(CoreIcons.REDO));

    private final Button replaceButton = new Button(null, new FontIconView(CoreIcons.REPLACE));

    private final Label positionLabel = new Label();

    private final Label areaOverwriteModeLabel = new Label();

    private final MenuItem cutItem = new MenuItem("Cut", new FontIconView(CoreIcons.CUT));

    private final MenuItem pasteItem = new MenuItem("Paste", new FontIconView(CoreIcons.PASTE));

    /**
     * Listener for tabSize.
     */
    private final ChangeListener<? super Number> tabSizeListener = (ov, oldV, newV) -> {
        this.getViewModel().updateTextTabValues();
    };

    public AbstractEditorTabView(ShellView<?> shell, T viewModel, ExtendedTextArea textArea) {
        super(shell, viewModel, textArea);
    }

    public void undo() {
        if (this.getFind() != null) {
            this.getFind().getViewModel().resetMatches();
        }
        getTextArea().getUndoManager().undo();
    }

    public void redo() {
        if (this.getFind() != null) {
            this.getFind().getViewModel().resetMatches();
        }
        getTextArea().getUndoManager().redo();
    }

    public void cut() {
        getTextArea().cut();
    }

    public void paste() {
        getTextArea().paste();
    }

    @Override
    protected void build(T viewModel) {
        super.build(viewModel);
        newButton.setTooltip(new Tooltip("New"));
        newButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        clearButton.setTooltip(new Tooltip("Clear"));
        clearButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        cutButton.setTooltip(new Tooltip("Cut"));
        cutButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        pasteButton.setTooltip(new Tooltip("Paste"));
        pasteButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        undoButton.setTooltip(new Tooltip("Undo"));
        undoButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        redoButton.setTooltip(new Tooltip("Redo"));
        redoButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        replaceButton.setTooltip(new Tooltip("Replace"));
        replaceButton.getStyleClass().add(StyleClasses.ICONED_BUTTON);
        var textArea = this.getTextArea();
        textArea.setEditable(true);
        textArea.setParagraphGraphicFactory(LineNumberFactory.get(textArea));
        this.updateAreaMode(false);
        var overwriteModeBox = new HBox(areaOverwriteModeLabel);
        overwriteModeBox.getStyleClass().add("overwrite-mode-box");
        overwriteModeBox.setAlignment(Pos.CENTER);
        var hbox = new HBox(positionLabel, new Separator(Orientation.VERTICAL), overwriteModeBox,
                new Separator(Orientation.VERTICAL));
        hbox.setPadding(new Insets(0, SizeConstants.INSET, 0, 0));
        hbox.setSpacing(SizeConstants.INSET * 2);
        hbox.setAlignment(Pos.CENTER_RIGHT);
        this.getStatusBar().getRightItems().add(0, hbox);
        textArea.getStyleClass().add("editor");
        var css = AbstractEditorTabView.class.getResource("editor.css").toExternalForm();
        this.getContentPane().getStylesheets().add(css);
        cutItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));
        pasteItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));
    }

    @Override
    protected void bind(T viewModel) {
        super.bind(viewModel);
        var textArea = this.getTextArea();
        viewModel.currentParagraphWrapper().bind(textArea.currentParagraphProperty());
        viewModel.currentColumnWrapper().bind(textArea.caretColumnProperty());
        positionLabel.textProperty().bindBidirectional(viewModel.positionTextProperty());
        cutButton.disableProperty().bind(
                Bindings.createBooleanBinding(() -> viewModel.selectedTextProperty().get() == null
                        || viewModel.selectedTextProperty().get().isEmpty(),
                viewModel.selectedTextProperty()
            )
        );
        var undoProperty = new SimpleBooleanProperty();
        undoProperty.bind(textArea.getUndoManager().undoAvailableProperty());
        undoButton.disableProperty().bind(Bindings.not(undoProperty));
        var redoProperty = new SimpleBooleanProperty();
        redoProperty.bind(textArea.getUndoManager().redoAvailableProperty());
        redoButton.disableProperty().bind(Bindings.not(redoProperty));
    }

    @Override
    protected void addListeners(T viewModel) {
        super.addListeners(viewModel);
        this.getTextArea().overwriteModeProperty().addListener((ov, oldV, newV) -> this.updateAreaMode(newV));
        var viewerSettings = viewModel.getSettings();
        viewerSettings.getTabSymbol().sizeProperty().addListener(tabSizeListener);
    }

    @Override
    protected void addHandlers(T viewModel) {
        super.addHandlers(viewModel);
        //shift selected text right/left on tab/shift+tab
        getTextArea().addEventFilter(KeyEvent.KEY_PRESSED, (e) -> this.shiftTextForTab(e));
        this.clearButton.setOnAction((t) -> this.getTextArea().clear());
        this.cutButton.setOnAction(e -> cut());
        this.pasteButton.setOnAction(e -> paste());
        this.undoButton.setOnAction(e -> undo());
        this.redoButton.setOnAction(e -> redo());
        this.replaceButton.setOnAction(e -> viewModel.addFindPane(true));
        this.getTextAreaMenu().setOnShowing((t) -> {
            if (this.getTextArea().getShowCaret() != Caret.CaretVisibility.OFF && this.getTextArea().isEditable()) {
                this.getCutItem().setDisable(false);
                this.getPasteItem().setDisable(false);
            } else {
                this.getCutItem().setDisable(true);
                this.getPasteItem().setDisable(true);
            }
        });
        cutItem.setOnAction((t) -> getTextArea().cut());
        pasteItem.setOnAction((t) -> getTextArea().paste());
        getTextArea().addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
            cutItem.setDisable(!viewModel.isCutItemValid());
            pasteItem.setDisable(!viewModel.isPasteItemValid());
        });
    }

    @Override
    protected void removeListeners(T viewModel) {
        super.removeListeners(viewModel);
        var viewerSettings = viewModel.getSettings();
        viewerSettings.getTabSymbol().sizeProperty().removeListener(tabSizeListener);
    }

    protected Button getNewButton() {
        return newButton;
    }

    protected Button getClearButton() {
        return clearButton;
    }

    protected Button getCutButton() {
        return cutButton;
    }

    protected Button getPasteButton() {
        return pasteButton;
    }

    protected Button getUndoButton() {
        return undoButton;
    }

    protected Button getRedoButton() {
        return redoButton;
    }

    protected Button getReplaceButton() {
        return replaceButton;
    }

    protected MenuItem getCutItem() {
        return cutItem;
    }

    protected MenuItem getPasteItem() {
        return pasteItem;
    }

    private void updateAreaMode(boolean overwrite) {
        if (overwrite) {
            this.areaOverwriteModeLabel.setText("OVR");
        } else {
            this.areaOverwriteModeLabel.setText("INS");
        }
    }

    private void shiftTextForTab(KeyEvent event) {
        var textArea = this.getTextArea();
        if (event.getCode() == KeyCode.TAB) {
            final var viewerSettings = getViewModel().getSettings();
            var spaceCount = viewerSettings.getTabSymbol().getSize();
            //selection shows start and end in the whole text
            var selection = textArea.getSelection();
            //position gives the line - y(major) and position in this line - x(minor).
            var selectionStartPosition = textArea.offsetToPosition(selection.getStart(), null);
            var selectionEndPosition = textArea.offsetToPosition(selection.getEnd(), null);
            //poistion M, 0
            var startParagraph = textArea.offsetToPosition(selection.getStart(), TwoDimensional.Bias.Backward);
            //position: N, till the end of the line
            var endParagraph = textArea.offsetToPosition(selection.getEnd(), TwoDimensional.Bias.Forward);
            //now we need to know the length of the string (without spaces) before selection on start and end
            var t = textArea.getText(selectionStartPosition.getMajor(), 0,
                    selectionStartPosition.getMajor(), selectionStartPosition.getMinor());
            var noSpaceSelectionStartOffset = StringUtils.ltrim(t).length();
            t = textArea.getText(selectionEndPosition.getMajor(), 0,
                    selectionEndPosition.getMajor(), selectionEndPosition.getMinor());
            var noSpaceSelectionEndOffset = StringUtils.ltrim(t).length();

            var mc = textArea.createMultiChange(); // group the changes into one for undo/redo
            IntConsumer tabAction; // takes paragraph number p as a parameter

            if (!event.isShiftDown()) {
                if (viewerSettings.getTabSymbol().isUseSpaces()) {
                    tabAction = p -> mc.insertText(p, 0, this.getViewModel().getTabSpaceString());
                } else {
                    tabAction = p -> mc.insertText(p, 0, "\t");
                }
            } else {
                spaceCount = spaceCount * -1;
                tabAction = p -> { // remove tabs/spaces if present
                    var match = this.getViewModel().getTabOrSpacePattern()
                            .matcher(textArea.getText(p, 0, p, viewerSettings.getTabSymbol().getSize()));
                    if (match.find()) {
                        mc.deleteText(p, 0, p, match.group().length());
                    }
                };
            }

            for (var p = startParagraph.getMajor(); p <= endParagraph.getMajor(); p++) {
                if (textArea.getParagraph(p).length() > 0) {
                    tabAction.accept(p);
                }
            }

            //user can press shift+tab when there is no space to shift left
            if (mc.hasChanges()) {
                mc.commit();
            }

            var startMinor = selectionStartPosition.getMinor() + spaceCount;
            //when we shift left
            if (startMinor < noSpaceSelectionStartOffset) {
                startMinor = noSpaceSelectionStartOffset;
            }
            var endMinor = selectionEndPosition.getMinor() + spaceCount;
            //when we shift left
            if (endMinor < noSpaceSelectionEndOffset) {
                endMinor = noSpaceSelectionEndOffset;
            }
            // Reselect the original selection for further tab actions
            textArea.selectRange(selectionStartPosition.getMajor(), startMinor,
                    selectionEndPosition.getMajor(), endMinor);

            event.consume();
        }
    }
}
