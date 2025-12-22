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

package com.techsenger.tabshell.storage;

import com.techsenger.tabshell.material.icon.FontIconView;
import com.techsenger.tabshell.material.icon.GenericFontIcon;
import com.techsenger.tabshell.material.table.NamedTableColumn;
import com.techsenger.toolkit.core.file.FileUtils;
import java.time.Instant;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.TableCell;
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
     * Builds type column.
     *
     * @return
     */
    public NamedTableColumn<GenericFile, GenericFile> buildTypeColumn(GenericFontIcon<?> dirIcon,
            GenericFontIcon fileIcon) {
        var typeColumn = new NamedTableColumn<GenericFile, GenericFile>(FileColumnNames.TYPE, ".");
        typeColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper(data.getValue()));
        typeColumn.setCellFactory(col -> new TableCell<GenericFile, GenericFile>() {

                @Override
                protected void updateItem(GenericFile file, boolean empty) {
                    super.updateItem(file, empty);
                    if (file == null || empty) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        if (file.getType() != null) {
                            if (file.isDirectory()) {
                                this.setGraphic(new FontIconView(dirIcon));
                            } else {
                                this.setGraphic(new FontIconView(fileIcon));
                            }
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            }
        );
        typeColumn.setComparator(Comparator.comparing(GenericFile::isDirectory).reversed());
        typeColumn.getStyleClass().add("type-column");
        typeColumn.setMaxWidth(this.font.getSize() * 1.25 + this.font.getSize() * 0.75); //0.75 = left pad + right pad
        typeColumn.setMinWidth(this.font.getSize() * 1.25 + this.font.getSize() * 0.75);
        typeColumn.setResizable(false);
        return typeColumn;
    }

    /**
     * Builds name column.
     *
     * @return
     */
    public NamedTableColumn<GenericFile, GenericFile> buildNameColumn() {
        var nameColumn = new NamedTableColumn<GenericFile, GenericFile>(FileColumnNames.NAME, "Name");
        nameColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper(data.getValue()));
        nameColumn.setCellFactory(col -> new TableCell<GenericFile, GenericFile>() {

                @Override
                protected void updateItem(GenericFile file, boolean empty) {
                    super.updateItem(file, empty);
                    if (file == null || empty) {
                        setText(null);
                    } else {
                        setText(file.getName());
                    }
                }
            }
        );
        nameColumn.setComparator(Comparator.comparing(GenericFile::getName, String.CASE_INSENSITIVE_ORDER));
        return nameColumn;
    }

    /**
     * Builds size column.
     *
     * @return
     */
    public NamedTableColumn<GenericFile, GenericFile> buildSizeColumn() {
        var sizeColumn = new NamedTableColumn<GenericFile, GenericFile>(FileColumnNames.SIZE, "Size");
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
                new NamedTableColumn<GenericFile, GenericFile>(FileColumnNames.LAST_MODIFIED, "Modified");
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
