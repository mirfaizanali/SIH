package com.placement.portal.service.recommendation;

/**
 * Immutable value object holding the composite match score between a student
 * and a job (or vice-versa).
 *
 * <p>All component scores are expressed on a 0–100 scale. The {@code totalScore}
 * is the weighted sum of those components.</p>
 *
 * @param entityId         the ID of the entity being scored (job ID or student profile ID)
 * @param totalScore       weighted total (0–100)
 * @param skillScore       skill-match component (0–100)
 * @param cgpaScore        CGPA component (0–100, or 0.0 as a hard disqualifier)
 * @param experienceScore  experience-level fit component (0–100)
 * @param preferenceScore  location/job-type preference component (0, 40, 60, or 100)
 * @param recencyScore     deadline recency component (20, 40, 70, or 100)
 */
public record MatchScore(
        String entityId,
        double totalScore,
        double skillScore,
        double cgpaScore,
        double experienceScore,
        double preferenceScore,
        double recencyScore
) {}
