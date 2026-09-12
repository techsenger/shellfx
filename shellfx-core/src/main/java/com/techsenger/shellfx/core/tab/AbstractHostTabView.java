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

package com.techsenger.shellfx.core.tab;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.shellfx.core.ShellView;
import com.techsenger.shellfx.core.dialog.DialogView;
import com.techsenger.shellfx.core.popup.ContainerPopupPort;
import com.techsenger.shellfx.core.popup.PopupView;
import com.techsenger.shellfx.core.window.AbstractWindowManager;
import com.techsenger.shellfx.core.window.ContainerWindowPort;
import com.techsenger.shellfx.core.window.WindowArrangement;
import com.techsenger.shellfx.core.window.WindowManager;
import com.techsenger.shellfx.core.window.WindowPosition;
import com.techsenger.shellfx.core.window.WindowView;
import com.techsenger.shellfx.material.Anchors;
import java.util.List;
import javafx.scene.input.InputEvent;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractHostTabView<VM extends AbstractHostTabViewModel<?>> extends AbstractTabView<VM>
        implements HostTabView<VM> {

    public class Composer extends AbstractTabView<VM>.Composer implements HostTabView.Composer {

        private final AbstractHostTabView<VM> view = AbstractHostTabView.this;

        private final AbstractWindowManager windowManager = createWindowManager();

        @Override
        public void addWindow(WindowView<?> window) {
            windowManager.addWindow(window);
            getModifiableChildren().add(window);
        }

        @Override
        public void removeWindow(WindowView<?> window) {
            windowManager.removeWindow(window);
            getModifiableChildren().remove(window);
        }

        @Override
        public void closeWindow(WindowView<?> window) {
            removeWindow(window);
            window.getViewModel().requestDeinitializeTree();
        }

        @Override
        public @Unmodifiable List<? extends WindowView<?>> getWindows() {
            return windowManager.getWindows();
        }

        @Override
        public void arrangeWindows(WindowArrangement arrangement) {
            windowManager.arrangeWindows(arrangement);
        }

        @Override
        public void alignWindow(WindowView<?> window, WindowPosition pos) {
            windowManager.alignWindow(window, pos, 0, 0);
        }

        @Override
        public void alignWindow(WindowView<?> window, WindowPosition pos, double xOffset, double yOffset) {
            windowManager.alignWindow(window, pos, xOffset, yOffset);
        }

        @Override
        public void alignWindowToStage(WindowView<?> window, WindowPosition pos) {
            windowManager.alignWindowToStage(window, pos, 0, 0);
        }

        @Override
        public void alignWindowToStage(WindowView<?> window, WindowPosition pos, double xOffset, double yOffset) {
            windowManager.alignWindowToStage(window, pos, xOffset, yOffset);
        }

        @Override
        public void addDialog(DialogView<?> dialog) {
            windowManager.addDialog(dialog);
            getModifiableChildren().add(dialog);
        }

        @Override
        public void maximizeWindow(WindowView<?> window) {
            windowManager.maximizeWindow(window);
        }

        @Override
        public void minimizeWindow(WindowView<?> window) {
            windowManager.minimizeWindow(window);
        }

        @Override
        public void restoreWindow(WindowView<?> window) {
            windowManager.restoreWindow(window);
        }

        @Override
        public @Unmodifiable List<? extends ContainerWindowPort> getWindowPorts() {
            return windowManager.getWindows().stream().map(v -> v.getViewModel()).toList();
        }

        @Override
        public void addPopup(PopupView<?> popup, Anchors anchors) {
            windowManager.addPopup(popup, anchors);
            getModifiableChildren().add(popup);
        }

        @Override
        public void removePopup(PopupView<?> popup) {
            windowManager.removePopup(popup);
            getModifiableChildren().remove(popup);
        }

        @Override
        public void closePopup(PopupView<?> popup) {
            removePopup(popup);
            popup.getViewModel().requestDeinitializeTree();
        }

        @Override
        public @Unmodifiable List<? extends PopupView<?>> getPopups() {
            return windowManager.getPopups();
        }

        @Override
        public List<? extends ContainerPopupPort> getPopupPorts() {
            return windowManager.getPopups().stream().map(v -> v.getViewModel()).toList();
        }

        protected WindowManager getWindowManager() {
            return windowManager;
        }

        protected AbstractWindowManager createWindowManager() {
            return new AbstractWindowManager(() -> view.getWrapperPane(),
                    () -> getShell().getComposer().focusedProperty()) {
                @Override
                protected void onContainerBlocked(boolean blocked) {
                    if (blocked) {
                        view.getNode().getContent().addEventFilter(InputEvent.ANY, getEventBlocker());
                    } else {
                        view.getNode().getContent().removeEventFilter(InputEvent.ANY, getEventBlocker());
                    }
                    view.getComposer().getParent().setTabHeaderBlocked(view.getNode(), blocked);
                }
            };
        }
    }

    public AbstractHostTabView(VM viewModel, ShellView<?> shell) {
        super(viewModel, shell);
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new AbstractHostTabView.Composer();
    }
}
