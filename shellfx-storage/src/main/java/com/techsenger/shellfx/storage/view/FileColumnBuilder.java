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

package com.techsenger.shellfx.storage.view;

import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.table.NamedTableColumn;
import com.techsenger.shellfx.material.table.TextFieldTableCell;
import com.techsenger.shellfx.storage.StorageFile;
import com.techsenger.shellfx.storage.style.FileDecorator;
import com.techsenger.toolkit.core.file.FileUtils;
import java.time.Year;
import java.util.Comparator;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

/**
 * Builds the columns that show files, whose cells resolve the icon and the style of the files they are about to show.
 *
 * @author Pavel Castornii
 * @param <F> the type of files
 */
public class FileColumnBuilder<F extends StorageFile> {

    private final Font font;

    private final FileDecorator<F> decorator;

    public FileColumnBuilder(Font font, FileDecorator<F> decorator) {
        this.font = font;
        this.decorator = decorator;
    }

    /**
     * Builds name column.
     *
     * @return
     */
    public NamedTableColumn<F, F> buildNameColumn() {
        var nameColumn = new NamedTableColumn<F, F>(FileColumns.NAME, "Name");
        nameColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper(data.getValue()));
        var converter = new FileStringConverter<F>();
        nameColumn.setCellFactory(col -> new TextFieldTableCell<F, F>(converter) {

            private final FontIconView iconView = new FontIconView();

            private final FileCellUpdater<F> updater =
                    new FileCellUpdater<>(this, iconView, decorator, () -> updateItem(getItem(), isEmpty()));

            @Override
            public void updateItem(F file, boolean empty) {
                super.updateItem(file, empty);
                if (file == null || empty) {
                    updater.clear();
                    setGraphic(null);
                    setText(null);
                } else {
                    updater.update(file);
                    setGraphic(iconView);
                    setText(file.getName());
                }
            }

            @Override
            protected HBox buildEditGraphic() {
                var box = new HBox(iconView, getTextField());
                return box;
            }

            @Override
            protected void updateDisplay() {
                var file = getItem();
                if (file != null) {
                    setGraphic(iconView);
                    setText(file.getName());
                }
            }
        });
        nameColumn.setComparator(Comparator.comparing(StorageFile::getName, String.CASE_INSENSITIVE_ORDER));
        return nameColumn;
    }

    /**
     * Builds size column.
     *
     * @return
     */
    public NamedTableColumn<F, F> buildSizeColumn() {
        var sizeColumn = new NamedTableColumn<F, F>(FileColumns.SIZE, "Size");
        sizeColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper(data.getValue()));
        sizeColumn.setCellFactory(callBack -> new TableCell<F, F>() {

                private final FileCellUpdater<F> updater =
                        new FileCellUpdater<>(this, decorator, () -> updateItem(getItem(), isEmpty()));

                @Override
                protected void updateItem(F file, boolean empty) {
                    super.updateItem(file, empty);
                    if (file == null || empty) {
                        updater.clear();
                        setText(null);
                    } else {
                        updater.update(file, file.sizeProperty());
                        setText(file.getSize() != null ? FileUtils.formatSize(file.getSize()) : null);
                    }
                }
            }
        );
        sizeColumn.setComparator(Comparator.comparingLong(file -> file.getSize() != null ? file.getSize() : 0));
        sizeColumn.setMaxWidth(this.font.getSize() * 6);
        sizeColumn.setMinWidth(this.font.getSize() * 6);
        sizeColumn.setResizable(false);
        return sizeColumn;
    }

    /**
     * Builds last modified column.
     *
     * @return
     */
    public NamedTableColumn<F, F> buildLastModifiedColumn() {
        var lastModifiedColumn =
                new NamedTableColumn<F, F>(FileColumns.LAST_MODIFIED, "Modified");
        lastModifiedColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper(data.getValue()));

        final var currentYear = Year.now();
        lastModifiedColumn.setCellFactory(col -> new TableCell<F, F>() {

            private final FileCellUpdater<F> updater =
                    new FileCellUpdater<>(this, decorator, () -> updateItem(getItem(), isEmpty()));

            @Override
            protected void updateItem(F file, boolean empty) {
                super.updateItem(file, empty);
                if (file == null || empty) {
                    updater.clear();
                    setText(null);
                } else {
                    updater.update(file, file.modifiedTimeProperty());
                    setText(file.getModifiedTime() != null
                            ? DateTimeUtils.format(file.getModifiedTime(), currentYear) : null);
                }
            }
        });
        lastModifiedColumn.setComparator(Comparator.comparing(StorageFile::getModifiedTime,
                Comparator.nullsLast(Comparator.naturalOrder())));
        lastModifiedColumn.setMaxWidth(this.font.getSize() * 8);
        lastModifiedColumn.setMinWidth(this.font.getSize() * 8);
        lastModifiedColumn.setResizable(false);
        return lastModifiedColumn;
    }

    protected Font getFont() {
        return font;
    }

    protected FileDecorator<F> getDecorator() {
        return decorator;
    }
}
