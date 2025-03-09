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

package com.techsenger.tabshell.kit.core.file;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public final class UriUtils {

    /**
     * Returns a list of path segments that must be traversed to go from the base URI to the child URI.
     * This method works with any URI scheme, including file systems, SMB, Google Drive, etc.
     *
     * <p>Example usage:</p>
     * <pre>
     * URI baseUri = URI.create("https://example.com/storage/");
     * URI childUri = URI.create("https://example.com/storage/folder1/folder2/file.txt");
     *
     * List<String> pathSegments = getPathSegments(baseUri, childUri);
     * System.out.println(pathSegments); // Output: [folder1, folder2, file.txt]
     * </pre>
     *
     * @param baseUri  the base URI
     * @param childUri the child URI, which must be a descendant of the base URI
     * @return a list of path segments representing the relative path
     * @throws IllegalArgumentException if the child URI is not a descendant of the base URI
     */
    public static List<String> getPathSegments(URI baseUri, URI childUri) {
        String basePath = baseUri.getPath();
        String childPath = childUri.getPath();
        if (!childPath.startsWith(basePath)) {
            throw new IllegalArgumentException("Child URI is not a descendant of the base URI");
        }
        String relativePart = childPath.substring(basePath.length());
        List<String> segments = new ArrayList<>();
        for (String segment : relativePart.split("/")) {
            if (!segment.isEmpty()) {
                segments.add(segment);
            }
        }
        return segments;
    }

    /**
     * Returns the parent URI of the given URI relative to the file storage URI.
     * <p>
     * If the given URI matches the file storage URI exactly, {@code null} is returned.
     * If the given URI is directly under the file storage URI, the file storage URI itself is returned.
     * Otherwise, the URI one level up from the given URI is returned.
     * </p>
     *
     * @param fileStorageUri the base URI of the file storage
     * @param uri the URI for which to determine the parent
     * @return the parent URI, the file storage URI if there is only one level, or {@code null} if the URI matches
     * the file storage URI exactly
     */
    public static URI getParentUri(URI fileStorageUri, URI uri) {
        var segments = UriUtils.getPathSegments(fileStorageUri, uri);
        return getParentUri(fileStorageUri, uri, segments);
    }

    /**
     * Returns the parent URI of the given URI relative to the file storage URI.
     * <p>
     * If the given URI matches the file storage URI exactly, {@code null} is returned.
     * If the given URI is directly under the file storage URI, the file storage URI itself is returned.
     * Otherwise, the URI one level up from the given URI is returned.
     * </p>
     *
     * @param fileStorageUri the base URI of the file storage
     * @param uri the URI for which to determine the parent
     * @param segments the result of {@link #getPathSegments(java.net.URI, java.net.URI)} method.
     * @return the parent URI, the file storage URI if there is only one level, or {@code null} if the URI matches
     * the file storage URI exactly
     */
    public static URI getParentUri(URI fileStorageUri, URI uri, List<String> segments) {
        if (segments.isEmpty()) {
            return null;
        }
        if (segments.size() == 1) {
            return fileStorageUri;
        }
        StringBuilder pathBuilder = new StringBuilder();
        for (int i = 0; i < segments.size() - 1; i++) {
            if (i > 0) {
                pathBuilder.append('/');
            }
            pathBuilder.append(segments.get(i));
        }
        String joined = pathBuilder.toString();
        var encoded = URLEncoder.encode(joined, StandardCharsets.UTF_8).replace("+", "%20");
        return fileStorageUri.resolve(encoded);
    }

    /**
     * Resolves a given path against a base URI, ensuring proper handling of path segments.
     *
     * <p><b>Note:</b> This method does <i>not</i> normalize paths (e.g., resolve {@code ".."} or {@code "."}).
     *
     * @param baseUri the base URI (e.g., {@code "gs://bucket/folder/"}). Must not be {@code null}.
     * @param path the path to resolve against the base URI (e.g., {@code "file.txt"}). Leading/trailing slashes are
     * handled gracefully. Must not be {@code null}.
     * @return A new URI with the resolved path.
     * @throws IllegalArgumentException If {@code baseUri} or {@code path} is {@code null}, or if the resolved URI
     * is invalid.
     */
    public static URI resolvePath(URI baseUri, String path) {
        if (baseUri == null || path == null) {
            throw new IllegalArgumentException("URI and path must not be null");
        }
        String basePath = baseUri.getPath();
        if (basePath == null) {
            basePath = "/";
        }
        String joinedPath = joinPaths(basePath, path);
        try {
            return new URI(
                baseUri.getScheme(),
                baseUri.getAuthority(),
                joinedPath, //path passed to constructor can't be encoded
                baseUri.getQuery(),
                baseUri.getFragment()
            );
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URI after resolving path", e);
        }
    }

    /**
     * Decodes a URI into a human-readable string by replacing percent-encoded sequences
     * (e.g., %20, %3F) with their corresponding characters.
     * <p>
     * Note: This method decodes the entire URI string, including special characters
     * like {@code ?}, {@code #}, and {@code &}, which may alter the URI's structure.
     * Use with caution if the URI contains query parameters or fragments.
     * </p>
     *
     * @param uri The URI to decode (e.g., "https://example.com/path%20with%20spaces").
     * @return A human-readable string (e.g., "https://example.com/path with spaces").
     */
    public static String toHumanString(URI uri) {
        var result = URLDecoder.decode(uri.toString(), StandardCharsets.UTF_8);
        return result;
    }

    private static String joinPaths(String base, String relative) {
        if (base.endsWith("/")) {
            return relative.startsWith("/")
                ? base + relative.substring(1)
                : base + relative;
        } else {
            return relative.startsWith("/")
                ? base + relative
                : base + "/" + relative;
        }
    }

    private UriUtils() {
        //empty
    }
}
