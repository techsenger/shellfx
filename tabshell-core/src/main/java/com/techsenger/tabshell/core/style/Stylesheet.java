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

package com.techsenger.tabshell.core.style;

import com.techsenger.tabshell.core.theme.TabShellTheme;
import java.net.URL;
import java.util.Objects;

/**
 * Generating CSS file in AtlantaFX is done via code with conditions. So, it is impossible to override some
 * styles using only JavaFX CSS features. That's why in some cases it is necessary to add rules specific for
 * concrete theme. At the same time if it possible it must be avoided.
 *
 * <p>Important! This class is used ONLY for stylesheets that should be applied for the whole scene.
 *
 * @author Pavel Castornii
 */
public class Stylesheet {

    private final TabShellTheme theme;

    private final URL url;

    /**
     * Creates stylesheet that must be used for all themes.
     *
     * @param url the url of the stylesheet.
     */
    public Stylesheet(URL url) {
        this(null, url);
    }

    /**
     * Creates stylesheet for concrete theme.
     *
     * @param theme the theme this stylesheet is for or null, if the stylesheet must be used for all themes.
     * @param url the url of the stylesheet.
     */
    public Stylesheet(TabShellTheme theme, URL url) {
        this.theme = theme;
        this.url = url;
    }

    public TabShellTheme getTheme() {
        return theme;
    }

    public URL getUrl() {
        return url;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.theme);
        hash = 97 * hash + Objects.hashCode(this.url);
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
        if (this.theme != other.theme) {
            return false;
        }
        return Objects.equals(this.url, other.url);
    }
}
