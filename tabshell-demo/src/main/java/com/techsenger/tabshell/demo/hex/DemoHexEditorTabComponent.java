package com.techsenger.tabshell.demo.hex;

///*
// * Copyright 2024-2026 Pavel Castornii.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.techsenger.tabshell.demos.full.hex;
//
//import com.techsenger.patternfx.core.Name;
//import com.techsenger.tabshell.core.ShellComponent;
//import com.techsenger.tabshell.demos.full.DemoComponentNames;
//import com.techsenger.tabshell.hex.editor.HexEditorTabComponent;
//
///**
// *
// * @author Pavel Castornii
// */
//public class DemoHexEditorTabComponent extends HexEditorTabComponent<DemoHexEditorTabView> {
//
//    protected class Mediator extends HexEditorTabComponent.Mediator implements DemoHexEditorTabMediator {
//
//        @Override
//        public DemoHexToolBarViewModel getToolBar() {
//            return (DemoHexToolBarViewModel) super.getToolBar();
//        }
//    }
//
//    public DemoHexEditorTabComponent(DemoHexEditorTabView view, ShellComponent<?> shell) {
//        super(view, shell);
//    }
//
//    @Override
//    protected void postInitialize() {
//        super.postInitialize();
//        var toolBar = getToolBar().getView();
//        var area = getArea();
//        var caret = getArea().getCaret();
//        toolBar.getCaretShapeComboBox().valueProperty()
//                .bindBidirectional(caret.getView().getViewModel().shapeProperty());
//        toolBar.getColumnSeparatorComboBox().valueProperty()
//                .bindBidirectional(area.getView().getViewModel().columnSeparatorProperty());
//    }
//
//    @Override
//    public DemoHexToolBarComponent getToolBar() {
//        return (DemoHexToolBarComponent) super.getToolBar();
//    }
//
//    @Override
//    public Name getName() {
//        return DemoComponentNames.DEMO_HEX_EDITOR_TAB;
//    }
//
//    @Override
//    protected DemoHexToolBarComponent createToolBar() {
//        var vm = new DemoHexToolBarViewModel();
//        var v = new DemoHexToolBarView(vm);
//        var c = new DemoHexToolBarComponent(v);
//        return c;
//    }
//}
