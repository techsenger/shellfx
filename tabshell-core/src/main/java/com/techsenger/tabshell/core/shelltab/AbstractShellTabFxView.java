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

package com.techsenger.tabshell.core.shelltab;

import com.techsenger.tabshell.core.ShellFxView;
import com.techsenger.tabshell.core.ShellPort;
import com.techsenger.tabshell.core.dialog.DefaultDialogManager;
import com.techsenger.tabshell.core.dialog.DialogFxView;
import com.techsenger.tabshell.core.dialog.DialogManager;
import com.techsenger.tabshell.core.dialog.DialogPort;
import com.techsenger.tabshell.core.popup.OverlayScope;
import com.techsenger.tabshell.core.popup.PopupFxView;
import com.techsenger.tabshell.core.popup.PopupPort;
import com.techsenger.tabshell.core.tab.AbstractTabFxView;
import com.techsenger.tabshell.material.Anchors;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractShellTabFxView<P extends ShellTabPresenter<?, ?>>
        extends AbstractTabFxView<P> implements ShellTabFxView<P> {

    public class Composer extends AbstractTabFxView.Composer implements ShellTabFxView.Composer {

        private final AbstractShellTabFxView<?> view = AbstractShellTabFxView.this;

        @Override
        public ShellPort getShell() {
            return view.getShell().getPresenter().getPort();
        }

        @Override
        public OverlayScope getOverlayScope() {
            return OverlayScope.TAB;
        }

        @Override
        public List<? extends DialogPort> getDialogs() {
            return view.dialogManager.getDialogs().stream().map(v -> v.getPresenter().getPort()).toList();
        }

        @Override
        public void addDialog(DialogFxView<?> dialog) {
            var scope = dialog.getPresenter().getOverlayScope();
            if (scope == getOverlayScope()) {
                view.dialogManager.showDialog(dialog);
                view.getModifiableChildren().add(dialog);
            } else {
                view.getShell().getComposer().addDialog(dialog);
            }
        }

        @Override
        public void removeDialog(DialogFxView<?> dialog) {
            var scope = dialog.getPresenter().getOverlayScope();
            if (scope == getOverlayScope()) {
                view.dialogManager.hideDialog(dialog);
                view.getModifiableChildren().remove(dialog);
                dialog.getPresenter().deinitializeTree();
            } else {
                view.getShell().getComposer().removeDialog(dialog);
            }
        }

        @Override
        public void addPopup(PopupFxView<?> popup, Anchors anchors) {
            var scope = popup.getPresenter().getOverlayScope();
            if (scope == getOverlayScope()) {
                view.dialogManager.showPopup(popup, anchors);
                view.getModifiableChildren().add(popup);
            } else {
                view.getShell().getComposer().addPopup(popup, anchors);
            }
        }

        @Override
        public void removePopup(PopupFxView<?> popup) {
            var scope = popup.getPresenter().getOverlayScope();
            if (scope == getOverlayScope()) {
                view.dialogManager.hidePopup(popup);
                view.getModifiableChildren().remove(popup);
                popup.getPresenter().deinitializeTree();
            } else {
                view.getShell().getComposer().removePopup(popup);
            }
        }

        @Override
        public List<? extends PopupPort> getPopups() {
            return view.dialogManager.getPopups().stream().map(v -> v.getPresenter().getPort()).toList();
        }
    }

    private final DialogManager dialogManager = new DefaultDialogManager(getWrapperPane(), getContentBox());

    private final ShellFxView<?> shell;

    public AbstractShellTabFxView(ShellFxView<?> shell) {
        super();
        this.shell = shell;
    }

    public ShellFxView<?> getShell() {
        return shell;
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    protected DialogManager getDialogManager() {
        return dialogManager;
    }

    @Override
    protected Composer createComposer() {
        return new AbstractShellTabFxView.Composer();
    }
}
