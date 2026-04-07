package com.placement.portal.exception;

/**
 * Thrown when a requested entity cannot be found in the data store.
 *
 * <p>Handled by {@link GlobalExceptionHandler} which maps it to HTTP 404.</p>
 */
public class EntityNotFoundException extends RuntimeException {

    /**
     * Creates a new exception for a missing entity.
     *
     * @param entityName the simple class name of the entity (e.g. {@code "User"})
     * @param id         the identifier that was looked up
     */
    public EntityNotFoundException(String entityName, String id) {
        super(entityName + " not found with id: " + id);
    }

    /**
     * Creates a new exception with a fully custom message.
     *
     * @param message the detail message
     */
    public EntityNotFoundException(String message) {
        super(message);
    }
}
