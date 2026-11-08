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

package com.techsenger.shellfx.demo.area;

import com.techsenger.annotations.Unmodifiable;
import com.techsenger.shellfx.core.area.AbstractAreaViewModel;
import com.techsenger.shellfx.core.dialog.DialogParams;
import com.techsenger.shellfx.core.popup.OverlayScope;
import com.techsenger.shellfx.core.settings.ShellSettings;
import com.techsenger.shellfx.core.window.WindowType;
import com.techsenger.shellfx.demo.dialog.DemoDailogResultButtons;
import com.techsenger.shellfx.layout.dockhost.DockUserPermission;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

/**
 *
 * @author Pavel Castornii
 */
public class DemoAreaViewModel<C extends DemoAreaComposer> extends AbstractAreaViewModel<C>
        implements DemoAreaPort {

    private final BooleanProperty fooDisabled = new SimpleBooleanProperty();

    private final BooleanProperty barIncluded = new SimpleBooleanProperty();

    private final BooleanProperty barDisabled = new SimpleBooleanProperty();

    private final Map<DockUserPermission, BooleanProperty> modifiableGrantedByPermission =
            new EnumMap<>(DockUserPermission.class);

    private final Map<DockUserPermission, BooleanProperty> grantedByPermission =
            Collections.unmodifiableMap(modifiableGrantedByPermission);

    private final SetChangeListener<DockUserPermission> dockUserPermissionsListener =
            c -> updateDockUserPermissions();

    private final ShellSettings settings;

    private ObservableSet<DockUserPermission> dockUserPermissions;

    public DemoAreaViewModel(DemoAreaParams params) {
        super(params);
        this.settings = params.getSettings();
    }

    @Override
    public boolean isFooDisabled() {
        return fooDisabled.get();
    }

    public void setFooDisabled(boolean fooDisabled) {
        this.fooDisabled.set(fooDisabled);
    }

    @Override
    public BooleanProperty fooDisabledProperty() {
        return fooDisabled;
    }

    @Override
    public boolean isBarIncluded() {
        return barIncluded.get();
    }

    public void setBarIncluded(boolean barIncluded) {
        this.barIncluded.set(barIncluded);
    }

    @Override
    public BooleanProperty barIncludedProperty() {
        return barIncluded;
    }

    @Override
    public boolean isBarDisabled() {
        return barDisabled.get();
    }

    public void setBarDisabled(boolean barDisabled) {
        this.barDisabled.set(barDisabled);
    }

    @Override
    public BooleanProperty barDisabledProperty() {
        return barDisabled;
    }

    /**
     * Returns the properties that are true while the dock host gives the user the permission; a change of a property
     * gives or takes away the permission.
     */
    public @Unmodifiable Map<DockUserPermission, BooleanProperty> getGrantedByPermission() {
        return grantedByPermission;
    }

    @Override
    protected void preInitialize() {
        super.preInitialize();
        for (var permission : DockUserPermission.values()) {
            var property = new SimpleBooleanProperty();
            property.addListener((ov, oldV, newV) -> updateDockHostPermission(permission, newV));
            modifiableGrantedByPermission.put(permission, property);
        }
    }

    @Override
    protected void postInitialize() {
        super.postInitialize();
        this.dockUserPermissions = getComposer().getDockHostPort().getUserPermissions();
        this.dockUserPermissions.addListener(dockUserPermissionsListener);
        updateDockUserPermissions();
    }

    @Override
    protected void preDeinitialize() {
        dockUserPermissions.removeListener(dockUserPermissionsListener);
        super.preDeinitialize();
    }

    protected void onDialogOpen(OverlayScope scope) {
        var params = new DialogParams(null, WindowType.NESTED, settings);
        var dialog = getComposer().openDemoDialog(scope, true, params);
        dialog.setOnResult((name) -> {
            if (name == DemoDailogResultButtons.OK) {
                dialog.closeSafely();
            }
        });
        dialog.setVisible(true);
    }

    protected void onPopupOpen(OverlayScope scope) {
        var popup = getComposer().openDemoPopup(scope);
        popup.setVisible(true);
    }

    /**
     * Copies the permissions of the dock host into the properties.
     */
    private void updateDockUserPermissions() {
        grantedByPermission.forEach((permission, property) ->
                property.set(dockUserPermissions.contains(permission)));
    }

    /**
     * Gives the dock host's user the permission or takes it away, following the property.
     */
    private void updateDockHostPermission(DockUserPermission permission, boolean permitted) {
        if (dockUserPermissions != null) {
            if (permitted) {
                dockUserPermissions.add(permission);
            } else {
                dockUserPermissions.remove(permission);
            }
        }
    }
}
