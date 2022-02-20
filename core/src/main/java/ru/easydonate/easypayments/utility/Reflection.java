package ru.easydonate.easypayments.utility;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public final class Reflection {

    public static @Nullable Method getMethod(@NotNull Class<?> type, @NotNull String methodName, @Nullable Class<?>... parameters) {
        try {
            return type.getMethod(methodName, parameters);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static @Nullable Method getDeclaredMethod(@NotNull Class<?> type, @NotNull String methodName, @Nullable Class<?>... parameters) {
        try {
            Method method = type.getDeclaredMethod(methodName, parameters);
            method.setAccessible(true);
            return method;
        } catch (Exception ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull Optional<T> invokeMethod(@Nullable Method method, @Nullable Object instance, @Nullable Object... parameters) {
        try {
            T returnedValue = (T) method.invoke(instance, parameters);
            return Optional.ofNullable(returnedValue);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    public static boolean invokeVoidMethod(@Nullable Method method, @Nullable Object instance, @Nullable Object... parameters) {
        try {
            method.invoke(instance, parameters);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static @NotNull Set<Method> findAllMethods(@NotNull Class<?> type) {
        Set<Method> methods = new LinkedHashSet<>();
        walkClassTree(type, t -> Collections.addAll(methods, t.getMethods()));
        return methods;
    }

    public static @NotNull Set<Method> findAllDeclaredMethods(@NotNull Class<?> type) {
        Set<Method> methods = new LinkedHashSet<>();
        walkClassTree(type, t -> Collections.addAll(methods, t.getDeclaredMethods()));
        return methods;
    }

    public static @NotNull Set<Field> findAllFields(@NotNull Class<?> type) {
        Set<Field> fields = new LinkedHashSet<>();
        walkClassTree(type, t -> Collections.addAll(fields, t.getFields()));
        return fields;
    }

    public static @NotNull Set<Field> findAllDeclaredFields(@NotNull Class<?> type) {
        Set<Field> fields = new LinkedHashSet<>();
        walkClassTree(type, t -> Collections.addAll(fields, t.getDeclaredFields()));
        return fields;
    }

    public static <A extends Annotation> @NotNull Map<Method, A> findAnnotatedMethods(@NotNull Class<?> type, @NotNull Class<A> annotationType) {
        return extractAnnotations(findAllMethods(type), annotationType, Method::getAnnotation);
    }

    public static <A extends Annotation> @NotNull Map<Method, A> findAnnotatedDeclaredMethods(@NotNull Class<?> type, @NotNull Class<A> annotationType) {
        return extractAnnotations(findAllDeclaredMethods(type), annotationType, Method::getAnnotation);
    }

    public static <A extends Annotation> @NotNull Map<Field, A> findAnnotatedFields(@NotNull Class<?> type, @NotNull Class<A> annotationType) {
        return extractAnnotations(findAllFields(type), annotationType, Field::getAnnotation);
    }

    public static <A extends Annotation> @NotNull Map<Field, A> findAnnotatedDeclaredFields(@NotNull Class<?> type, @NotNull Class<A> annotationType) {
        return extractAnnotations(findAllDeclaredFields(type), annotationType, Field::getAnnotation);
    }

    public static <T, A extends Annotation> @NotNull Map<T, A> extractAnnotations(
            @NotNull Collection<T> collection,
            @NotNull Class<A> annotationType,
            @NotNull BiFunction<T, Class<A>, A> annotationExtractor
    ) {
        Map<T, A> annotatedElements = new LinkedHashMap<>();
        for(T element : collection) {
            A annotation = annotationExtractor.apply(element, annotationType);
            if(annotation != null) {
                annotatedElements.put(element, annotation);
            }
        }

        return annotatedElements;
    }

    private static void walkClassTree(@NotNull Class<?> type, @NotNull Consumer<Class<?>> typeConsumer) {
        Class<?> clazz = type;
        while(clazz != null) {
            typeConsumer.accept(clazz);
            clazz = clazz.getSuperclass();
        }
    }

}
