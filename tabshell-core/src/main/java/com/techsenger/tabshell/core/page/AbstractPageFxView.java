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
import javafx.scene.control.Label;

/**
 *
 * @author Pavel Castornii
 */
public abstract class AbstractPageFxView<P extends PagePresenter<?, ?>>
        extends AbstractAreaFxView<P> implements PageFxView<P> {

    public class Composer extends AbstractAreaFxView.Composer implements PageFxView.Composer {

    }

    private final Label titleLabel = new Label();

    private final IconViewBox iconViewBox = new IconViewBox();

    public AbstractPageFxView() {
        super();
    }

    @Override
    public String getTitle() {
        return titleLabel.getText();
    }

    @Override
    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    @Override
    public Icon<?> getIcon() {
        return iconViewBox.getIcon();
    }

    @Override
    public void setIcon(Icon<?> icon) {
        iconViewBox.setIcon(icon);
    }

    protected Label getTitleLabel() {
        return titleLabel;
    }

    protected IconViewBox getIconViewBox() {
        return iconViewBox;
    }

    @Override
    public Composer getComposer() {
        return (Composer) super.getComposer();
    }

    @Override
    protected Composer createComposer() {
        return new AbstractPageFxView.Composer();
    }
}
