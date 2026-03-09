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

package com.techsenger.tabshell.demo.page;

import com.techsenger.tabshell.core.page.AbstractPageFxView;
import com.techsenger.tabshell.material.style.SizeConstants;
import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author Pavel Castornii
 */
public class DemoPageFxView extends AbstractPageFxView<DemoPagePresenter> implements DemoPageView {

    private final TextArea textArea = new TextArea();

    private final VBox box = new VBox(getTitleBox(), textArea);

    public DemoPageFxView(Insets padding, int index) {
        this.box.setPadding(padding);
        this.textArea.setText("Page " + index + " text: " + Text.INSTANCE);
    }

    @Override
    public void requestFocus() {

    }

    @Override
    public VBox getNode() {
        return box;
    }

    @Override
    protected void build() {
        super.build();
        textArea.setWrapText(true);
        VBox.setVgrow(textArea, Priority.ALWAYS);
        box.setSpacing(SizeConstants.INSET);
    }
}
