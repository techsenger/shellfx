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

package com.techsenger.tabshell.terminal;

import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import com.techsenger.jeditermfx.core.ProcessTtyConnector;
import com.techsenger.jeditermfx.core.util.TermSize;
import java.nio.charset.Charset;
import java.util.List;

/**
 *
 * @author Pavel Castornii
 */
public class PtyProcessTtyConnector extends ProcessTtyConnector {

    private final PtyProcess myProcess;

    PtyProcessTtyConnector(PtyProcess process, Charset charset) {
        this(process, charset, null);
    }

    PtyProcessTtyConnector(PtyProcess process, Charset charset, List<String> commandLine) {
        super(process, charset, commandLine);
        myProcess = process;
    }

    @Override
    public void resize(TermSize termSize) {
        if (isConnected()) {
            myProcess.setWinSize(new WinSize(termSize.getColumns(), termSize.getRows()));
        }
    }

    @Override
    public boolean isConnected() {
        return myProcess.isAlive();
    }

    @Override
    public String getName() {
        return "Local";
    }
}
