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

package com.techsenger.shellfx.layout.dockhost;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author Pavel Castornii
 */
class TreeIterator implements Iterator<ModelNode> {

    private final Deque<Iterator<ModelNode>> stack = new ArrayDeque<>();

    private ModelNode next;

    TreeIterator(SplitModelNode root) {
        next = root;
        stack.push(root.getChildren().iterator());
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public ModelNode next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        ModelNode result = next;
        next = findNext();

        return result;
    }

    private ModelNode findNext() {
        while (!stack.isEmpty()) {
            Iterator<ModelNode> iterator = stack.peek();

            if (iterator.hasNext()) {
                ModelNode child = iterator.next();
                if (child instanceof SplitModelNode smn) {
                    stack.push(smn.getChildren().iterator());
                }
                return child;
            }

            stack.pop();
        }

        return null;
    }
}
