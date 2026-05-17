package exceptions;

public class LowHIndexException extends Exception {

    private final String researcherName;
    private final int actualHIndex;
    private final int requiredHIndex;

    public LowHIndexException(String researcherName, int actualHIndex, int requiredHIndex) {
        super(String.format(
            "Researcher '%s' has h-index %d, but minimum required is %d.",
            researcherName, actualHIndex, requiredHIndex
        ));
        this.actualHIndex = actualHIndex;
        this.requiredHIndex = requiredHIndex;
        this.researcherName = researcherName;
    }

    public int getActualHIndex() {
        return actualHIndex;
    }

    public int getRequiredHIndex() {
        return requiredHIndex;
    }

    public String getResearcherName(){
        return researcherName;
    }
}