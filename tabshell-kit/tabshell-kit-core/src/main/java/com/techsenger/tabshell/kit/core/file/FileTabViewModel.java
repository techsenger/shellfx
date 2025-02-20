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

package com.techsenger.tabshell.kit.core.file;

import com.techsenger.tabshell.kit.core.workertab.TabWorker;
import com.techsenger.toolkit.core.file.FileUtils;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javafx.concurrent.Worker;
import javafx.event.Event;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains base logic for file reading/writing. There can be different file types - text, binary, image etc.
 *
 * @param <T> the type of content.
 * @author Pavel Castornii
 */
public interface FileTabViewModel<T> {

    default void openFile(Window window, Path initialPath) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(FileChooserTitles.OPEN);
        var fileInfo = this.getFileInfo();
        if (!fileInfo.isRemote() && fileInfo.getPath() != null) {
            fileChooser.setInitialDirectory(Paths.get(fileInfo.getPath()).toFile().getParentFile());
        } else {
            fileChooser.setInitialDirectory(initialPath.toFile());
        }
        fileChooser.getExtensionFilters().addAll(this.getExtensionFilters());
        var file = fileChooser.showOpenDialog(window);
        if (file != null) {
            fileInfo.setName(file.getName());
            fileInfo.setPath(file.getAbsolutePath());
            var extension = FileUtils.getExtension(file.getName());
            fileInfo.setExtension(extension);
            fileInfo.setRemote(false);
            setTitle(fileInfo.getName());
            setTooltip(file.getAbsolutePath());
            readFile();
        }
    }

    default boolean saveFile(Window window) {
        var fileInfo = this.getFileInfo();
        if (fileInfo != null) {
            this.writeFile();
            return true;
        } else {
            //return this.saveFileAs(window);
            return false;
        }
    }

    /**
     * This method can be used only for local files.
     *
     * @param window
     * @return
     */
    default boolean saveFileAs(Window window, Path initialPath) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(FileChooserTitles.SAVE_AS);
        fileChooser.getExtensionFilters().addAll(this.getExtensionFilters());
        var fileInfo = this.getFileInfo();
        var filePath = fileInfo.getPath();
        //fileChooser.getExtensionFilters().addAll(createSaveExtensionFilters());
        if (filePath == null) {
            fileChooser.setInitialDirectory(initialPath.toFile());
        } else {
            var path = Paths.get(filePath);
            fileChooser.setInitialDirectory(path.toFile().getParentFile());
        }
        var file = fileChooser.showSaveDialog(window);
        if (file != null) {
            var path = file.toPath();
            fileInfo.setPath(file.getAbsolutePath());
            fileInfo.setName(file.getName());
            fileInfo.setRemote(false);
            this.writeFile();
            this.setTitle(fileInfo.getName());
            this.setTooltip(fileInfo.getPath());
            return true;
        } else {
            return false;
        }
    }

    default void readFile() {
        if (this.getFileInfo() == null || this.getFileInfo().getPath() == null) {
            return;
        }
        var task = this.getFileTaskProvider().createFileReader(this.getFileInfo());
        task.stateProperty().addListener((ob, oldV, newV) -> {
            if (newV == Worker.State.SUCCEEDED) {
                this.setContentModified(false);
                this.setContent(task.getValue());
            } else if (newV == Worker.State.FAILED) {
                LogHolder.logger().warn("Error reading file {}", this.getFileInfo().getPath(), task.getException());
            }
        });
        this.submitWorker(task);
    }

    default void writeFile() {
        FileInfo fileInfo = this.getFileInfo();
        var content = this.getContent();
        var task = this.getFileTaskProvider().createFileWriter(fileInfo, content);
        task.stateProperty().addListener((ob, oldV, newV) -> {
            if (newV == Worker.State.FAILED) {
                LogHolder.logger().warn("Error writing file {}", fileInfo.getPath(), task.getException());
            }
        });
        this.submitWorker(task);
        this.setContentModified(false);
    }

    default String resolveTabTitle(String title) {
        if (title == null) {
            return null;
        }
        if (isContentModified()) {
            if (!title.endsWith("*")) {
                return title + "*";
            } else {
                return title;
            }
        } else {
            if (title.endsWith("*")) {
                return title.substring(0, title.length() - 1);
            } else {
                return title;
            }
        }
    }

    default void doOnTabClosed(Event event, Window window) {
        if (this.getFileInfo().getPath() != null && this.isContentModified()) {
            var i = 0;
            //TODO
//            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Save changes to file before closing?",
//                    ButtonType.NO, ButtonType.CANCEL, ButtonType.YES);
//            FxUtils.makeFixedButtonOrder(alert);
//            this.getContext().getTabShell().setCssTo(alert);
//            alert.setTitle("Confirmation");
//            alert.setHeaderText(null);
//            alert.showAndWait();
//            if (alert.getResult() == ButtonType.YES) {
//                if (!this.saveFile(window)) {
//                    event.consume();
//                }
//            } else if (alert.getResult() == ButtonType.CANCEL) {
//                event.consume();
//            }
        }
    }

    FileInfo getFileInfo();

    FileTaskProvider<T> getFileTaskProvider();

    boolean isContentModified();

    void setContentModified(boolean modified);

    T getContent();

    void setContent(T content);

    void setTitle(String text);

    void setTooltip(String tooltip);

    void submitWorker(TabWorker<?> worker);

    List<FileChooser.ExtensionFilter> getExtensionFilters();

    /**
     * Resolves default extension - the extension that will be used, when user doesn't provide extension in file name.
     *
     * @param filter
     * @return
     */
    String resolveDefaultExtension(FileChooser.ExtensionFilter filter);

    /**
     * Returns default extension.
     * @return
     */
    String getDefaultExtension();
}

final class LogHolder {

    private static final Logger logger = LoggerFactory.getLogger(FileTabViewModel.class);

    static Logger logger() {
        return logger;
    }

    private LogHolder() {
        //empty
    }
}
