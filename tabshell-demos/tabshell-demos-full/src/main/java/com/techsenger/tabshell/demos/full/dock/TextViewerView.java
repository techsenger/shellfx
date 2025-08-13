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

package com.techsenger.tabshell.demos.full.dock;

import com.techsenger.tabshell.core.pane.AbstractPaneView;
import com.techsenger.tabshell.demos.full.Text;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Pavel Castornii
 */
public class TextViewerView extends AbstractPaneView<TextViewerViewModel> {

    private final TextArea textArea = new TextArea(Text.INSTANCE);

    private final StackPane stackPane = new StackPane(textArea);

    public TextViewerView(TextViewerViewModel viewModel) {
        super(viewModel);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public Region getNode() {
        return stackPane;
    }

    @Override
    protected void build(TextViewerViewModel viewModel) {
        super.build(viewModel);
        textArea.setWrapText(true);
    }


}
