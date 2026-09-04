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

package com.techsenger.shellfx.material.pane;

import atlantafx.base.theme.Styles;
import com.techsenger.annotations.Nullable;
import com.techsenger.shellfx.material.icon.FontIconView;
import com.techsenger.shellfx.material.style.MaterialIcons;
import com.techsenger.shellfx.material.style.StyleClasses;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;

/**
 * One item hosted by a {@link CollapsibleSplitPane}: its content, an arbitrary {@code value} a header factory
 * can read to render whatever it needs (a title, an icon, ...) - the same generic, display-agnostic role
 * {@code TreeItem<T>.getValue()} plays for {@code TreeCell} - and a ready-made maximize/restore toggle button
 * the factory can place anywhere in its own header layout without having to rebuild the icon-swap/tooltip logic
 * itself. Clicking the button maximizes this item (collapsing every sibling), or restores an even split if this
 * item is already the sole one not collapsed - the actual cross-item work happens in the owning
 * {@link CollapsibleSplitPane}, which is the only party with visibility into siblings;
 * {@link #collapsedProperty()} itself stays a plain, directly settable flag with no dependency on that owner,
 * the same shape as {@code TreeItem.expandedProperty()}/{@code setExpanded}.
 *
 * @author Pavel Castornii
 */
public class SplitPaneItem<T> {

    private final Node content;

    private final ObjectProperty<T> value = new SimpleObjectProperty<>();

    private final BooleanProperty collapsed = new SimpleBooleanProperty(false);

    private final Button collapseButton;

    private @Nullable CollapsibleSplitPane<T> owner;

    public SplitPaneItem(Node content, T value) {
        this.content = content;
        this.value.set(value);
        this.collapseButton = createButton();
        collapseButton.setOnAction(e -> {
            if (owner != null) {
                owner.toggleMaximized(this);
            }
        });
    }

    public Node getContent() {
        return content;
    }

    public ObjectProperty<T> valueProperty() {
        return value;
    }

    public T getValue() {
        return value.get();
    }

    public void setValue(T value) {
        this.value.set(value);
    }

    /**
     * Whether this item is pinned down to just its header, with the {@link CollapsibleSplitPane} that hosts it
     * redistributing the freed space among its remaining, uncollapsed siblings - the same direct-property
     * shape as {@code TreeItem.expandedProperty()}/{@code setExpanded}, rather than a method on the owning
     * container.
     */
    public BooleanProperty collapsedProperty() {
        return collapsed;
    }

    public boolean isCollapsed() {
        return collapsed.get();
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed.set(collapsed);
    }

    public Button getCollapseButton() {
        return collapseButton;
    }

    /**
     * Creates {@link #collapseButton}'s underlying {@code Button}; override to change its appearance while
     * keeping the base class's action handler, wired in the constructor after this returns.
     */
    protected Button createButton() {
        var button = new Button(null, new FontIconView(MaterialIcons.CHEVRON_DOWN));
        button.getStyleClass().addAll(StyleClasses.SQUARE, Styles.FLAT, StyleClasses.SIZE_S);
        return button;
    }

    /**
     * Set by the owning {@link CollapsibleSplitPane} when this item enters its
     * {@link CollapsibleSplitPane#getItems()}; only {@link #collapseButton}'s action depends on it,
     * {@link #collapsedProperty()} does not.
     */
    void setOwner(@Nullable CollapsibleSplitPane<T> owner) {
        this.owner = owner;
    }

    /**
     * Called by the owning {@link CollapsibleSplitPane} whenever this item's maximized state (whether it is
     * the sole one not collapsed) might have changed, to keep {@link #collapseButton}'s icon/tooltip in sync.
     */
    void updateButton(boolean maximized) {
        ((FontIconView) collapseButton.getGraphic())
                .setIcon(maximized ? MaterialIcons.CHEVRON_UP : MaterialIcons.CHEVRON_DOWN);
        collapseButton.setTooltip(new Tooltip(maximized ? "Restore" : "Maximize"));
    }
}
