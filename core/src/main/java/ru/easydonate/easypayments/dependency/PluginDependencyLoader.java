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

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/** @author VoidPointer aka NyanGuyMF */
public final class PluginDependencyLoader implements DependencyLoader {

    private static final String ADD_URL_METHOD_NAME = "addURL";
    private static final String URL_CLASS_PATH_FIELD_NAME = "ucp";

    /**
     * Returns {@link DependencyLoader} instance for specified plugin or
     *      <tt>null</tt> if it cannot be created.
     */
    public static DependencyLoader forPlugin(final @NotNull Plugin plugin) {
        DependencyLoader dependencyLoader = null;
        try {
            dependencyLoader = new PluginDependencyLoader(plugin);
        } catch (IllegalArgumentException ignore) {}
        return dependencyLoader;
    }

    private final URLClassLoader pluginClassLoader;

    private PluginDependencyLoader(final @NotNull Plugin plugin) {
        ClassLoader pluginClassLoader = plugin.getClass().getClassLoader();
        if(pluginClassLoader instanceof URLClassLoader)
            this.pluginClassLoader = (URLClassLoader) pluginClassLoader;
        else
            throw new IllegalArgumentException("Plugin class loader is not instance of URLClassLoader");
    }

    /**
     * Load given dependency .jar archive to current JVM process
     *      using plugin {@link URLClassLoader}.
     * @throws MalformedURLException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    @Override
    public boolean load(final @NotNull File dependencyFile) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, MalformedURLException {
        Method addUrlMethod = getAddUrlMethod();
        if(addUrlMethod == null)
            return false;

        URL url = dependencyFile.toPath().toUri().toURL();
        addUrlMethod.invoke(pluginClassLoader, url);
        return true;
    }

    private Method getAddUrlMethod() {
        Method addUrlMethod = null;

        try {
            addUrlMethod = URLClassLoader.class.getDeclaredMethod(ADD_URL_METHOD_NAME, URL.class);
            addUrlMethod.setAccessible(true);
        } catch (Exception ignored) {}

        return addUrlMethod;
    }

}