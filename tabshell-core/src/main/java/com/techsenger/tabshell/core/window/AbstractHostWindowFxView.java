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

package com.techsenger.tabshell.core.window;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.tabshell.core.popup.PopupFxView;
import com.techsenger.tabshell.core.popup.PopupPort;
import com.techsenger.tabshell.material.Anchors;
import com.techsenger.tabshell.material.style.Stylesheet;
import java.util.List;
import javafx.scene.input.InputEvent;
import javafx.stage.Stage;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractHostWindowFxView<P extends AbstractHostWindowPresenter<?>> extends
        AbstractWindowFxView<P> implements HostWindowFxView<P> {

    public class Composer extends AbstractWindowFxView<P>.Composer implements HostWindowFxView.Composer {

        private final AbstractHostWindowFxView<P> view = AbstractHostWindowFxView.this;

        private final AbstractWindowManager windowManager = createWindowManager();

        @Override
        public void compose() {
            super.compose();
            focusedProperty().addListener((ov, oldV, newV) -> this.windowManager.onFocusedComponentChanged(newV));
        }

        @Override
        public void addWindow(WindowFxView<?> window) {
            windowManager.addWindow(window);
            getModifiableChildren().add(window);
        }

        @Override
        public void removeWindow(WindowFxView<?> window) {
            windowManager.removeWindow(window);
            getModifiableChildren().remove(window);
        }

        @Override
        public void closeWindow(WindowFxView<?> window) {
            removeWindow(window);
            window.getPresenter().deinitializeTree();
        }

        @Override
        public @Unmodifiable List<? extends WindowPort> getWindowPorts() {
            return windowManager.getWindows().stream().map(d -> d.getPresenter()).toList();
        }

        @Override
        public @Unmodifiable List<? extends WindowFxView<?>> getWindows() {
            return windowManager.getWindows();
        }

        @Override
        public void arrangeWindows(WindowArrangement arrangement) {
            windowManager.arrangeWindows(arrangement);
        }

        @Override
        public void alignWindow(WindowFxView<?> window, WindowPosition pos, double xOffset, double yOffset) {
            windowManager.alignWindow(window, pos, xOffset, yOffset);
        }

        @Override
        public void addPopup(PopupFxView<?> popup, Anchors anchors) {
            windowManager.addPopup(popup, anchors);
            getModifiableChildren().add(popup);
        }

        @Override
        public void removePopup(PopupFxView<?> popup) {
            windowManager.removePopup(popup);
            getModifiableChildren().remove(popup);
        }

        @Override
        public void closePopup(PopupFxView<?> popup) {
            removePopup(popup);
            popup.getPresenter().deinitializeTree();
        }

        @Override
        public @Unmodifiable List<? extends PopupPort> getPopupPorts() {
            return windowManager.getPopups().stream().map(d -> d.getPresenter()).toList();
        }

        @Override
        public @Unmodifiable List<? extends PopupFxView<?>> getPopups() {
            return windowManager.getPopups();
        }

        @Override
        public void maximizeWindow(WindowFxView<?> window) {
            windowManager.maximizeWindow(window);
        }

        @Override
        public void minimizeWindow(WindowFxView<?> window) {
            windowManager.minimizeWindow(window);
        }

        @Override
        public void restoreWindow(WindowFxView<?> window) {
            windowManager.restoreWindow(window);
        }

        protected WindowManager getWindowManager() {
            return this.windowManager;
        }

        protected AbstractWindowManager createWindowManager() {
            return new AbstractWindowManager(() -> view.getContentPane()) {

                @Override
                protected void onContainerBlocked(boolean blocked) {
                    var scene = view.getNode().getScene();
                    if (blocked) {
                        scene.addEventFilter(InputEvent.ANY, getEventBlocker());
                    } else {
                        scene.removeEventFilter(InputEvent.ANY, getEventBlocker());
                    }
                }
            };
        }
    }

    public AbstractHostWindowFxView() {
    }

    public AbstractHostWindowFxView(Stage stage, List<Stylesheet> stylesheets) {
        super(stage, stylesheets);
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
    void setActive(boolean active) {
        super.setActive(active);
        if (!active && getComposer().windowManager != null) {
            getComposer().windowManager.deactivateAllWindows(null);
        }
    }
}
