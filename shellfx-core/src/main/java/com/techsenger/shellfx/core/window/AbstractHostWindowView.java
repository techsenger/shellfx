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

package com.techsenger.shellfx.core.window;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.shellfx.core.dialog.DialogView;
import com.techsenger.shellfx.core.popup.ContainerPopupPort;
import com.techsenger.shellfx.core.popup.PopupView;
import com.techsenger.shellfx.material.Anchors;
import com.techsenger.shellfx.material.style.Stylesheet;
import java.util.List;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractHostWindowView<VM extends AbstractHostWindowViewModel<?>> extends
        AbstractWindowView<VM> implements HostWindowView<VM> {

    public class Composer extends AbstractWindowView<VM>.Composer implements HostWindowView.Composer {

        private final AbstractHostWindowView<VM> view = AbstractHostWindowView.this;

        private final AbstractWindowManager windowManager = createWindowManager();

        @Override
        public void compose() {
            super.compose();
            focusedProperty().addListener((ov, oldV, newV) -> this.windowManager.onFocusedComponentChanged(newV));
        }

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
            window.deinitializeTree();
        }

        @Override
        public @Unmodifiable List<? extends ContainerWindowPort> getWindowPorts() {
            return windowManager.getWindows().stream().map(d -> d.getViewModel()).toList();
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
            popup.deinitializeTree();
        }

        @Override
        public @Unmodifiable List<? extends ContainerPopupPort> getPopupPorts() {
            return windowManager.getPopups().stream().map(d -> d.getViewModel()).toList();
        }

        @Override
        public @Unmodifiable List<? extends PopupView<?>> getPopups() {
            return windowManager.getPopups();
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

        protected WindowManager getWindowManager() {
            return this.windowManager;
        }

        protected AbstractWindowManager createWindowManager() {
            return new AbstractWindowManager(() -> view.getContentPane(), () -> focusedProperty()) {

                /**
                 * This pane is used to prevent title bar dragging and, until JDK-8384230 is resolved,
                 * resizing on a title bar double-click.
                 */
                private final StackPane blockingPane = new StackPane();

                {
                    this.blockingPane.setMouseTransparent(false);
                }

                @Override
                protected void onContainerBlocked(boolean blocked) {
                    var scene = view.getNode().getScene();
                    if (blocked) {
                        getTitlePane().getChildren().add(blockingPane);
                        scene.addEventFilter(InputEvent.ANY, getEventBlocker());
                    } else {
                        getTitlePane().getChildren().remove(blockingPane);
                        scene.removeEventFilter(InputEvent.ANY, getEventBlocker());
                    }
                }
            };
        }
    }

    public AbstractHostWindowView(VM viewModel) {
        super(viewModel);
    }

    public AbstractHostWindowView(VM viewModel, Stage stage, List<Stylesheet> stylesheets) {
        super(viewModel, stage, stylesheets);
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new Composer();
    }

    @Override
    void updateActive(boolean active) {
        super.updateActive(active);
        if (!active && getComposer().windowManager != null) {
            getComposer().windowManager.deactivateAllWindows(null);
        }
    }
}
