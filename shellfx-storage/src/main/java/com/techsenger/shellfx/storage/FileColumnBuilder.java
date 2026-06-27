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

package com.techsenger.shellfx.storage;

import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.icon.GenericFontIcon;
import com.techsenger.shellfx.material.table.NamedTableColumn;
import com.techsenger.shellfx.material.table.TextFieldTableCell;
import com.techsenger.toolkit.core.file.FileUtils;
import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

/**
 *
 * @author Pavel Castornii
 */
public class FileColumnBuilder {

    private final Font font;

    public FileColumnBuilder(Font font) {
        this.font = font;
    }

    /**
     * Builds name column.
     *
     * @return
     */
    public NamedTableColumn<GenericFile, GenericFile> buildNameColumn(GenericFontIcon<?> dirIcon,
            GenericFontIcon fileIcon) {
        var nameColumn = new NamedTableColumn<GenericFile, GenericFile>(FileColumns.NAME, "Name");
        nameColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper(data.getValue()));
        var converter = new FileStringConverter();
        nameColumn.setCellFactory(col -> new TextFieldTableCell<GenericFile, GenericFile>(converter) {

            @Override
            public void updateItem(GenericFile file, boolean empty) {
                super.updateItem(file, empty);
                if (file == null || empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setGraphic(buildIcon(file));
                    setText(file.getName());
                }
            }

            @Override
            protected HBox buildEditGraphic() {
                var icon = buildIcon(getItem());
                var box = new HBox(getTextField());
                if (icon != null) {
                    box.getChildren().add(0, icon);
                }
                return box;
            }

            @Override
            protected void updateDisplay() {
                var file = getItem();
                if (file != null) {
                    setGraphic(buildIcon(file));
                    setText(file.getName());
                }
            }

            private Node buildIcon(GenericFile file) {
                if (file.getEntryType() == null) {
                    return null;
                }
                return new FontIconView(file.isDirectory() ? dirIcon : fileIcon);
            }
        });
        nameColumn.setComparator(Comparator.comparing(GenericFile::getName, String.CASE_INSENSITIVE_ORDER));
        return nameColumn;
    }

    /**
     * Builds size column.
     *
     * @return
     */
    public NamedTableColumn<GenericFile, GenericFile> buildSizeColumn() {
        var sizeColumn = new NamedTableColumn<GenericFile, GenericFile>(FileColumns.SIZE, "Size");
        sizeColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper(data.getValue()));
        sizeColumn.setCellFactory(callBack -> new TableCell<GenericFile, GenericFile>() {

                @Override
                protected void updateItem(GenericFile file, boolean empty) {
                    super.updateItem(file, empty);
                    if (file == null || file.getSize() == null || empty) {
                        setText(null);
                    } else {
                        setText(FileUtils.formatSize(file.getSize()));
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
     * @return
     */
    public NamedTableColumn<GenericFile, GenericFile> buildLastModifiedColumn() {
        var lastModifiedColumn =
                new NamedTableColumn<GenericFile, GenericFile>(FileColumns.LAST_MODIFIED, "Modified");
        lastModifiedColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper(data.getValue()));
        final DateTimeFormatter currentYearformatter = DateTimeFormatter.ofPattern("MMM dd HH:mm");
        final DateTimeFormatter otherYearformatter = DateTimeFormatter.ofPattern("MMM dd yyyy");
        final int currentYear = Year.now().getValue();
        lastModifiedColumn.setCellFactory(col -> new TableCell<GenericFile, GenericFile>() {
            @Override
            protected void updateItem(GenericFile file, boolean empty) {
                super.updateItem(file, empty);
                if (file == null || file.getLastModified() == null || empty) {
                    setText(null);
                } else {
                    var zonedDateTime = Instant.ofEpochMilli(file.getLastModified()).atZone(ZoneId.systemDefault());
                    if (currentYear == zonedDateTime.getYear()) {
                        setText(currentYearformatter.format(zonedDateTime));
                    } else {
                        setText(otherYearformatter.format(zonedDateTime));
                    }
                }
            }
        });
        lastModifiedColumn.setComparator(Comparator.comparing(GenericFile::getLastModified,
                Comparator.nullsLast(Comparator.naturalOrder())));
        lastModifiedColumn.setMaxWidth(this.font.getSize() * 8);
        lastModifiedColumn.setMinWidth(this.font.getSize() * 8);
        lastModifiedColumn.setResizable(false);
        return lastModifiedColumn;
    }

    protected Font getFont() {
        return font;
    }
}
