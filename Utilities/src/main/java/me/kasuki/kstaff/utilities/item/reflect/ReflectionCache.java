package me.kasuki.kstaff.utilities.item.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the reflection cache component.
 */
public final class ReflectionCache {

    private static final Map<String, Optional<Class<?>>> CLASS_CACHE = new ConcurrentHashMap<>();
    private static final Map<MethodKey, Optional<Method>> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Map<ConstructorKey, Optional<Constructor<?>>> CONSTRUCTOR_CACHE =
            new ConcurrentHashMap<>();
    private static final Map<EnumKey, Optional<Enum<?>>> ENUM_CACHE = new ConcurrentHashMap<>();

    /**
     * Creates a new ReflectionCache instance.
     */
    private ReflectionCache() {
    }

    /**
     * Returns the result of find class.
     *
     * @param name the name
     * @return the result of find class
     */
    public static Class<?> findClass(String name) {
        return CLASS_CACHE.computeIfAbsent(name, ReflectionCache::resolveClass).orElse(null);
    }

    /**
     * Returns the result of resolve class.
     *
     * @param name the name
     * @return the result of resolve class
     */
    private static Optional<Class<?>> resolveClass(String name) {
        try {
            return Optional.of(Class.forName(name));
        } catch (Throwable ignored) {
            return Optional.empty();
        }
    }

    /**
     * Returns the result of find method.
     *
     * @param owner          the owner
     * @param name           the name
     * @param parameterTypes the parameter types
     * @return the result of find method
     */
    public static Method findMethod(Class<?> owner, String name, Class<?>... parameterTypes) {
        if (owner == null) {
            return null;
        }

        MethodKey key = new MethodKey(owner, name, parameterTypes);
        return METHOD_CACHE.computeIfAbsent(key, ReflectionCache::resolveMethod).orElse(null);
    }

    /**
     * Returns the result of resolve method.
     *
     * @param key the key
     * @return the result of resolve method
     */
    private static Optional<Method> resolveMethod(MethodKey key) {
        try {
            Method method = key.owner.getMethod(key.name, key.parameterTypes);
            method.setAccessible(true);
            return Optional.of(method);
        } catch (Throwable ignored) {
            return Optional.empty();
        }
    }

    /**
     * Returns the result of find constructor.
     *
     * @param owner          the owner
     * @param parameterTypes the parameter types
     * @return the result of find constructor
     */
    public static Constructor<?> findConstructor(Class<?> owner, Class<?>... parameterTypes) {
        if (owner == null) {
            return null;
        }

        ConstructorKey key = new ConstructorKey(owner, parameterTypes);
        return CONSTRUCTOR_CACHE.computeIfAbsent(key, ReflectionCache::resolveConstructor).orElse(null);
    }

    /**
     * Returns the result of resolve constructor.
     *
     * @param key the key
     * @return the result of resolve constructor
     */
    private static Optional<Constructor<?>> resolveConstructor(ConstructorKey key) {
        try {
            Constructor<?> constructor = key.owner.getConstructor(key.parameterTypes);
            constructor.setAccessible(true);
            return Optional.of(constructor);
        } catch (Throwable ignored) {
            return Optional.empty();
        }
    }

    /**
     * Returns the result of enum value.
     *
     * @param <E>       the type parameter for e
     * @param enumClass the enum class
     * @param name      the name
     * @return the result of enum value
     */
    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> E enumValue(Class<?> enumClass, String name) {
        if (enumClass == null || !enumClass.isEnum() || name == null || name.isEmpty()) {
            return null;
        }

        EnumKey key = new EnumKey(enumClass, name);
        Optional<Enum<?>> value = ENUM_CACHE.computeIfAbsent(key, ReflectionCache::resolveEnumValue);
        return (E) value.orElse(null);
    }

    /**
     * Returns the result of resolve enum value.
     *
     * @param key the key
     * @return the result of resolve enum value
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Optional<Enum<?>> resolveEnumValue(EnumKey key) {
        try {
            Class<? extends Enum> rawEnumClass = (Class<? extends Enum>) key.enumClass;
            Enum<?> value = Enum.valueOf(rawEnumClass, key.name);
            return Optional.of(value);
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    /**
     * Represents the method key component.
     */
    private static final class MethodKey {
        private final Class<?> owner;
        private final String name;
        private final Class<?>[] parameterTypes;
        private final int hash;

        /**
         * Creates a new MethodKey instance.
         *
         * @param owner          the owner
         * @param name           the name
         * @param parameterTypes the parameter types
         */
        private MethodKey(Class<?> owner, String name, Class<?>[] parameterTypes) {
            this.owner = owner;
            this.name = name;
            this.parameterTypes = parameterTypes == null ? new Class<?>[0] : parameterTypes.clone();
            this.hash = computeHash();
        }

        /**
         * Returns the result of compute hash.
         *
         * @return the result of compute hash
         */
        private int computeHash() {
            int result = owner.hashCode();
            result = 31 * result + name.hashCode();
            result = 31 * result + Arrays.hashCode(parameterTypes);
            return result;
        }

        /**
         * Returns the result of equals.
         *
         * @param object the object
         * @return whether equals
         */
        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof MethodKey)) {
                return false;
            }

            MethodKey that = (MethodKey) object;
            return owner.equals(that.owner)
                    && name.equals(that.name)
                    && Arrays.equals(parameterTypes, that.parameterTypes);
        }

        /**
         * Returns the hash code.
         *
         * @return the hash code
         */
        @Override
        public int hashCode() {
            return hash;
        }
    }

    /**
     * Represents the constructor key component.
     */
    private static final class ConstructorKey {
        private final Class<?> owner;
        private final Class<?>[] parameterTypes;
        private final int hash;

        /**
         * Creates a new ConstructorKey instance.
         *
         * @param owner          the owner
         * @param parameterTypes the parameter types
         */
        private ConstructorKey(Class<?> owner, Class<?>[] parameterTypes) {
            this.owner = owner;
            this.parameterTypes = parameterTypes == null ? new Class<?>[0] : parameterTypes.clone();
            this.hash = computeHash();
        }

        /**
         * Returns the result of compute hash.
         *
         * @return the result of compute hash
         */
        private int computeHash() {
            int result = owner.hashCode();
            result = 31 * result + Arrays.hashCode(parameterTypes);
            return result;
        }

        /**
         * Returns the result of equals.
         *
         * @param object the object
         * @return whether equals
         */
        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof ConstructorKey)) {
                return false;
            }

            ConstructorKey that = (ConstructorKey) object;
            return owner.equals(that.owner) && Arrays.equals(parameterTypes, that.parameterTypes);
        }

        /**
         * Returns the hash code.
         *
         * @return the hash code
         */
        @Override
        public int hashCode() {
            return hash;
        }
    }

    /**
     * Represents the enum key component.
     */
    private static final class EnumKey {
        private final Class<?> enumClass;
        private final String name;
        private final int hash;

        /**
         * Creates a new EnumKey instance.
         *
         * @param enumClass the enum class
         * @param name      the name
         */
        private EnumKey(Class<?> enumClass, String name) {
            this.enumClass = enumClass;
            this.name = name;
            this.hash = 31 * enumClass.hashCode() + name.hashCode();
        }

        /**
         * Returns the result of equals.
         *
         * @param object the object
         * @return whether equals
         */
        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof EnumKey)) {
                return false;
            }

            EnumKey that = (EnumKey) object;
            return enumClass.equals(that.enumClass) && name.equals(that.name);
        }

        /**
         * Returns the hash code.
         *
         * @return the hash code
         */
        @Override
        public int hashCode() {
            return hash;
        }
    }
}
