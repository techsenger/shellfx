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

package com.techsenger.tabshell.material.style;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.tabshell.material.theme.Theme;
import java.net.URL;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a CSS stylesheet in the application. Can be a single stylesheet or one that has an additional themed
 * variant. The name of the additional stylesheet is generated automatically based on the name of the base file
 * and the currently used theme.
 *
 * <p>For example, given a stylesheet {@code base.css}, if {@link Stylesheet#extensionThemes} is non-empty,
 * then for the {@code CUPERTINO_DARK} theme the additional themed stylesheet will be named
 * {@code base-cupertino-dark.css}.
 *
 * <p>Generating CSS file in AtlantaFX is done via code with conditions. So, it is impossible to override some
 * styles using only JavaFX CSS features. That's why in some cases it is necessary to add rules specific for
 * concrete theme. At the same time if it possible it must be avoided.
 *
 * @author Pavel Castornii
 */
public class Stylesheet {

    private final URL url;

    private final Set<Theme> extensionThemes;

    /**
     * Creates a stylesheet with the given {@link URL} and no theme-specific extensions.
     *
     * @param url the URL of the stylesheet
     */
    public Stylesheet(URL url) {
        this(url, Collections.emptySet());
    }

    /**
     * Creates a stylesheet, optionally accompanied by theme-specific extension stylesheets.
     *
     * @param url             the URL of the base stylesheet
     * @param extensionThemes the set of themes for which an extension stylesheet exists; an empty set means no
     *                        theme-specific extensions are available
     */
    public Stylesheet(URL url, Set<Theme> extensionThemes) {
        this.url = url;
        this.extensionThemes = extensionThemes;
    }

    /**
     * Returns the set of themes for which a theme-specific extension stylesheet exists.
     *
     * @return an unmodifiable set of themes with extension stylesheets; may be empty if no extensions exist
     */
    public @Unmodifiable Set<Theme> getExtensionThemes() {
        return extensionThemes;
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.url);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Stylesheet other = (Stylesheet) obj;
        return Objects.equals(this.url, other.url);
    }
}
