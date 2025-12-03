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

package com.techsenger.tabshell.demos.full.hex;

import com.techsenger.tabshell.hex.editor.HexEditorTabComposer;
import com.techsenger.tabshell.hex.editor.HexToolBarViewModel;

/**
 *
 * @author Pavel Castornii
 */
public class DemoHexEditorTabComposer extends HexEditorTabComposer<DemoHexEditorTabView> {

    protected class Mediator extends HexEditorTabComposer.Mediator implements DemoHexEditorTabMediator {

        public Mediator() {
            super();
        }

        @Override
        public DemoHexToolBarViewModel getToolBar() {
            return (DemoHexToolBarViewModel) super.getToolBar();
        }

        @Override
        protected HexToolBarViewModel createToolBar() {
            return new DemoHexToolBarViewModel();
        }
    }

    public DemoHexEditorTabComposer(DemoHexEditorTabView view) {
        super(view);
    }

    @Override
    public DemoHexEditorTabMediator getMediator() {
        return (DemoHexEditorTabMediator) super.getMediator();
    }

    @Override
    public void initialize() {
        super.initialize();
        DemoHexToolBarView toolBar = getToolBar();
        toolBar.getCaretShapeComboBox().valueProperty()
                .bindBidirectional(getMediator().getArea().getCaret().shapeProperty());
        toolBar.getColumnSeparatorComboBox().valueProperty()
                .bindBidirectional(getMediator().getArea().columnSeparatorProperty());
    }

    @Override
    public DemoHexToolBarView getToolBar() {
        return (DemoHexToolBarView) super.getToolBar();
    }

    @Override
    protected DemoHexToolBarView createToolBar() {
        return new DemoHexToolBarView(getMediator().getToolBar());
    }

    @Override
    protected DemoHexEditorTabMediator createMediator() {
        return new DemoHexEditorTabComposer.Mediator();
    }


}
