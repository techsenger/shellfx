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

package com.techsenger.shellfx.shared.find;

import com.techsenger.annotations.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Builds the {@link Matcher} used across the various find components to test {@code findText} against candidate
 * strings.
 *
 * @author Pavel Castornii
 */
public final class TextMatcherFactory {

    private TextMatcherFactory() {
        // empty
    }

    /**
     * Returns a {@link Matcher} for {@code text}, quoted so it matches literally rather than as a regular
     * expression, case-insensitive unless {@code matchCase} is {@code true}.
     */
    public static Matcher create(String text, boolean matchCase) {
        int flags = matchCase ? 0 : Pattern.CASE_INSENSITIVE;
        return Pattern.compile(Pattern.quote(text), flags).matcher("");
    }

    /**
     * Returns a {@link Matcher} for {@code text}, or {@code null} if {@code regexp} is {@code true} and
     * {@code text} is not a syntactically valid regular expression. {@code text} is quoted so it matches
     * literally unless {@code regexp} is {@code true}; wrapped in a word boundary if {@code wholeWord} is
     * {@code true}; case-insensitive, Unicode-aware, unless {@code matchCase} is {@code true}.
     */
    public static @Nullable Matcher create(String text, boolean matchCase, boolean wholeWord, boolean regexp) {
        var literal = regexp ? text : Pattern.quote(text);
        var regex = wholeWord ? "\\b(?:" + literal + ")\\b" : literal;
        int flags = matchCase ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        try {
            return Pattern.compile(regex, flags).matcher("");
        } catch (PatternSyntaxException e) {
            return null;
        }
    }
}
