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

package com.techsenger.tabshell.hex.editor;

import com.techsenger.patternfx.core.ComponentName;
import com.techsenger.tabshell.core.area.AbstractAreaComponent;
import com.techsenger.tabshell.hex.HexComponentNames;

/**
 *
 * @author Pavel Castornii
 */
public class HexAreaComponent<T extends HexAreaView<?, ?>> extends AbstractAreaComponent<T> {

    protected class Mediator extends AbstractAreaComponent.Mediator implements HexAreaMediator {

        @Override
        public HexToolBarViewModel getToolBar() {
            return toolbar.getView().getViewModel();
        }

        @Override
        public CaretViewModel getCaret() {
            return caret.getView().getViewModel();
        }

    }

    private final HexToolBarComponent<?> toolbar;

    private final CaretComponent<?> caret;

    public HexAreaComponent(T view, HexToolBarComponent<?> toolbar) {
        super(view);
        this.toolbar = toolbar;
        this.caret = createCaret();
        getModifiableChildren().add(this.caret);
    }

    public HexToolBarComponent<?> getToolbar() {
        return toolbar;
    }

    public CaretComponent<?> getCaret() {
        return caret;
    }

    @Override
    protected void preInitialize() {
        super.preInitialize();
        this.caret.initialize();
    }

    @Override
    protected Mediator createMediator() {
        return new Mediator();
    }

    @Override
    public ComponentName getName() {
        return HexComponentNames.HEX_AREA;
    }

    protected CaretComponent<?> createCaret() {
        var areaVM = getView().getViewModel();
        var vm = new CaretViewModel(areaVM::getRowByteCount, areaVM.charSizeProperty());
        var v = new CaretView(vm);
        var c = new CaretComponent<>(v);
        return c;
    }
}
