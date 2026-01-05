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

package com.techsenger.tabshell.text.editor;

import com.techsenger.tabshell.core.menu.SimpleMenuItemHelper;
import com.techsenger.tabshell.shared.menu.EditMenuNames;
import com.techsenger.tabshell.storage.GenericFile;
import com.techsenger.tabshell.storage.UriUtils;
import com.techsenger.tabshell.text.viewer.AbstractViewerTabViewModel;
import java.util.regex.Pattern;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.input.Clipboard;

/**
 * Abstract class for editors in exported package.
 *
 * @author Pavel Castornii
 */
public abstract class AbstractEditorTabViewModel<T extends EditorTabMediator> extends AbstractViewerTabViewModel<T> {

    /**
     * Current paragraph (it corresponds to a single line when the text is not wrapped or spans multiple lines when the
     * text is wrapped).
     */
    private ReadOnlyIntegerWrapper currentParagraph = new ReadOnlyIntegerWrapper();

    private ReadOnlyIntegerWrapper currentColumn = new ReadOnlyIntegerWrapper();

    /**
     * Clipboard. It is used for resolving (context) menu items state.
     */
    private final Clipboard clipboard = Clipboard.getSystemClipboard();

    /**
     * Pattern to see if a line starts with a tab or 1-N spaces.
     */
    private Pattern tabOrSpacePattern;

    /**
     * String with " " with length equal to tab size.
     */
    private String tabSpaceString;

    private SimpleStringProperty positionText = new SimpleStringProperty();

    public AbstractEditorTabViewModel(GenericFile file) {
        super(file);
    }

    public Clipboard getClipboard() {
        return clipboard;
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.updateTextTabValues();
        this.currentParagraph.addListener((ov, oldV, newV)
                -> this.updatePosition(newV.intValue(), this.currentColumn.get()));
        this.currentColumn.addListener((ov, oldV, newV)
                -> this.updatePosition(this.currentParagraph.get(), newV.intValue()));
        //setting initial value
        this.updatePosition(0, 0);
        addMenuItemHelpers(new SimpleMenuItemHelper(EditMenuNames.UNDO, Boolean.TRUE) {
                @Override
                public Boolean getItemValid() {
                    return getUndoManager().isUndoAvailable();
                }
            },
            new SimpleMenuItemHelper(EditMenuNames.REDO, Boolean.TRUE) {
                @Override
                public Boolean getItemValid() {
                    return getUndoManager().isRedoAvailable();
                }
            },
            new SimpleMenuItemHelper(EditMenuNames.CUT, Boolean.TRUE) {
                @Override
                public Boolean getItemValid() {
                    return isCutItemValid();
                }
            },
            new SimpleMenuItemHelper(EditMenuNames.PASTE, Boolean.TRUE) {
                @Override
                public Boolean getItemValid() {
                    return isPasteItemValid();
                }
            },
            new SimpleMenuItemHelper(EditMenuNames.GO_TO_LINE, true, true)
        );
        modifiedProperty().addListener((ov, oldV, newV) -> {
            updateTitle();
            updateTooltip();
        });
        fileProperty().addListener((ov, oldV, newV) -> {
            updateTitle();
            updateTooltip();
        });
    }

    protected boolean isCutItemValid() {
        return !selectedTextProperty().get().isEmpty();
    }

    protected boolean isPasteItemValid() {
        return this.getClipboard().getString() != null;
    }

    protected StringProperty positionTextProperty() {
        return positionText;
    }

    protected String getPositionText() {
        return positionText.get();
    }

    protected void setPositionText(String text) {
        this.positionText.set(text);
    }

    protected ReadOnlyIntegerProperty currentParagraphProperty() {
        return currentParagraph.getReadOnlyProperty();
    }

    protected int getCurrentParagraph() {
        return currentParagraphProperty().get();
    }

    protected ReadOnlyIntegerProperty currentColumnProperty() {
        return currentColumn.getReadOnlyProperty();
    }

    protected int getCurrentColumn() {
        return currentColumnProperty().get();
    }

    protected Pattern getTabOrSpacePattern() {
        return tabOrSpacePattern;
    }

    protected String getTabSpaceString() {
        return tabSpaceString;
    }

    protected void updateTitle() {
        var file = getFile();
        var modified = isModified();
        if (file == null) {
            setTitle("");
        }
        if (modified) {
            setTitle("*" + file.getName());
        } else {
            setTitle(file.getName());
        }
    }

    protected void updateTooltip() {
        var file = getFile();
        if (file == null) {
            setTooltip("");
        } else {
            setTooltip(UriUtils.toHumanString(file.getUri()));
        }
    }

    ReadOnlyIntegerWrapper currentParagraphWrapper() {
        return currentParagraph;
    }

    ReadOnlyIntegerWrapper currentColumnWrapper() {
        return currentColumn;
    }

    void updateTextTabValues() {
        var tabSize = getSettings().getTabSymbol().getSize();
        tabOrSpacePattern =  this.createTabOrSpacePattern(tabSize);
        tabSpaceString = this.createTabSpaceString(tabSize);
    }

    private Pattern createTabOrSpacePattern(int tabSize) {
        return Pattern.compile("^(\t| {1," + tabSize + "})");
    }

    private String createTabSpaceString(int tabSize) {
        return " ".repeat(tabSize);
    }

    private void updatePosition(int paragraph, int column) {
        this.positionText.set("Ln " + (paragraph + 1) + ", Col " + (column + 1));
    }
}
