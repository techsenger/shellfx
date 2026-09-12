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

package com.techsenger.shellfx.demo.page;

import com.techsenger.patternfx.mvvm.ChildComposer;
import com.techsenger.shellfx.core.page.AbstractPageViewModel;
import com.techsenger.shellfx.core.page.PageParams;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Pavel Castornii
 */
public class DemoPageViewModel<C extends ChildComposer> extends AbstractPageViewModel<C> {

    private final StringProperty text = new SimpleStringProperty();

    public DemoPageViewModel(PageParams params) {
        super(params);
    }

    public String getText() {
        return text.get();
    }

    public StringProperty textProperty() {
        return text;
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        text.set(getItem().getText() + " - " + Text.INSTANCE);
    }
}
