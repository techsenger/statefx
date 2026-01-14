/*
 * Copyright (c) 2026 Pavel Castornii. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation. This particular file is
 * subject to the "Classpath" exception as provided in the LICENSE file
 * that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.techsenger.statefx.core.impl;

import java.util.List;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;

/**
 *
 * @author Pavel Castornii
 */
class AnalyzerResult {

    private final List<PropertyMeta> properties;

    private final List<ContainerMeta<ObservableList<?>>> lists;

    private final List<ContainerMeta<ObservableSet<?>>> sets;

    private final List<ContainerMeta<ObservableMap<?, ?>>> maps;

    AnalyzerResult(List<PropertyMeta> properties, List<ContainerMeta<ObservableList<?>>> lists,
            List<ContainerMeta<ObservableSet<?>>> sets, List<ContainerMeta<ObservableMap<?, ?>>> maps) {
        this.properties = properties;
        this.lists = lists;
        this.sets = sets;
        this.maps = maps;
    }

    public List<PropertyMeta> getProperties() {
        return properties;
    }

    public List<ContainerMeta<ObservableList<?>>> getLists() {
        return lists;
    }

    public List<ContainerMeta<ObservableSet<?>>> getSets() {
        return sets;
    }

    public List<ContainerMeta<ObservableMap<?, ?>>> getMaps() {
        return maps;
    }
}
