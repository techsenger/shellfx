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

package com.techsenger.tabshell.material.style;

import com.techsenger.tabshell.material.theme.Theme;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

/**
 * Represents a CSS stylesheet in the application. Can be a single stylesheet or one that has an additional themed
 * variant. The name of the additional stylesheet is generated automatically based on the name of the base file
 * and the currently used theme.
 *
 * <p>For example, given a stylesheet {@code base.css}, if {@link Stylesheet#themed} is {@code true},
 * then for the {@code CUPERTINO_DARK} theme the additional themed stylesheet will be named
 * {@code base-cupertino-dark.css}.
 *
 * <p>Generating CSS file in AtlantaFX is done via code with conditions. So, it is impossible to override some
 * styles using only JavaFX CSS features. That's why in some cases it is necessary to add rules specific for
 * concrete theme. At the same time if it possible it must be avoided.
 *
 * <p>Important! This class is used ONLY for stylesheets that should be applied for the whole scene.
 *
 * @author Pavel Castornii
 */
public class Stylesheet {

    private final URL url;

    private final Set<Theme> extendedThemes;

    /**
     * Creates stylesheet with specific {@link URL}.
     *
     * @param url the url of the stylesheet.
     */
    public Stylesheet(URL url) {
        this(url, Collections.EMPTY_SET);
    }

    /**
     * Creates a stylesheet, optionally accompanied by a theme-specific stylesheet.
     *
     * @param url the URL of the base stylesheet
     * @param extendedThemes set of themes for which extended stylesheet versions exist; empty set means no theme
     *        extensions are available
     */
    public Stylesheet(URL url, Set<Theme> extendedThemes) {
        this.url = url;
        this.extendedThemes = extendedThemes;
    }

    /**
     * Returns the set of themes for which extended stylesheet versions exist.
     *
     * @return an unmodifiable set of themes with extended stylesheet versions; may be empty if no theme
     * extensions exist
     */
   public Set<Theme> getExtendedThemes() {
       return extendedThemes;
   }

    public URL getUrl() {
        return url;
    }
}
