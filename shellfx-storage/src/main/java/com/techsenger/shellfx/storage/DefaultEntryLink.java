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
 * Default implementation of {@link EntryLink}.
 *
 * <p>Instances are created by {@link FileStorage} implementations, which populate the fields right after
 * construction. This class is designed to be subclassed when additional application-level information about the
 * target needs to be attached to it.
 *
 * @author Pavel Castornii
 */
public class DefaultEntryLink implements EntryLink {

    private URI targetUri;

    private @Nullable URI resolvedUri;

    // written by the second loading phase on a background thread
    private volatile @Nullable StorageFile target;

    /**
     * Constructs an empty {@code DefaultEntryLink}. Fields should be populated by the {@link FileStorage}
     * immediately after construction.
     */
    public DefaultEntryLink() {
    }

    @Override
    public URI getTargetUri() {
        return targetUri;
    }

    @Override
    public @Nullable URI getResolvedUri() {
        return resolvedUri;
    }

    @Override
    public @Nullable StorageFile getTarget() {
        return target;
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetUri, target);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        var other = (DefaultEntryLink) obj;
        return Objects.equals(targetUri, other.targetUri) && Objects.equals(target, other.target);
    }

    @Override
    public String toString() {
        return "DefaultEntryLink[targetUri=" + targetUri + ", target=" + target + ']';
    }

    /**
     * Sets the file the link finally points to. It is public so that the second loading phase can set it, and must be
     * called on a thread that owns the link, as it is not observable.
     *
     * @param target the file at the end of the chain of links, or {@code null} if the link is broken
     */
    public void setTarget(@Nullable StorageFile target) {
        this.target = target;
    }

    /**
     * Sets the URI of the path the link states.
     *
     * @param targetUri the URI, must not be {@code null}
     */
    protected void setTargetUri(URI targetUri) {
        this.targetUri = targetUri;
    }

    /**
     * Sets the URI of the entry at the end of the chain of links.
     *
     * @param resolvedUri the URI, or {@code null} if nothing exists at the end of the chain or it has no path
     */
    protected void setResolvedUri(@Nullable URI resolvedUri) {
        this.resolvedUri = resolvedUri;
    }
}
