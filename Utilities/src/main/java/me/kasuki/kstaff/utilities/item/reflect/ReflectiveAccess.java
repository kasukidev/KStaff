package me.kasuki.kstaff.utilities.item.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Represents the reflective access component.
 */
public final class ReflectiveAccess {

    /**
     * Creates a new ReflectiveAccess instance.
     */
    private ReflectiveAccess() {
    }

    /**
     * Returns the result of invoke.
     *
     * @param target     the target
     * @param methodName the method name
     * @return the result of invoke
     */
    public static Object invoke(Object target, String methodName) {
        if (target == null) {
            return null;
        }

        Method method = ReflectionCache.findMethod(target.getClass(), methodName);
        if (method == null) {
            return null;
        }

        try {
            return method.invoke(target);
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Returns the result of invoke.
     *
     * @param target         the target
     * @param methodName     the method name
     * @param parameterTypes the parameter types
     * @param args           the args
     * @return the result of invoke
     */
    public static Object invoke(
            Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        if (target == null) {
            return null;
        }

        Method method = ReflectionCache.findMethod(target.getClass(), methodName, parameterTypes);
        if (method == null) {
            return null;
        }

        try {
            return method.invoke(target, args);
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Returns the result of invoke void.
     *
     * @param target         the target
     * @param methodName     the method name
     * @param parameterTypes the parameter types
     * @param args           the args
     * @return whether invoke void
     */
    public static boolean invokeVoid(
            Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        return invoke(target, methodName, parameterTypes, args) != null
                || ReflectionCache.findMethod(
                target == null ? null : target.getClass(), methodName, parameterTypes)
                != null;
    }

    /**
     * Returns the result of invoke static.
     *
     * @param owner          the owner
     * @param methodName     the method name
     * @param parameterTypes the parameter types
     * @param args           the args
     * @return the result of invoke static
     */
    public static Object invokeStatic(
            Class<?> owner, String methodName, Class<?>[] parameterTypes, Object... args) {
        if (owner == null) {
            return null;
        }

        Method method = ReflectionCache.findMethod(owner, methodName, parameterTypes);
        if (method == null) {
            return null;
        }

        try {
            return method.invoke(null, args);
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Returns the result of construct.
     *
     * @param owner          the owner
     * @param parameterTypes the parameter types
     * @param args           the args
     * @return the result of construct
     */
    public static Object construct(Class<?> owner, Class<?>[] parameterTypes, Object... args) {
        Constructor<?> constructor = ReflectionCache.findConstructor(owner, parameterTypes);
        if (constructor == null) {
            return null;
        }

        try {
            return constructor.newInstance(args);
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Returns whether a matching method exists.
     *
     * @param owner          the owner
     * @param methodName     the method name
     * @param parameterTypes the parameter types
     * @return whether a matching method exists
     */
    public static boolean hasMethod(Class<?> owner, String methodName, Class<?>... parameterTypes) {
        return ReflectionCache.findMethod(owner, methodName, parameterTypes) != null;
    }

    /**
     * Returns the result of invoke string.
     *
     * @param target     the target
     * @param methodName the method name
     * @return the result of invoke string
     */
    public static String invokeString(Object target, String methodName) {
        Object value = invoke(target, methodName);
        return value instanceof String ? (String) value : "";
    }

    /**
     * Returns the result of invoke integer.
     *
     * @param target     the target
     * @param methodName the method name
     * @return the result of invoke integer
     */
    public static Integer invokeInteger(Object target, String methodName) {
        Object value = invoke(target, methodName);
        return value instanceof Integer ? (Integer) value : null;
    }

    /**
     * Returns the result of invoke double.
     *
     * @param target     the target
     * @param methodName the method name
     * @return the result of invoke double
     */
    public static Double invokeDouble(Object target, String methodName) {
        Object value = invoke(target, methodName);
        return value instanceof Double ? (Double) value : null;
    }

    /**
     * Returns the result of invoke boolean.
     *
     * @param target     the target
     * @param methodName the method name
     * @return whether invoke boolean
     */
    public static Boolean invokeBoolean(Object target, String methodName) {
        Object value = invoke(target, methodName);
        return value instanceof Boolean ? (Boolean) value : null;
    }

    /**
     * Returns the result of enum value.
     *
     * @param <E>       the type parameter for e
     * @param enumClass the enum class
     * @param name      the name
     * @return the result of enum value
     */
    public static <E extends Enum<E>> E enumValue(Class<?> enumClass, String name) {
        return ReflectionCache.enumValue(enumClass, name);
    }
}
