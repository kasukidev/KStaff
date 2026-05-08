package me.kasuki.kstaff.api;

import lombok.Getter;
import me.kasuki.kstaff.api.data.ILoadable;
import me.kasuki.kstaff.api.data.IUnloadable;
import me.kasuki.kstaff.api.module.IModule;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central module and service registry for KStaff.
 *
 * <p>This registry supports two related but distinct registration models:
 *
 * <ul>
 *   <li>Typed registration via {@link #register(Class, Object)} for service lookup by class.
 *   <li>Dependency-aware module registration via {@link #registerModules(Collection)} for modules
 *       identified by {@link IModule#getId()} and ordered by declared dependencies.
 * </ul>
 *
 * <p>Modules may optionally implement {@link ILoadable} and/or {@link IUnloadable} to participate
 * in lifecycle callbacks during registration, unregistration, and shutdown.
 *
 * <p>This type is intended for plugin-side runtime use and is not a general-purpose dependency
 * injection container.
 */
public final class KStaffAPI {

    /**
     * Global KStaff API instance.
     *
     * <p>This is published as a volatile reference for visibility across threads. Construction is not
     * otherwise synchronized, so this should still be treated as bootstrap-time initialization.
     */
    @Getter
    private static volatile KStaffAPI instance;

    /**
     * Maps service types to registered instances.
     */
    private final Map<Class<?>, Object> moduleMap = new ConcurrentHashMap<>();

    /**
     * Maps module ids to registered modules.
     */
    private final Map<String, IModule> moduleIdMap = new ConcurrentHashMap<>();

    /**
     * Tracks module load order by id.
     *
     * <p>This is used to unload dependency-aware modules in reverse order during shutdown.
     */
    private final List<String> loadOrder = Collections.synchronizedList(new ArrayList<>());

    /**
     * Logger used for lifecycle and failure reporting.
     */
    private final Logger logger;

    /**
     * Indicates whether this registry has been shut down.
     */
    private volatile boolean shutdown;

    /**
     * Error message used when a type argument is null.
     */
    private static final String TYPE_NULL_MESSAGE = "type cannot be null";

    /**
     * Error message used when a module id argument is null.
     */
    private static final String ID_NULL_MESSAGE = "module id cannot be null";

    /**
     * Error message used when a module argument is null.
     */
    private static final String MODULE_NULL_MESSAGE = "module cannot be null";

    /**
     * Creates a new KStaff API registry.
     *
     * @param logger the logger to use for lifecycle and failure reporting
     * @throws NullPointerException  if {@code logger} is null
     * @throws IllegalStateException if a global {@link KStaffAPI} instance already exists
     */
    public KStaffAPI(Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger cannot be null");

        if (instance != null) {
            throw new IllegalStateException("KStaffAPI already initialized.");
        }

        instance = this;
    }

    /**
     * Registers or replaces a typed service instance.
     *
     * <p>If the supplied instance also implements {@link IModule}, its module id is validated and
     * indexed. If the instance implements {@link ILoadable}, its {@link ILoadable#load()} method is
     * invoked after registration state is prepared.
     *
     * <p>If loading fails, the previous registration state is restored.
     *
     * @param type   the exposed service type
     * @param module the implementation instance
     * @param <T>    the service type
     * @throws NullPointerException     if {@code type} or {@code module} is null
     * @throws IllegalStateException    if the registry has been shut down or if the module id conflicts
     *                                  with another registered module
     * @throws IllegalArgumentException if {@code module} is not assignable to {@code type}
     * @throws RuntimeException         if loading fails and the implementation throws a runtime exception
     * @throws Error                    if loading fails and the implementation throws an error
     */
    public <T> void register(Class<T> type, T module) {
        Objects.requireNonNull(type, TYPE_NULL_MESSAGE);
        Objects.requireNonNull(module, MODULE_NULL_MESSAGE);
        this.ensureActive();

        if (!type.isInstance(module)) {
            throw new IllegalArgumentException(
                    module.getClass().getName() + " does not implement/extend " + type.getName());
        }

        IModule newModule = (module instanceof IModule) ? (IModule) module : null;
        if (newModule != null) {
            this.validateModuleId(newModule);
        }

        Object old = this.moduleMap.put(type, module);

        if (newModule != null) {
            IModule existingById = this.moduleIdMap.get(newModule.getId());

            if (existingById != null && existingById != module && existingById != old) {
                if (old == null) {
                    this.moduleMap.remove(type, module);
                } else {
                    this.moduleMap.put(type, old);
                }
                throw new IllegalStateException("Duplicate module id detected: " + newModule.getId());
            }

            this.moduleIdMap.put(newModule.getId(), newModule);
        }

        try {
            if (module instanceof ILoadable) {
                ((ILoadable) module).load();
            }
        } catch (Throwable throwable) {
            this.rollbackRegistration(type, module, old);
            this.logger.log(Level.SEVERE, "Load failed for " + type.getName(), throwable);
            throw throwable;
        }

        this.logger.info("Registered " + type.getName() + " -> " + module.getClass().getName());

        if (old instanceof IUnloadable && !this.isStillRegistered(old)) {
            this.safeUnload(type.getName(), (IUnloadable) old);
        }
    }

    /**
     * Registers and loads a dependency-aware module batch.
     *
     * <p>Each module is validated, checked for duplicate ids, checked for missing dependencies, and
     * then loaded in topological order according to its declared dependencies.
     *
     * <p>This method only indexes modules by id. It does not automatically register service-type
     * bindings in {@link #moduleMap}. If typed lookup is also required, those service interfaces must
     * be registered separately via {@link #register(Class, Object)}.
     *
     * <p>If any module fails to load, previously loaded modules from this batch are rolled back in
     * reverse order and unload callbacks are invoked where supported.
     *
     * @param modules the modules to register and load
     * @throws NullPointerException  if {@code modules} is null or contains a null element
     * @throws IllegalStateException if the registry has been shut down, a duplicate id is found, a
     *                               dependency is missing, or a cyclic dependency exists
     * @throws RuntimeException      if loading fails and an implementation throws a runtime exception
     * @throws Error                 if loading fails and an implementation throws an error
     */
    public void registerModules(Collection<? extends IModule> modules) {
        Objects.requireNonNull(modules, "modules cannot be null");
        this.ensureActive();

        Map<String, IModule> pending = new LinkedHashMap<>();
        for (IModule module : modules) {
            Objects.requireNonNull(module, MODULE_NULL_MESSAGE);
            this.validateModuleId(module);

            IModule old = pending.putIfAbsent(module.getId(), module);
            if (old != null) {
                throw new IllegalStateException("Duplicate module id in batch: " + module.getId());
            }

            if (this.moduleIdMap.containsKey(module.getId())) {
                throw new IllegalStateException("Module id already registered: " + module.getId());
            }
        }

        this.validateDependenciesExist(pending);
        List<IModule> ordered = this.topologicalSort(pending);
        List<IModule> loaded = new ArrayList<>();

        try {
            for (IModule module : ordered) {
                this.moduleIdMap.put(module.getId(), module);
                this.loadOrder.add(module.getId());

                if (module instanceof ILoadable) {
                    ((ILoadable) module).load();
                }

                loaded.add(module);
                this.logger.info("Loaded module " + module.getId());
            }
        } catch (Throwable throwable) {
            this.logger.log(Level.SEVERE, "Module batch load failed. Rolling back.", throwable);

            Collections.reverse(loaded);
            for (IModule module : loaded) {
                this.moduleIdMap.remove(module.getId(), module);
                this.loadOrder.remove(module.getId());

                if (module instanceof IUnloadable) {
                    this.safeUnload(module.getId(), (IUnloadable) module);
                }
            }

            throw throwable;
        }
    }

    /**
     * Returns the registered service for the given type.
     *
     * @param type the service type
     * @param <T>  the service type
     * @return the registered instance
     * @throws NullPointerException  if {@code type} is null
     * @throws IllegalStateException if no instance is registered for {@code type}
     */
    public <T> T get(Class<T> type) {
        Objects.requireNonNull(type, TYPE_NULL_MESSAGE);

        Object object = this.moduleMap.get(type);
        if (object == null) {
            throw new IllegalStateException("Module not initialized: " + type.getName());
        }

        return type.cast(object);
    }

    /**
     * Returns the registered service for the given type if present.
     *
     * @param type the service type
     * @param <T>  the service type
     * @return an optional containing the registered instance if present
     * @throws NullPointerException if {@code type} is null
     */
    public <T> Optional<T> maybe(Class<T> type) {
        Objects.requireNonNull(type, TYPE_NULL_MESSAGE);
        return Optional.ofNullable(type.cast(this.moduleMap.get(type)));
    }

    /**
     * Returns the registered module for the given id.
     *
     * @param id the module id
     * @return the registered module
     * @throws NullPointerException  if {@code id} is null
     * @throws IllegalStateException if no module is registered for {@code id}
     */
    public IModule getModule(String id) {
        Objects.requireNonNull(id, ID_NULL_MESSAGE);

        IModule module = this.moduleIdMap.get(id);
        if (module == null) {
            throw new IllegalStateException("Module not initialized: " + id);
        }

        return module;
    }

    /**
     * Returns the registered module for the given id if present.
     *
     * @param id the module id
     * @return an optional containing the registered module if present
     * @throws NullPointerException if {@code id} is null
     */
    public Optional<IModule> maybeModule(String id) {
        Objects.requireNonNull(id, ID_NULL_MESSAGE);
        return Optional.ofNullable(this.moduleIdMap.get(id));
    }

    /**
     * Unregisters a typed service binding.
     *
     * <p>If the removed instance is the last remaining registration of a module, dependency checks
     * are enforced before removal and unload callbacks are invoked where supported.
     *
     * @param type the service type to unregister
     * @param <T>  the service type
     * @throws NullPointerException  if {@code type} is null
     * @throws IllegalStateException if the removed module is still required by another registered
     *                               module
     */
    public <T> void unregister(Class<T> type) {
        Objects.requireNonNull(type, TYPE_NULL_MESSAGE);

        Object current = this.moduleMap.get(type);
        if (current instanceof IModule) {
            IModule module = (IModule) current;

            boolean registeredElsewhere = false;
            for (Map.Entry<Class<?>, Object> entry : this.moduleMap.entrySet()) {
                if (entry.getKey() != type && entry.getValue() == current) {
                    registeredElsewhere = true;
                    break;
                }
            }

            if (!registeredElsewhere) {
                this.ensureNoDependents(module.getId());
            }
        }

        Object old = this.moduleMap.remove(type);
        if (old == null) {
            return;
        }

        boolean stillRegistered = this.isStillRegistered(old);

        if (!stillRegistered && old instanceof IModule) {
            IModule module = (IModule) old;
            this.moduleIdMap.remove(module.getId(), module);
            this.loadOrder.remove(module.getId());
        }

        if (!stillRegistered && old instanceof IUnloadable) {
            this.safeUnload(type.getName(), (IUnloadable) old);
        }
    }

    /**
     * Returns whether the given instance is still registered under at least one service type.
     *
     * @param target the instance to check
     * @return {@code true} if the instance is still registered
     */
    private boolean isStillRegistered(Object target) {
        for (Object value : this.moduleMap.values()) {
            if (value == target) {
                return true;
            }
        }
        return false;
    }

    /**
     * Unregisters a module by its id.
     *
     * <p>If other loaded modules depend on the target module, unregistration is rejected.
     *
     * @param id the module id
     * @throws NullPointerException  if {@code id} is null
     * @throws IllegalStateException if another module depends on the target module
     */
    public void unregisterModule(String id) {
        Objects.requireNonNull(id, ID_NULL_MESSAGE);

        this.ensureNoDependents(id);

        IModule module = this.moduleIdMap.remove(id);
        if (module == null) {
            return;
        }

        this.loadOrder.remove(id);
        this.moduleMap.entrySet().removeIf(entry -> entry.getValue() == module);

        if (module instanceof IUnloadable) {
            this.safeUnload(id, (IUnloadable) module);
        }
    }

    /**
     * Ensures that no registered module currently depends on the supplied module id.
     *
     * @param id the module id to check
     * @throws IllegalStateException if a dependent module is found
     */
    private void ensureNoDependents(String id) {
        for (IModule module : this.moduleIdMap.values()) {
            if (module.getDependencies().contains(id)) {
                throw new IllegalStateException(
                        "Cannot unregister module '" + id + "' because '" + module.getId() + "' depends on it");
            }
        }
    }

    /**
     * Shuts down the registry.
     *
     * <p>Dependency-aware modules are unloaded in reverse load order first. Any remaining typed
     * registrations that support unloading are then unloaded once each. All internal state is cleared
     * afterward.
     */
    public void shutdown() {
        if (this.shutdown) {
            return;
        }

        this.shutdown = true;

        List<String> orderedIds = new ArrayList<>(this.loadOrder);
        Collections.reverse(orderedIds);

        Set<Object> unloaded = new LinkedHashSet<>();

        for (String id : orderedIds) {
            IModule module = this.moduleIdMap.remove(id);
            if (module instanceof IUnloadable && unloaded.add(module)) {
                this.safeUnload(id, (IUnloadable) module);
            }
        }

        for (Object object : new LinkedHashSet<Object>(this.moduleMap.values())) {
            if (object instanceof IUnloadable && unloaded.add(object)) {
                this.safeUnload(object.getClass().getName(), (IUnloadable) object);
            }
        }

        this.moduleMap.clear();
        this.moduleIdMap.clear();
        this.loadOrder.clear();

        if (instance == this) {
            instance = null;
        }
    }

    /**
     * Restores previous registration state after a failed typed registration attempt.
     *
     * @param type   the service type that was being registered
     * @param module the new module that failed to load
     * @param old    the previous binding for {@code type}, or {@code null} if none existed
     */
    private void rollbackRegistration(Class<?> type, Object module, Object old) {
        if (old == null) {
            this.moduleMap.remove(type, module);
        } else {
            this.moduleMap.replace(type, module, old);
        }

        if (module instanceof IModule) {
            IModule failedModule = (IModule) module;
            this.moduleIdMap.remove(failedModule.getId(), failedModule);
        }

        if (old instanceof IModule) {
            IModule oldModule = (IModule) old;
            this.moduleIdMap.put(oldModule.getId(), oldModule);
        }
    }

    /**
     * Validates that a module id is non-null and non-blank.
     *
     * @param module the module whose id is being validated
     * @throws NullPointerException     if the module id is null
     * @throws IllegalArgumentException if the module id is blank
     */
    private void validateModuleId(IModule module) {
        String id = Objects.requireNonNull(module.getId(), ID_NULL_MESSAGE).trim();
        if (id.isEmpty()) {
            throw new IllegalArgumentException("module id cannot be blank");
        }
    }

    /**
     * Validates that all declared dependencies in the supplied module batch exist.
     *
     * <p>Dependencies may be satisfied either by another module in the batch or by an already
     * registered module.
     *
     * @param modules the pending module batch keyed by module id
     * @throws IllegalStateException if a module depends on itself or references a missing dependency
     */
    private void validateDependenciesExist(Map<String, IModule> modules) {
        for (IModule module : modules.values()) {
            for (String dependency : this.safeDependencies(module)) {
                if (dependency.equals(module.getId())) {
                    throw new IllegalStateException("Module cannot depend on itself: " + module.getId());
                }

                if (!modules.containsKey(dependency) && !this.moduleIdMap.containsKey(dependency)) {
                    throw new IllegalStateException(
                            "Missing dependency '" + dependency + "' for module '" + module.getId() + "'");
                }
            }
        }
    }

    /**
     * Performs a topological sort over the supplied module batch.
     *
     * <p>Only dependencies within the supplied batch participate in the ordering graph. Dependencies
     * already satisfied by previously registered modules are treated as pre-resolved.
     *
     * @param modules the pending module batch keyed by module id
     * @return the modules in dependency-safe load order
     * @throws IllegalStateException if a cyclic dependency is detected
     */
    private List<IModule> topologicalSort(Map<String, IModule> modules) {
        Map<String, Integer> indegree = new LinkedHashMap<>();
        Map<String, Set<String>> edges = new LinkedHashMap<>();

        for (IModule module : modules.values()) {
            indegree.put(module.getId(), 0);
            edges.put(module.getId(), new LinkedHashSet<>());
        }

        for (IModule module : modules.values()) {
            for (String dependency : this.safeDependencies(module)) {
                if (!modules.containsKey(dependency)) {
                    continue;
                }

                edges.get(dependency).add(module.getId());
                indegree.computeIfPresent(module.getId(), (k, current) -> current + 1);
            }
        }

        Deque<String> queue = new ArrayDeque<>();
        for (Map.Entry<String, Integer> entry : indegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.addLast(entry.getKey());
            }
        }

        List<IModule> ordered = new ArrayList<>(modules.size());

        while (!queue.isEmpty()) {
            String id = queue.removeFirst();
            ordered.add(modules.get(id));

            for (String dependent : edges.get(id)) {
                Integer current = indegree.get(dependent);
                int next = current - 1;
                indegree.put(dependent, next);
                if (next == 0) {
                    queue.addLast(dependent);
                }
            }
        }

        if (ordered.size() != modules.size()) {
            throw new IllegalStateException("Cyclic module dependency detected");
        }

        return ordered;
    }

    /**
     * Returns the dependency ids declared by the given module after validating them.
     *
     * @param module the module whose dependencies are being read
     * @return the validated dependency collection
     * @throws NullPointerException     if the dependency collection is null
     * @throws IllegalArgumentException if any dependency id is null or blank
     */
    private Collection<String> safeDependencies(IModule module) {
        Collection<String> dependencies =
                Objects.requireNonNull(
                        module.getDependencies(), "dependencies cannot be null for module " + module.getId());

        for (String dependency : dependencies) {
            if (dependency == null || dependency.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Module " + module.getId() + " has a null/blank dependency id");
            }
        }

        return dependencies;
    }

    /**
     * Invokes a module unload callback and logs any failure.
     *
     * @param name       the logical name used in failure logging
     * @param unloadable the unloadable module
     */
    private void safeUnload(String name, IUnloadable unloadable) {
        try {
            unloadable.unload();
        } catch (Throwable throwable) {
            this.logger.log(Level.SEVERE, "Unload failed for " + name, throwable);
        }
    }

    /**
     * Ensures that this registry is still active.
     *
     * @throws IllegalStateException if the registry has already been shut down
     */
    private void ensureActive() {
        if (this.shutdown) {
            throw new IllegalStateException("KStaff has already been shut down.");
        }
    }
}
