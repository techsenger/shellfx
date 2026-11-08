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

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.core.popup.AbstractPopupManager;
import com.techsenger.shellfx.core.popup.ContainerPopupPort;
import com.techsenger.shellfx.core.popup.PopupContainerView;
import com.techsenger.shellfx.core.popup.PopupManager;
import com.techsenger.shellfx.core.popup.PopupView;
import com.techsenger.shellfx.core.tab.AbstractTabView;
import com.techsenger.shellfx.demo.area.DemoAreaParams;
import com.techsenger.shellfx.demo.area.DemoAreaView;
import com.techsenger.shellfx.demo.area.DemoAreaViewModel;
import com.techsenger.shellfx.layout.dockhost.DockHostPort;
import com.techsenger.shellfx.material.Anchors;
import java.util.List;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * This tab is a popup container.
 *
 * @author Pavel Castornii
 */
public class IdeMainTabView<VM extends IdeMainTabViewModel<?>> extends AbstractTabView<VM> implements
        PopupContainerView<VM> {

    public class Composer extends AbstractTabView<VM>.Composer implements IdeMainTabComposer,
            PopupContainerView.Composer {

        private final IdeMainTabView<VM> view = IdeMainTabView.this;

        private final PopupManager popupManager = new AbstractPopupManager(() -> view.getWrapperPane()) {

            @Override
            protected void onContainerBlocked(boolean blocked) {
                if (blocked) {
                    view.getNode().getContent().addEventFilter(InputEvent.ANY, getEventBlocker());
                } else {
                    view.getNode().getContent().removeEventFilter(InputEvent.ANY, getEventBlocker());
                }
                getParent().setTabHeaderBlocked(view.getNode(), blocked);
            }
        };

        @Override
        public void compose() {
            super.compose();
            var area = createArea();
            getModifiableChildren().add(area);
            VBox.setVgrow(area.getNode(), Priority.ALWAYS);
            getContentBox().getChildren().add(area.getNode());
        }

        @Override
        public void addPopup(PopupView<?> popup, Anchors anchors) {
            getModifiableChildren().add(popup);
            this.popupManager.addPopup(popup, anchors);
        }

        @Override
        public void removePopup(PopupView<?> popup) {
            this.popupManager.removePopup(popup);
            getModifiableChildren().remove(popup);
        }

        @Override
        public void closePopup(PopupView<?> popup) {
            removePopup(popup);
            popup.deinitializeTree();
        }

        @Override
        public @Unmodifiable List<? extends PopupView<?>> getPopups() {
            return popupManager.getPopups();
        }

        @Override
        public @Unmodifiable List<? extends ContainerPopupPort> getPopupPorts() {
            return popupManager.getPopups().stream().map(d -> d.getViewModel()).toList();
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
