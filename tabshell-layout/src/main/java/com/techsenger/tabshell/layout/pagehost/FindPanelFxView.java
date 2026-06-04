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

package com.techsenger.tabshell.layout.pagehost;

import atlantafx.base.theme.Styles;
import com.techsenger.tabshell.material.style.Spacing;
import com.techsenger.tabshell.shared.find.AbstractFindBaseFxView;
import com.techsenger.tabshell.shared.find.FindTrigger;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 *
 * @author Pavel Castornii
 */
public class FindPanelFxView<P extends FindPanelPresenter<?>> extends AbstractFindBaseFxView<P>
        implements FindPanelView {

    private final HBox box = new HBox(getFindComboBoxWrapper());

    public FindPanelFxView() {
        super(FindTrigger.ON_TYPE);
    }

    @Override
    public HBox getNode() {
        return box;
    }

    @Override
    protected void build() {
        super.build();
        box.getStyleClass().add("find-box");
        box.setPadding(new Insets(Spacing.getVertical(), Spacing.getHorizontal(),
                Spacing.getVertical(), Spacing.getHorizontal()));
        HBox.setHgrow(getFindComboBoxWrapper(), Priority.ALWAYS);
        getFindComboBox().setMaxWidth(Double.MAX_VALUE);
        getFindComboBoxWrapper().setMinWidth(100);
        getFindComboBoxWrapper().setPadding(Insets.EMPTY);
        getFindComboBox().getStyleClass().add(Styles.DENSE);
        getFindRightBox().getStyleClass().add(Styles.DENSE);
    }
}
