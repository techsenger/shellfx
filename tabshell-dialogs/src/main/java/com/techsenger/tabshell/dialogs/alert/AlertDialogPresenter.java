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

package com.techsenger.tabshell.dialogs.alert;

import com.techsenger.patternfx.mvp.Descriptor;
import com.techsenger.tabshell.core.CloseCheckResult;
import com.techsenger.tabshell.core.ClosePreparationResult;
import com.techsenger.tabshell.core.dialog.AbstractDialogPresenter;
import com.techsenger.tabshell.core.dialog.DialogComposer;
import com.techsenger.tabshell.core.popup.OverlayScope;
import com.techsenger.tabshell.dialogs.DialogComponents;
import static com.techsenger.tabshell.dialogs.alert.AlertDialogType.ERROR;
import static com.techsenger.tabshell.dialogs.alert.AlertDialogType.INFO;
import static com.techsenger.tabshell.dialogs.alert.AlertDialogType.WARNING;
import com.techsenger.tabshell.dialogs.style.DialogIcons;
import java.util.function.Consumer;

/**
 *
 * @author Pavel Castornii
 */
public class AlertDialogPresenter<V extends AlertDialogView, C extends DialogComposer>
        extends AbstractDialogPresenter<V, C> {

    private final AlertDialogType dialogType;

    private String message;

    public AlertDialogPresenter(V view, OverlayScope scope, AlertDialogType dialogType, String message) {
        super(view, scope);
        this.dialogType = dialogType;
        this.message = message;
    }

    @Override
    protected Descriptor createDescriptor() {
        return new Descriptor(DialogComponents.ALERT_DIALOG);
    }

    @Override
    public CloseCheckResult isReadyToClose() {
        return CloseCheckResult.READY;
    }

    @Override
    public void prepareToClose(Consumer<ClosePreparationResult> resultCallback) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public AlertDialogType getDialogType() {
        return dialogType;
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        var view = getView();
        switch (dialogType) {
            case INFO:
                view.setTitle("Info");
                view.setMessageIcon(DialogIcons.INFO);
            break;
            case ERROR:
                view.setTitle("Error");
                view.setMessageIcon(DialogIcons.ERROR);
            break;
            case WARNING:
                view.setTitle("Warning");
                view.setMessageIcon(DialogIcons.WARNING);
            break;
            case CONFIRMATION:
                view.setTitle("Confirm");
                view.setMessageIcon(DialogIcons.QUESTION);
                break;
            default:
                throw new AssertionError("Unknown type - " + dialogType);
        }
        if (dialogType != AlertDialogType.CONFIRMATION) {
            view.addRightButtons(AlertDialogButtons.OK);
            view.setButtonDefault(AlertDialogButtons.OK, true);
        } else {
            view.addRightButtons(AlertDialogButtons.NO, AlertDialogButtons.YES);
            view.setButtonDefault(AlertDialogButtons.YES, true);
        }
        setResultAction((result) -> requestClose());
        view.setPrefWidth(600);
        getView().setMessage(message);
        this.message = null;
        view.setButtonWidthEqual(true);
    }
}
