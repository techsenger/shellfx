/*
 * Copyright 2024-2025 Pavel Castornii.
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

package com.techsenger.tabshell.jfx;

import com.techsenger.tabshell.core.area.AbstractAreaViewModel;
import com.techsenger.tabshell.core.area.AreaMediator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Pavel Castornii
 */
public class SearchPanelViewModel<T extends AreaMediator> extends AbstractAreaViewModel<T> {

    private final StringProperty searchText = new SimpleStringProperty();

    private final BooleanProperty caseSensitive = new SimpleBooleanProperty(false);

    public String getSearchText() {
        return searchText.get();
    }

    public void setSearchText(String value) {
        searchText.set(value);
    }

    public StringProperty searchTextProperty() {
        return searchText;
    }

    public boolean isCaseSensitive() {
        return caseSensitive.get();
    }

    public void setCaseSensitive(boolean value) {
        caseSensitive.set(value);
    }

    public BooleanProperty caseSensitiveProperty() {
        return caseSensitive;
    }

    public Matcher createMatcher() {
        if (getSearchText() != null && !getSearchText().isBlank()) {
             int flags = isCaseSensitive() ? Pattern.LITERAL
                                  : Pattern.CASE_INSENSITIVE | Pattern.LITERAL;
            var pattern = Pattern.compile(getSearchText().trim(), flags);
            return pattern.matcher("");
        }
        return null;
    }
}
