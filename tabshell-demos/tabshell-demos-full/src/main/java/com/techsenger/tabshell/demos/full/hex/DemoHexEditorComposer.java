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
public class DemoHexEditorComposer extends HexEditorTabComposer<DemoHexEditorTabView>
        implements DemoHexEditorTabView.Composer {

    protected class ViewModelComposer extends HexEditorTabComposer.ViewModelComposer
            implements DemoHexEditorTabViewModel.Composer {

        public ViewModelComposer() {
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

    public DemoHexEditorComposer(DemoHexEditorTabView view) {
        super(view);
    }

    @Override
    public ViewModelComposer getViewModelComposer() {
        return (ViewModelComposer) super.getViewModelComposer();
    }

    @Override
    public void initialize() {
        super.initialize();
        DemoHexToolBarView toolBar = getToolBar();
        toolBar.getCaretShapeComboBox().valueProperty()
                .bindBidirectional(getViewModelComposer().getArea().getCaret().shapeProperty());
        toolBar.getColumnSeparatorComboBox().valueProperty()
                .bindBidirectional(getViewModelComposer().getArea().columnSeparatorProperty());
    }

    @Override
    public DemoHexToolBarView getToolBar() {
        return (DemoHexToolBarView) super.getToolBar();
    }

    @Override
    protected DemoHexToolBarView createToolBar() {
        return new DemoHexToolBarView(getViewModelComposer().getToolBar());
    }

    @Override
    protected ViewModelComposer createViewModelComposer() {
        return new ViewModelComposer();
    }
}
