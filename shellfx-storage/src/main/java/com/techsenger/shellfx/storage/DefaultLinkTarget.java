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

package com.techsenger.shellfx.storage;

import com.techsenger.annotations.Nullable;
import java.net.URI;
import java.util.Objects;

/**
 * Default implementation of {@link LinkTarget}.
 *
 * <p>Instances are created by {@link FileStorage} implementations, which populate the fields right after
 * construction. This class is designed to be subclassed when additional application-level information about the
 * target needs to be attached to it.
 *
 * @author Pavel Castornii
 */
public class DefaultLinkTarget implements LinkTarget {

    private URI uri;

    private @Nullable FileEntryType entryType;

    /**
     * Constructs an empty {@code DefaultLinkTarget}. Fields should be populated by the {@link FileStorage}
     * immediately after construction.
     */
    public DefaultLinkTarget() {
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public @Nullable FileEntryType getEntryType() {
        return entryType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, entryType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        var other = (DefaultLinkTarget) obj;
        return Objects.equals(uri, other.uri) && entryType == other.entryType;
    }

    @Override
    public String toString() {
        return "DefaultLinkTarget[uri=" + uri + ", entryType=" + entryType + ']';
    }

    /**
     * Sets the URI of the path the link points to.
     *
     * @param uri the URI, must not be {@code null}
     */
    protected void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * Sets the type of the target.
     *
     * @param entryType the type of the target, or {@code null} if the link is broken
     */
    protected void setEntryType(@Nullable FileEntryType entryType) {
        this.entryType = entryType;
    }
}
