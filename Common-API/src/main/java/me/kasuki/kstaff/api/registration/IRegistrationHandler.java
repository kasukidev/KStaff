package me.kasuki.kstaff.api.registration;

/**
 * Defines a contract for components responsible for performing object registration.
 *
 * <p>Implementations are expected to register modules, services, or other application components
 * into a central registry (e.g., an API container).
 *
 * <p>This interface abstracts the registration phase from the application bootstrap process,
 * allowing registration logic to be modularized and invoked in a controlled lifecycle stage.
 *
 * <p>Implementations should:
 *
 * <ul>
 *   <li>Be idempotent where possible (avoid duplicate registrations).
 *   <li>Fail fast if required dependencies are missing.
 *   <li>Avoid performing heavy initialization logic unrelated to registration.
 * </ul>
 */
public interface IRegistrationHandler {

  /**
   * Executes registration logic.
   *
   * <p>This method should register all required objects into the target registry. Implementations
   * may throw runtime exceptions if registration fails.
   */
  void registerObjects();
}
