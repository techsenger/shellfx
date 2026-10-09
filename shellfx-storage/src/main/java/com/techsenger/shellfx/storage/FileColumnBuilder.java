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

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.table.NamedTableColumn;
import com.techsenger.shellfx.material.table.TextFieldTableCell;
import com.techsenger.toolkit.core.file.FileUtils;
import java.time.Year;
import java.util.Comparator;
import java.util.function.Supplier;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
     * Builds name column without styling.
     *
     * @return
     */
    public <F extends GenericFile> NamedTableColumn<F, F> buildNameColumn() {
        return buildNameColumn(() -> null);
    }

    /**
     * Builds name column.
     *
     * @param styleResolver supplies the style resolver on every cell update, which may be {@code null}
     * @return
     */
    public <F extends GenericFile> NamedTableColumn<F, F> buildNameColumn(
            Supplier<@Nullable FileStyleResolver<F>> styleResolver) {
        var nameColumn = new NamedTableColumn<F, F>(FileColumns.NAME, "Name");
        nameColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper(data.getValue()));
        var converter = new FileStringConverter<F>();
        nameColumn.setCellFactory(col -> new TextFieldTableCell<F, F>(converter) {

            private final FontIconView iconView = new FontIconView();

            @Override
            public void updateItem(F file, boolean empty) {
                super.updateItem(file, empty);
                var resolver = styleResolver.get();
                if (file == null || empty) {
                    setGraphic(null);
                    setText(null);
                    if (resolver != null) {
                        FileCellUtils.updateStyles(this, iconView, null, resolver);
                    }
                } else {
                    if (resolver != null) {
                        FileCellUtils.updateStyles(this, iconView, file, resolver);
                    }
                    iconView.setIcon(file.getIcon());
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
        nameColumn.setComparator(Comparator.comparing(GenericFile::getName, String.CASE_INSENSITIVE_ORDER));
        return nameColumn;
    }

    /**
     * Builds size column without styling.
     *
     * @return
     */
    public <F extends GenericFile> NamedTableColumn<F, F> buildSizeColumn() {
        return buildSizeColumn(() -> null);
    }

    /**
     * Builds size column.
     *
     * @param styleResolver supplies the style resolver on every cell update, which may be {@code null}
     * @return
     */
    public <F extends GenericFile> NamedTableColumn<F, F> buildSizeColumn(
            Supplier<@Nullable FileStyleResolver<F>> styleResolver) {
        var sizeColumn = new NamedTableColumn<F, F>(FileColumns.SIZE, "Size");
        sizeColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper(data.getValue()));
        sizeColumn.setCellFactory(callBack -> new TableCell<F, F>() {

                @Override
                protected void updateItem(F file, boolean empty) {
                    super.updateItem(file, empty);
                    var resolver = styleResolver.get();
                    if (resolver != null) {
                        FileCellUtils.updateStyles(this, null, empty ? null : file, resolver);
                    }
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
     * Builds last modified column without styling.
     *
     * @return
     */
    public <F extends GenericFile> NamedTableColumn<F, F> buildLastModifiedColumn() {
        return buildLastModifiedColumn(() -> null);
    }

    /**
     * Builds last modified column.
     *
     * @param styleResolver supplies the style resolver on every cell update, which may be {@code null}
     * @return
     */
    public <F extends GenericFile> NamedTableColumn<F, F> buildLastModifiedColumn(
            Supplier<@Nullable FileStyleResolver<F>> styleResolver) {
        var lastModifiedColumn =
                new NamedTableColumn<F, F>(FileColumns.LAST_MODIFIED, "Modified");
        lastModifiedColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper(data.getValue()));

        final var currentYear = Year.now();
        lastModifiedColumn.setCellFactory(col -> new TableCell<F, F>() {
            @Override
            protected void updateItem(F file, boolean empty) {
                super.updateItem(file, empty);
                var resolver = styleResolver.get();
                if (resolver != null) {
                    FileCellUtils.updateStyles(this, null, empty ? null : file, resolver);
                }
                if (file == null || file.getModifiedTime() == null || empty) {
                    setText(null);
                } else {
                    setText(DateTimeUtils.format(file.getModifiedTime(), currentYear));
                }
            }
        });
        lastModifiedColumn.setComparator(Comparator.comparing(GenericFile::getModifiedTime,
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
