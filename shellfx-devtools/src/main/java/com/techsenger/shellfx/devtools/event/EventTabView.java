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

package com.techsenger.shellfx.devtools.event;

import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.core.tab.AbstractTabView;
import com.techsenger.shellfx.material.style.StyleClasses;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import jfx.incubator.scene.control.richtext.RichTextArea;

/**
 *
 * @author Pavel Castornii
 */
public class EventTabView<VM extends EventTabViewModel<?>> extends AbstractTabView<VM> {

    public class Composer extends AbstractTabView<VM>.Composer implements EventTabComposer {

        private EventToolBarView<?> toolBar;

        @Override
        public void compose() {
            super.compose();
            toolBar = createToolBar();
            getModifiableChildren().add(toolBar);
            getContentBox().getChildren().add(0, toolBar.getNode());
        }

        @Override
        public FullEventToolBarPort getToolBarPort() {
            return toolBar == null ? null : toolBar.getViewModel();
        }

        protected EventToolBarView<?> createToolBar() {
            var params = new EventToolBarParams(getViewModel().new ToolBarAwarePortImpl());
            var viewModel = new EventToolBarViewModel<>(params);
            var toolBarView = new EventToolBarView<>(viewModel);
            toolBarView.initialize();
            return toolBarView;
        }
    }

    /**
     * JFX RichTextArea and JFX ListView generates too many events (NodeAdd, NodeRemove), so we use RTFX text area.
     */
    private final RichTextArea textArea = new RichTextArea();

    public EventTabView(VM viewModel, ShellView<?> shell) {
        super(viewModel, shell);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new EventTabView.Composer();
    }

    @Override
    protected void build() {
        super.build();
        textArea.setEditable(false);
        textArea.getStyleClass().add(StyleClasses.MONOSPACE);
        VBox.setVgrow(textArea, Priority.ALWAYS);
        getContentBox().getChildren().add(textArea);
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        getViewModel().getAppendTextSource().addListener(textArea::appendText);
        getViewModel().getClearTextSource().addListener((v) -> textArea.clear());
    }
}
