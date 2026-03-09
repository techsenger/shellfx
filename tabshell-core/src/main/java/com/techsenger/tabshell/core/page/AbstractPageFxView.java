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

package com.techsenger.tabshell.core.page;

import com.techsenger.tabshell.core.area.AbstractAreaFxView;
import com.techsenger.tabshell.material.icon.Icon;
import com.techsenger.tabshell.material.icon.IconViewBox;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractPageFxView<P extends AbstractPagePresenter<?, ?>>
        extends AbstractAreaFxView<P> implements PageFxView<P> {

    public class Composer extends AbstractAreaFxView<P>.Composer implements PageFxView.Composer {

    }

    private final IconViewBox iconViewBox = new IconViewBox();

    private Icon<?> breadcrumbDivider;

    private final HBox breadcrumbsBox = new HBox();

    private final HBox titleBox = new HBox(iconViewBox, breadcrumbsBox);

    private final BooleanProperty selected = new SimpleBooleanProperty();

    public AbstractPageFxView() {
        super();
    }

    @Override
    public void setIcon(Icon<?> icon) {
        iconViewBox.setIcon(icon);
    }

    @Override
    public void setSelected(boolean value) {
        selected.set(value);
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    public void setBreadcrumbs(List<PageBreadcrumb> breadcrumbs) {
        this.breadcrumbsBox.getChildren().clear();
        for (var i = 0; i < breadcrumbs.size(); i++) {
            var b = breadcrumbs.get(i);
            var link = new Hyperlink(b.getText());
            if (b.getIcon() != null) {
                link.setGraphic(new IconViewBox(b.getIcon()));
            }
            breadcrumbsBox.getChildren().add(link);
            if (i + 1 < breadcrumbs.size()) {
                breadcrumbsBox.getChildren().add(createBreadcrumbDivider());
            }
        }
    }

    @Override
    public Icon<?> getBreadcrumbDivider() {
        return breadcrumbDivider;
    }

    public void setBreadcrumbDivider(Icon<?> breadcrumbDivider) {
        this.breadcrumbDivider = breadcrumbDivider;
    }

    protected BooleanProperty selectedProperty() {
        return selected;
    }

    protected IconViewBox getIconViewBox() {
        return iconViewBox;
    }

    protected HBox getBreadcrumbsBox() {
        return breadcrumbsBox;
    }

    protected HBox getTitleBox() {
        return titleBox;
    }

    @Override
    protected Composer createComposer() {
        return new AbstractPageFxView.Composer();
    }

    protected Node createBreadcrumbDivider() {
        if (this.breadcrumbDivider == null) {
            return new Label("/");
        } else {
            return new IconViewBox(breadcrumbDivider);
        }
    }

    @Override
    protected void build() {
        super.build();
        titleBox.getStylesheets().add(AbstractPageFxView.class.getResource("page-title.css").toExternalForm());
        titleBox.getStyleClass().add("title-box");
        breadcrumbsBox.getStyleClass().add("breadcrumbs-box");
        HBox.setHgrow(breadcrumbsBox, Priority.ALWAYS);
    }
}
