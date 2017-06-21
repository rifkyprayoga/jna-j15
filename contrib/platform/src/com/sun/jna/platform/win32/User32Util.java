/*
 * Copyright (c) 2013 Ralf Hamberger, Markus Karg, All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */

package com.sun.jna.platform.win32;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinDef.HINSTANCE;
import com.sun.jna.platform.win32.WinDef.HMENU;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPVOID;
import com.sun.jna.platform.win32.WinUser.RAWINPUTDEVICELIST;
import com.sun.jna.ptr.IntByReference;


/**
 * Provides convenient usage of functions defined by {@code User32.dll}.
 *
 * @author Ralf HAMBERGER
 * @author Markus KARG (markus[at]headcrashing[dot]eu)
 */
public final class User32Util {
    public static final int registerWindowMessage(final String lpString) {
        final int messageId = User32.INSTANCE.RegisterWindowMessage(lpString);
        if (messageId == 0)
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        return messageId;
    }

    public static final HWND createWindow(final String className, final String windowName, final int style, final int x, final int y, final int width,
            final int height, final HWND parent, final HMENU menu, final HINSTANCE instance, final LPVOID param) {
        return User32Util.createWindowEx(0, className, windowName, style, x, y, width, height, parent, menu, instance, param);
    }

    public static final HWND createWindowEx(final int exStyle, final String className, final String windowName, final int style, final int x, final int y,
            final int width, final int height, final HWND parent, final HMENU menu, final HINSTANCE instance, final LPVOID param) {
        final HWND hWnd = User32.INSTANCE
                .CreateWindowEx(exStyle, new WString(className), windowName, style, x, y, width, height, parent, menu, instance, param);
        if (hWnd == null)
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        return hWnd;
    }

    public static final void destroyWindow(final HWND hWnd) {
        if (!User32.INSTANCE.DestroyWindow(hWnd))
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
    }

    public static final List<RAWINPUTDEVICELIST> GetRawInputDeviceList() {
        IntByReference puiNumDevices = new IntByReference(0);
        RAWINPUTDEVICELIST placeholder = new RAWINPUTDEVICELIST();
        int cbSize = placeholder.sizeof();
        // first call is with NULL so we query the expected number of devices
        int returnValue = User32.INSTANCE.GetRawInputDeviceList(null, puiNumDevices, cbSize);
        if (returnValue != 0) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }

        int deviceCount = puiNumDevices.getValue();
        RAWINPUTDEVICELIST[] records = (RAWINPUTDEVICELIST[]) placeholder.toArray(deviceCount);
        returnValue = User32.INSTANCE.GetRawInputDeviceList(records, puiNumDevices, cbSize);
        if (returnValue == (-1)) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }

        if (returnValue != records.length) {
            throw new IllegalStateException("Mismatched allocated (" + records.length + ") vs. received devices count (" + returnValue + ")");
        }

        return Arrays.asList(records);
    }
}