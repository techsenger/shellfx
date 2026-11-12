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

package com.techsenger.shellfx.demo.ide;

import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.core.tab.AbstractHostTabView;
import com.techsenger.shellfx.demo.area.DemoAreaParams;
import com.techsenger.shellfx.demo.area.DemoAreaView;
import com.techsenger.shellfx.demo.area.DemoAreaViewModel;
import com.techsenger.shellfx.layout.dockhost.DockHostPort;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * This tab is a window container: dialogs and popups of the tab scope are opened in it.
 *
 * @author Pavel Castornii
 */
public class IdeMainTabView<VM extends IdeMainTabViewModel<?>> extends AbstractHostTabView<VM> {

    public class Composer extends AbstractHostTabView<VM>.Composer implements IdeMainTabComposer {

        private final IdeMainTabView<VM> view = IdeMainTabView.this;

        @Override
        public void compose() {
            super.compose();
            var area = createArea();
            getModifiableChildren().add(area);
            VBox.setVgrow(area.getNode(), Priority.ALWAYS);
            getContentBox().getChildren().add(area.getNode());
        }

        protected DemoAreaView<?> createArea() {
            var settings = getShell().getContext().getSettings();
            var params = new DemoAreaParams(null, settings);
            var viewModel = new DemoAreaViewModel<>(params);
            // in the IDE application the workspace is the dock host the area is placed in
            var dockHostPort = (DockHostPort) getShell().getViewModel().getComposerAccess().getWorkspacePort();
            var areaView = new DemoAreaView<>(viewModel, getShell(), view, dockHostPort);
            areaView.initialize();
            return areaView;
        }
    }

    public IdeMainTabView(VM viewModel, ShellView<?> shell) {
        super(viewModel, shell);
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    public void requestFocus() {

    }

    @Override
    protected Composer createComposer() {
        return new IdeMainTabView.Composer();
    }
}
