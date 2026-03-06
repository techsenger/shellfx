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

package com.techsenger.tabshell.devtools.event;

import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.tab.AbstractTabFxView;
import com.techsenger.tabshell.material.style.StyleClasses;
import javafx.application.Platform;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.InlineCssTextArea;

/**
 *
 * @author Pavel Castornii
 */
public class EventTabFxView<P extends EventTabPresenter<?, ?>> extends AbstractTabFxView<P> implements EventTabView {

    public class Composer extends AbstractTabFxView<P>.Composer implements EventTabComposer {

        @Override
        public void compose() {
            super.compose();
            toolBar = createToolBar();
            toolBar.getPresenter().initialize();
            getModifiableChildren().add(toolBar);
            getContentBox().getChildren().add(0, toolBar.getNode());
        }

        @Override
        public EventToolBarPort getToolBar() {
            return toolBar.getPresenter();
        }

        protected EventToolBarFxView<?> createToolBar() {
            var view = new EventToolBarFxView<>();
            var presenter = new EventToolBarPresenter<>(view, getPresenter().new ToolBarAwarePortImpl());
            return view;
        }
    }

    /**
     * JFX RichTextArea and JFX ListView generates too many events (NodeAdd, NodeRemove), so we use RTFX text area.
     */
    private final InlineCssTextArea textArea = new InlineCssTextArea();

    private final VirtualizedScrollPane<InlineCssTextArea> textScrollPane = new VirtualizedScrollPane(textArea);

    private EventToolBarFxView<?> toolBar;

    public EventTabFxView(ShellFxView<?> shell) {
        super(shell);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    public void appendText(String text) {
        // called from non-JavaFX thread
        Platform.runLater(() -> {
            this.textArea.appendText(text);
        });
    }

    @Override
    public void clearText() {
        // called from non-JavaFX thread
        Platform.runLater(() -> {
            this.textArea.clear();
        });
    }

    @Override
    protected Composer createComposer() {
        return new EventTabFxView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        textArea.setEditable(false);
        textArea.getStyleClass().add(StyleClasses.MONOSPACE);
        VBox.setVgrow(textScrollPane, Priority.ALWAYS);
        getContentBox().getChildren().add(textScrollPane);
    }
}
