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

package com.techsenger.tabshell.kit.terminal;

import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import com.techsenger.jeditermfx.core.util.Platform;
import com.techsenger.jeditermfx.ui.settings.SettingsProvider;
import com.techsenger.mvvm4fx.core.HistoryPolicy;
import com.techsenger.tabshell.core.TabShellViewModel;
import com.techsenger.tabshell.core.tab.AbstractShellTabViewModel;
import com.techsenger.tabshell.core.tab.ShellTabKey;
import com.techsenger.tabshell.core.theme.TabShellTheme;
import com.techsenger.tabshell.kit.terminal.style.TerminalIcons;
import com.techsenger.tabshell.material.icon.FontIcon;
import com.techsenger.toolkit.fx.value.ObservableSource;
import com.techsenger.toolkit.fx.value.SimpleObservableSource;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Pavel Castornii
 */
public class TerminalTabViewModel extends AbstractShellTabViewModel {

    private TerminalPalette terminalPalette;

    private final ObservableSource<Boolean> focusRequired = new SimpleObservableSource<>();

    private final ChangeListener<? super TabShellTheme> themeListener = (ov, oldV, newV) -> {
        terminalPalette.setTheme(newV);
        focusRequired.next(true);
    };

    private final ReadOnlyStringWrapper selectedText = new ReadOnlyStringWrapper();

    private final BooleanProperty copyButtonDisabled = new SimpleBooleanProperty(true);

    private final BooleanProperty openUrlButtonDisabled = new SimpleBooleanProperty(true);

    private final ObservableList<TerminalPaletteType> paletteTypes = FXCollections.observableArrayList(
            Arrays.asList(TerminalPaletteType.values()));

    private final ObjectProperty<TerminalPaletteType> paletteType =
            new SimpleObjectProperty<>();

    private final PtyProcessTtyConnector ttyConnector;

    private FindPaneViewModel find;

    public TerminalTabViewModel(TabShellViewModel tabShell, String directory) {
        super(tabShell);
        this.ttyConnector = createTtyConnector(directory);
        this.setIcon(new FontIcon(TerminalIcons.TERMINAL));
        this.setTitle("Terminal");
        setHistoryPolicy(HistoryPolicy.ALL);
        setHistoryProvider(() -> tabShell.getHistoryManager().getHistory(TerminalHistory.class, TerminalHistory::new));
    }

    @Override
    public ShellTabKey getKey() {
        return TerminalComponentKeys.TERMINAL_TAB;
    }

    public ObjectProperty<TerminalPaletteType> paletteTypeProperty() {
        return this.paletteType;
    }

    public ReadOnlyStringProperty selectedTextProperty() {
        return this.selectedText.getReadOnlyProperty();
    }

    public BooleanProperty copyButtonDisabledProperty() {
        return copyButtonDisabled;
    }

    public BooleanProperty openUrlButtonDisabledProperty() {
        return openUrlButtonDisabled;
    }

    @Override
    protected void postHistoryRestore() {
        super.postHistoryRestore();
        this.terminalPalette = new TerminalPalette(getTabShell().getSettings().getAppearance().getTheme(),
                paletteType.get());
    }

    protected SettingsProvider createSettingsProvider() {
        var settings = getTabShell().getSettings();
        return new TerminalSettingsProvider(settings.getAppearance().getMonospaceFont(), terminalPalette);
    }

    protected PtyProcessTtyConnector createTtyConnector(String directory) {
        try {
            Map<String, String> envs = System.getenv();
            String[] command;
            if (Platform.isWindows()) {
                command = new String[]{"cmd.exe"};
            } else {
                command = new String[]{"/bin/bash", "--login"};
                envs = new HashMap<>(System.getenv());
                envs.put("TERM", "xterm-256color");
            }
            PtyProcess process = new PtyProcessBuilder()
                    .setDirectory(directory)
                    .setCommand(command).setEnvironment(envs).start();
            return new PtyProcessTtyConnector(process, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public PtyProcessTtyConnector getTtyConnector() {
        return ttyConnector;
    }

    protected void showFind() {
        if (this.find != null) {
            return;
        }
        this.find = new FindPaneViewModel(getTabShell().getHistoryManager(), selectedText.get());
        this.find.closeActionProperty().set(() -> hideFind());
        getComponentHelper().showFindPane(this.find);
    }

    protected void hideFind() {
        if (this.find == null) {
            return;
        }
        this.find = null;
        getComponentHelper().hideFindPane();
    }

    @Override
    public TerminalTabHelper<?> getComponentHelper() {
        return (TerminalTabHelper<?>) super.getComponentHelper();
    }

    protected void createNewTerminal() {
        //TODO
    }

    void addListeners() {
        getTabShell().getSettings().getAppearance().themeProperty().addListener(themeListener);
        this.paletteType.addListener((ov, oldV, newV) -> {
            this.terminalPalette.setPaletteType(newV);
            this.focusRequired.next(true);
        });
        this.selectedText.addListener((ov, oldV, newV) -> {
            this.copyButtonDisabled.set(newV == null);
            this.openUrlButtonDisabled.set(!isTextUrl(newV));
        });
    }

    void removeListeners() {
        getTabShell().getSettings().getAppearance().themeProperty().removeListener(themeListener);
    }

    ObservableList<TerminalPaletteType> getPaletteTypes() {
        return paletteTypes;
    }

    ReadOnlyStringWrapper selectedTextWrapper() {
        return selectedText;
    }

    ObservableSource<Boolean> getFocusRequired() {
        return focusRequired;
    }

    private boolean isTextUrl(String text) {
        if (text != null) {
            try {
                URI uri = new URI(text);
                uri.toURL();
                return true;
            } catch (Exception e) {
                //do nothing
            }
        }
        return false;
    }
}
