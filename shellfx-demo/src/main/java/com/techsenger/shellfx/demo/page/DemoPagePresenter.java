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

import com.techsenger.shellfx.core.page.AbstractPagePresenter;
import com.techsenger.shellfx.core.page.PageParams;
import java.util.Objects;

/**
 *
 * @author Pavel Castornii
 */
public class DemoPagePresenter extends AbstractPagePresenter<DemoPageView> {

    private String text;

    public DemoPagePresenter(DemoPageView view, PageParams params) {
        super(view, params);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (Objects.equals(this.text, text)) {
            return;
        }
        this.text = text;
        getView().updateText(text);
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        setText(getItem().getText() + " - " + Text.INSTANCE);
    }
}
