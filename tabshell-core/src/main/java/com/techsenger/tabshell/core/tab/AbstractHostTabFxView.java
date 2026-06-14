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

package com.techsenger.tabshell.core.tab;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.dialog.DialogFxView;
import com.techsenger.tabshell.core.popup.PopupFxView;
import com.techsenger.tabshell.core.popup.PopupPort;
import com.techsenger.tabshell.core.window.AbstractWindowManager;
import com.techsenger.tabshell.core.window.WindowArrangement;
import com.techsenger.tabshell.core.window.WindowFxView;
import com.techsenger.tabshell.core.window.WindowManager;
import com.techsenger.tabshell.core.window.WindowPort;
import com.techsenger.tabshell.core.window.WindowPosition;
import com.techsenger.tabshell.material.Anchors;
import java.util.List;
import javafx.scene.input.InputEvent;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractHostTabFxView<P extends AbstractHostTabPresenter<?>> extends AbstractTabFxView<P>
        implements HostTabFxView<P> {

    public class Composer extends AbstractTabFxView<P>.Composer implements HostTabFxView.Composer {

        private final AbstractHostTabFxView<P> view = AbstractHostTabFxView.this;

        private final AbstractWindowManager windowManager = createWindowManager();

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
        public @Unmodifiable List<? extends WindowFxView<?>> getWindows() {
            return windowManager.getWindows();
        }

        @Override
        public void arrangeWindows(WindowArrangement arrangement) {
            windowManager.arrangeWindows(arrangement);
        }

        @Override
        public void alignWindow(WindowFxView<?> window, WindowPosition pos) {
            windowManager.alignWindow(window, pos, 0, 0);
        }

        @Override
        public void alignWindow(WindowFxView<?> window, WindowPosition pos, double xOffset, double yOffset) {
            windowManager.alignWindow(window, pos, xOffset, yOffset);
        }

        @Override
        public void alignWindowToStage(WindowFxView<?> window, WindowPosition pos) {
            windowManager.alignWindowToStage(window, pos, 0, 0);
        }

        @Override
        public void alignWindowToStage(WindowFxView<?> window, WindowPosition pos, double xOffset, double yOffset) {
            windowManager.alignWindowToStage(window, pos, xOffset, yOffset);
        }

        @Override
        public void addDialog(DialogFxView<?> dialog) {
            windowManager.addDialog(dialog);
            getModifiableChildren().add(dialog);
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

        @Override
        public @Unmodifiable List<? extends WindowPort> getWindowPorts() {
            return windowManager.getWindows().stream().map(v -> v.getPresenter()).toList();
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
        public @Unmodifiable List<? extends PopupFxView<?>> getPopups() {
            return windowManager.getPopups();
        }

        @Override
        public List<? extends PopupPort> getPopupPorts() {
            return windowManager.getPopups().stream().map(v -> v.getPresenter()).toList();
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
                    view.getComposer().getParent(TabContainerFxView.class).setTabHeaderBlocked(view.getNode(), blocked);
                }
            };
        }
    }

    public AbstractHostTabFxView(ShellFxView<?> shell) {
        super(shell);
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new AbstractHostTabFxView.Composer();
    }
}
