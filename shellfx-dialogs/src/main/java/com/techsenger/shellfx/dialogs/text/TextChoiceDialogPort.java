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
import com.techsenger.shellfx.core.dialog.DialogPort;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

/**
 * Provides full access to the component's client API.
 *
 * @author Pavel Castornii
 */
public interface TextChoiceDialogPort extends DialogPort {

    /**
     * The text chosen by the user; {@code null} while nothing is chosen.
     */
    @Nullable String getText();

    void setText(@Nullable String text);

    StringProperty textProperty();

    /**
     * The texts the user chooses from. The list belongs to the caller: changes to it are shown right away.
     */
    ObservableList<String> getTexts();
}
