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
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gets the texts of the bundles in the way every control wants them: a gap in the bundles is seen in the UI and is
 * logged, and never fails the control.
 *
 * @author Pavel Castornii
 */
public final class TextUtils {

    private static final Logger logger = LoggerFactory.getLogger(TextUtils.class);

    /**
     * Returns the text of a key.
     *
     * @param bundles gives the bundle by its name or {@code null} if there is none, for example
     *     {@code registry::getBundle}
     * @param textKey the address of the text
     * @return the text or, if there is no such bundle or text, the key in the form {@code !key!} so that the gap is
     *     seen in the UI; the gap is logged
     */
    public static String getText(Function<BundleName, @Nullable ResourceBundle> bundles, TextKey textKey) {
        var bundle = bundles.apply(textKey.bundleName());
        if (bundle == null) {
            logger.warn("No bundle {} for the text {}", textKey.bundleName(), textKey.key());
        } else {
            try {
                return bundle.getString(textKey.key());
            } catch (MissingResourceException ex) {
                logger.warn("No text {} in the bundle {}", textKey.key(), textKey.bundleName());
            }
        }
        return "!" + textKey.key() + "!";
    }

    private TextUtils() {
        // empty
    }
}
