package exceptions;

/**
 * Thrown when a researcher's h-index is too low for a requested operation.
 *
 * <p>The most common use-case is validating supervisor eligibility: a researcher
 * must have an h-index of at least
 * {@link research.ResearcherDecorator#MIN_SUPERVISOR_H_INDEX} before they can
 * be assigned as a thesis or project supervisor.
 *
 * <p>This is a <em>checked</em> exception, so callers of methods that throw it
 * must either handle it or declare it in their own {@code throws} clause.
 *
 * <p><strong>Example:</strong>
 * <pre>{@code
 * try {
 *     researcher.validateSupervisorEligibility();
 * } catch (LowHIndexException e) {
 *     System.out.println(e.getMessage());
 *     // "Researcher 'Alice' has h-index 1, but minimum required is 3."
 * }
 * }</pre>
 *
 * @author Gotei 4
 * @version 1.0
 * @see research.ResearcherDecorator#validateSupervisorEligibility()
 */
public class LowHIndexException extends Exception {

    /** Full name of the researcher who failed the check. */
    private final String researcherName;

    /** The researcher's h-index at the time the exception was thrown. */
    private final int actualHIndex;

    /** The minimum h-index that was required for the attempted operation. */
    private final int requiredHIndex;

    /**
     * Constructs a {@code LowHIndexException} with a formatted detail message.
     *
     * <p>The generated message has the form:
     * <pre>
     *   Researcher 'Alice' has h-index 1, but minimum required is 3.
     * </pre>
     *
     * @param researcherName full name of the researcher (used in the message)
     * @param actualHIndex   the researcher's current h-index
     * @param requiredHIndex the minimum h-index required by the operation
     */
    public LowHIndexException(String researcherName, int actualHIndex, int requiredHIndex) {
        super(String.format(
            "Researcher '%s' has h-index %d, but minimum required is %d.",
            researcherName, actualHIndex, requiredHIndex
        ));
        this.researcherName = researcherName;
        this.actualHIndex   = actualHIndex;
        this.requiredHIndex = requiredHIndex;
    }

    /**
     * Returns the full name of the researcher who triggered the exception.
     *
     * @return researcher name (never {@code null})
     */
    public String getResearcherName() { 
        return researcherName; 
    }

    /**
     * Returns the researcher's actual h-index at the time of the check.
     *
     * @return actual h-index (&ge; 0)
     */
    public int getActualHIndex() { 
        return actualHIndex; 
    }

    /**
     * Returns the minimum h-index that was required for the attempted operation.
     *
     * @return required h-index (&gt; 0)
     */
    public int getRequiredHIndex() { 
        return requiredHIndex; 
    }
}