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

import java.lang.reflect.Method;

/**
 *
 * @author Pavel Castornii
 */
class PropertyMeta extends AbstractMeta {

    private final Class<?> implType;

    private Method method;

    private Method getter;

    private Method setter;

    private String setDescriptor;

    private String getDescriptor;

    private boolean referenceType;

    private int getterReturnOpcode;

    private int setterLoadOpcode;

    PropertyMeta(String name, Class<?> type, Class<?> implType) {
        super(name, type);
        this.implType = implType;
    }

    public Class<?> getImplType() {
        return implType;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Method getGetter() {
        return getter;
    }

    public void setGetter(Method getter) {
        this.getter = getter;
    }

    public Method getSetter() {
        return setter;
    }

    public void setSetter(Method setter) {
        this.setter = setter;
    }

    public String getSetDescriptor() {
        return setDescriptor;
    }

    public void setSetDescriptor(String setDescriptor) {
        this.setDescriptor = setDescriptor;
    }

    public String getGetDescriptor() {
        return getDescriptor;
    }

    public void setGetDescriptor(String getDescriptor) {
        this.getDescriptor = getDescriptor;
    }

    public boolean isReferenceType() {
        return referenceType;
    }

    public void setReferenceType(boolean referenceType) {
        this.referenceType = referenceType;
    }

    public int getGetterReturnOpcode() {
        return getterReturnOpcode;
    }

    public void setGetterReturnOpcode(int getterReturnOpcode) {
        this.getterReturnOpcode = getterReturnOpcode;
    }

    public int getSetterLoadOpcode() {
        return setterLoadOpcode;
    }

    public void setSetterLoadOpcode(int setterLoadOpcode) {
        this.setterLoadOpcode = setterLoadOpcode;
    }
}
