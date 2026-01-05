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

package com.techsenger.tabshell.material.textarea;

import java.util.ArrayList;
import java.util.List;

/**
 * RichTextFX allows to use custom styles classes for StyleSpan, so, it will be {@code StyleSpan<TextAreaStyle>}. Using
 * this feature we can add our data to style. For example, when we check if styled span was modified we can check its
 * previous length.
 *
 * <p>Important! This class uses default hashCode() and equals() because RichTextFX merges equal spans and this is
 * the behavior we want to avoid.
 *
 * @author Pavel Castornii
 */
public class TextAreaStyle {

    /**
     * This style will be used for creating empty StyleSpan. Attention! There seems to be a bug in richtext fx as
     * this constant is not always used for creating empty style spans. So, don't compare them to this constant
     * by reference. Do isEmpty().
     */
    public static final List<TextAreaStyle> EMPTY = new ArrayList<>();

    private final TextAreaStyleName name;

    /**
     * The length of the text when this style was created. If user modifies span, then we can understand by length
     * change.
     */
    private final int length;

    private final String className;

    public TextAreaStyle(String className) {
        this.name = null;
        this.length = -1;
        this.className = className;
    }

    public TextAreaStyle(TextAreaStyleName name, int length, String className) {
        this.name = name;
        this.className = className;
        this.length = length;
    }

    public TextAreaStyleName getName() {
        return name;
    }

    public int getLength() {
        return length;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        return "TextAreaStyle{" + "name=" + name + ", initialLength=" + length + ", className=" + className + '}';
    }
}
