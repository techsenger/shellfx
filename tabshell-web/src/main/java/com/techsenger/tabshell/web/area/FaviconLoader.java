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

package com.techsenger.tabshell.web.area;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import javafx.scene.image.Image;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;


/**
 * Favicon loader using Swing's ImageIcon for ICO support. Swing's ImageIcon can decode ICO files natively.
 *
 * @author Pavel Castornii
 */
public final class FaviconLoader {

    public static Image loadFavicon(URL url, int size) {
        try {
            // 1. Try Google Service
            Image icon = loadPng("https://www.google.com/s2/favicons?sz=" + size + "&domain=" + url.getHost());
            if (icon != null && icon.getWidth() > 1) {
                return icon;
            }

            String baseUrl = buildBaseUrl(url);

            // 2. Try .ico
            String icoUrl = baseUrl + "/favicon.ico";
            icon = loadIco(icoUrl);
            if (icon != null && icon.getWidth() > 1) {
                return icon;
            }

            // 3. Try .png
            String pngUrl = baseUrl + "/favicon.png";
            icon = loadPng(pngUrl);
            return icon;
        } catch (Exception e) {
            return null;
        }
    }

    private static Image loadIco(String icoUrl) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(icoUrl).openConnection();
            conn.addRequestProperty("User-Agent", "Mozilla/5.0");

            // Read all bytes from the connection
            byte[] data = conn.getInputStream().readAllBytes();
            conn.disconnect();

            try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(data))) {
                // Get ICO reader via ImageIO
                Iterator<ImageReader> readers = ImageIO.getImageReaders(input);

                if (readers.hasNext()) {
                    ImageReader reader = readers.next();
                    reader.setInput(input);

                    // Read the first icon from the ICO file
                    BufferedImage buffered = reader.read(0);
                    reader.dispose();

                    // Convert BufferedImage to JavaFX Image
                    return bufferedToFxImage(buffered);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static Image loadPng(String pngUrl) {
        try {
            return new Image(pngUrl, 32, 32, true, true);
        } catch (Exception e) {
            return null;
        }
    }

    private static Image bufferedToFxImage(BufferedImage buffered) {
        try {
            // Convert BufferedImage to PNG bytes
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(buffered, "PNG", out);

            // Create JavaFX Image from PNG bytes
            return new Image(new ByteArrayInputStream(out.toByteArray()));
        } catch (Exception e) {
            return null;
        }
    }

    private static String buildBaseUrl(URL url) {
        String base = url.getProtocol() + "://" + url.getHost();
        int port = url.getPort();
        if (port > 0 && port != 80 && port != 443) {
            base += ":" + port;
        }
        return base;
    }

    private FaviconLoader() {
        // empty
    }
}
