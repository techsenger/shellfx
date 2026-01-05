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

package com.techsenger.tabshell.core.settings.xml;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Pavel Castornii
 */
@XmlRootElement
public class XmlFontSettings {

    private String family;

    private double size;

    public XmlFontSettings() {

    }

    public XmlFontSettings(String family, double size) {
        this.family = family;
        this.size = size;
    }

    public double getSize() {
        return size;
    }

    @XmlAttribute(name = "size")
    public void setSize(double size) {
        this.size = size;
    }

    public String getFamily() {
        return family;
    }

    @XmlAttribute(name = "family")
    public void setFamily(String family) {
        this.family = family;
    }
}
