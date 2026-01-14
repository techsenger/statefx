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

import java.util.Objects;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;

/**
 * Represents metadata about a property in a class. Contains information about property name, type signature,
 * raw type, and generic parameters.
 *
 * @author Pavel Castornii
 */
class PropertyMeta extends AbstractMeta {

    private Class<? extends ReadOnlyProperty> readOnlyType;

    private Class<? extends Property> type;

    private String wrapperType;

    private String genericParameter;

    private String valueType;

    private String getterPrefix;

    PropertyMeta(Class<?> ownerType, Class<?> rawType, String name) {
        super(ownerType, rawType, name, NameUtils.firstToUpperCase(name));
    }

    public Class<? extends ReadOnlyProperty> getReadOnlyType() {
        return readOnlyType;
    }

    public void setReadOnlyType(Class<? extends ReadOnlyProperty> readOnlyType) {
        this.readOnlyType = readOnlyType;
    }

    public Class<? extends Property> getType() {
        return type;
    }

    public void setType(Class<? extends Property> type) {
        this.type = type;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public String getGetterPrefix() {
        return getterPrefix;
    }

    public void setGetterPrefix(String getterPrefix) {
        this.getterPrefix = getterPrefix;
    }

    public String getGenericParameter() {
        return genericParameter;
    }

    public void setGenericParameter(String genericParameter) {
        this.genericParameter = genericParameter;
    }

    public String getWrapperType() {
        return wrapperType;
    }

    public void setWrapperType(String wrapperType) {
        this.wrapperType = wrapperType;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(getName());
        hash = 17 * hash + Objects.hashCode(this.type);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PropertyMeta other = (PropertyMeta) obj;
        if (!Objects.equals(getName(), other.getName())) {
            return false;
        }
        return Objects.equals(this.type, other.type);
    }
}
