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

package com.techsenger.shellfx.base.bundle;

import com.techsenger.annotations.Nullable;
import java.util.ResourceBundle;
import java.util.function.Function;

/**
 * Something that has a text for the user in the languages of the application, such as the name of an action. The
 * object knows only where the text is, not the text itself: the text is found by the key in the bundles.
 *
 * @author Pavel Castornii
 */
public interface Multilingual {

    /**
     * Returns the address of the text of this object.
     *
     * @return the key, never {@code null}
     */
    TextKey getTextKey();

    /**
     * Returns the text of this object.
     *
     * @param bundles gives the bundle by its name or {@code null} if there is none, for example
     *     {@code registry::getBundle}
     * @return the text or, if there is no such bundle or text, the key in the form {@code !key!} so that the gap is
     *     seen in the UI; the gap is logged
     */
    default String getText(Function<BundleName, @Nullable ResourceBundle> bundles) {
        return TextUtils.getText(bundles, getTextKey());
    }
}
