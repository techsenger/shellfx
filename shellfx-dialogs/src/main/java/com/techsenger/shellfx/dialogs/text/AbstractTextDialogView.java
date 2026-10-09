/*
 * Copyright 2026 Pavel Castornii.
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

package com.techsenger.shellfx.dialogs.text;

import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.core.dialog.AbstractDialogView;
import com.techsenger.shellfx.material.button.ResultButton;
import com.techsenger.toolkit.fx.utils.NodeUtils;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;

/**
 * The part of a text dialog common to all of them: the label, the buttons and the binding of the text, the editable
 * flag and the selection to the input control that a subclass provides.
 *
 * @author Pavel Castornii
 */
public abstract class AbstractTextDialogView<VM extends AbstractTextDialogViewModel<?>>
        extends AbstractDialogView<VM> {

    private final Label label = new Label("Text");

    private final ResultButton cancelButton = new ResultButton(TextDialogButtons.CANCEL, "Cancel");

    private final ResultButton okButton = new ResultButton(TextDialogButtons.OK, "OK");

    private @Nullable IndexRange requestedSelection;

    public AbstractTextDialogView(VM viewModel) {
        super(viewModel);
    }

    public AbstractTextDialogView(VM viewModel, String labelText) {
        super(viewModel);
        label.setText(labelText);
    }

    @Override
    public void requestFocus() {
        NodeUtils.requestFocus(getInput(), () -> updateSelection(requestedSelection));
    }

    /**
     * Adds the label and the input control to the content of the dialog.
     */
    protected abstract void buildContent();

    @Override
    protected void build() {
        super.build();
        label.setMinWidth(Label.USE_PREF_SIZE);
        buildContent();
        registerButtons(cancelButton, okButton);
        getButtonWidthGroup().add(cancelButton, okButton);
        getFocusTrap().activate();
    }

    @Override
    protected void bind() {
        super.bind();
        var viewModel = getViewModel();
        var input = getInput();
        input.textProperty().bindBidirectional(viewModel.textProperty());
        input.editableProperty().bind(viewModel.editableProperty());
        viewModel.selectionWrapper().bind(input.selectionProperty());
    }

    @Override
    protected void addListeners() {
        super.addListeners();
        getViewModel().selectionSource().addListener((selection) -> {
            requestedSelection = selection;
            updateSelection(selection);
        });
    }

    protected Label getLabel() {
        return label;
    }

    /**
     * Returns the control the user types the text into. It is called only after the subclass is constructed.
     */
    abstract TextInputControl getInput();

    private void updateSelection(@Nullable IndexRange selection) {
        if (selection == null) {
            getInput().deselect();
        } else {
            getInput().selectRange(selection.getStart(), selection.getEnd());
        }
    }
}
