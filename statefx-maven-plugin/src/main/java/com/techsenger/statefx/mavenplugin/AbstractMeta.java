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

package com.techsenger.statefx.mavenplugin;

/**
 *
 * @author Pavel Castornii
 */
abstract class AbstractMeta {

    private final Class<?> ownerType;

    private final String name;

    private final String capitalizedName;

    private final Class<?> rawType;

    /**
     * Constructs a new PropertyInfo instance.
     *
     * @param ownerType the class that declares this property/collection
     * @param rawType the raw class type without generic parameters
     * @param capitalizedName the name with the first letter in upper case
     */
    AbstractMeta(Class<?> ownerType, Class<?> rawType, String name, String capitalizedName) {
        this.ownerType = ownerType;
        this.rawType = rawType;
        this.name = name;
        this.capitalizedName = capitalizedName;
    }

    public Class<?> getOwnerType() {
        return ownerType;
    }

    public String getName() {
        return name;
    }

    public String getCapitalizedName() {
        return capitalizedName;
    }

    public Class<?> getRawType() {
        return rawType;
    }
}
