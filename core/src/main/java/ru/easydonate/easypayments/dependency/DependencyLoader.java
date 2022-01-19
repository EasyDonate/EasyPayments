/*
 * This file is part of VoidPointerFramework Bukkit plug-in.
 *
 * VoidPointerFramework is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VoidPointerFramework is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VoidPointerFramework. If not, see <https://www.gnu.org/licenses/>.
 */
package ru.easydonate.easypayments.dependency;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

/**
 * This abstraction assumes that every Dependency is
 *      file, and it may be loaded.
 *
 * @author VoidPointer aka NyanGuyMF
 */
public interface DependencyLoader {

    /**
     * Load given dependency .jar archive to current JVM process.
     *
     * @throws MalformedURLException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    boolean load(File dependencyFile) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, MalformedURLException, NoSuchFieldException;

}
