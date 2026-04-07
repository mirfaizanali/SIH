package com.placement.portal.util;

/**
 * Application-wide constants shared across service and validation layers.
 *
 * <p>Declared as an interface so that implementing classes automatically inherit
 * the constants without any instantiation overhead.</p>
 */
public interface AppConstants {

    // -----------------------------------------------------------------------
    // File upload
    // -----------------------------------------------------------------------

    /**
     * MIME types accepted for résumé uploads.
     */
    String[] ALLOWED_RESUME_TYPES = {
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    };

    /**
     * Maximum résumé file size in bytes (10 MB).
     */
    long MAX_RESUME_FILE_SIZE = 10 * 1024 * 1024L;

    // -----------------------------------------------------------------------
    // Pagination
    // -----------------------------------------------------------------------

    /**
     * Default number of items returned per page when no {@code size} query
     * parameter is supplied.
     */
    int DEFAULT_PAGE_SIZE = 10;

    /**
     * Absolute maximum page size that clients may request.
     */
    int MAX_PAGE_SIZE = 100;
}
