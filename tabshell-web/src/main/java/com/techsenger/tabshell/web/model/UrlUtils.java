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

package com.techsenger.tabshell.web.model;

import java.net.URI;
import org.apache.commons.validator.routines.UrlValidator;

/**
 *
 * @author Pavel Castornii
 */
public final class UrlUtils {

    private static final UrlValidator URL_VALIDATOR = new UrlValidator(
        new String[]{"http", "https", "ftp", "file"},
        UrlValidator.ALLOW_LOCAL_URLS + UrlValidator.ALLOW_2_SLASHES
    );

    /**
     * Normalizes URL string.
     *
     * @param url
     * @return a normalized URL or empty string
     */
    public static String normalize(String input) {
         if (input == null) {
            return "";
        }
        var url = input.trim();
        if (url.isEmpty()) {
            return "";
        }

        String lowerUrl = url.toLowerCase();
        if (!lowerUrl.startsWith("http://") && !lowerUrl.startsWith("https://")
                && !lowerUrl.startsWith("file://") && !lowerUrl.startsWith("ftp://")) {
            url = "https://" + url;
        }
        try {
            return new URI(url).toURL().toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean isValid(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null) {
                return false;
            }
            if (!host.equals("localhost") && !host.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$")
                    && !host.contains(".")) {
                return false;
            }
            return URL_VALIDATOR.isValid(url);
        } catch (Exception ex) {
            return false;
        }
    }

    public static String getSearch(String input) {
        return "https://www.google.com/search?q=" + input.replace(" ", "+");
    }

    private UrlUtils() {
        // empty
    }
}
