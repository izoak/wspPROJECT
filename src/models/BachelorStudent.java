package models;

public class BachelorStudent extends Student {
    private static final long serialVersionUID = 1L;

    private String researchSupervisorName;

    public BachelorStudent(String id, String firstName, String lastName, String username, String passwordHash,
                           String major, int yearOfStudy) {
        super(id, firstName, lastName, username, passwordHash, major, yearOfStudy);
    }

    @Override
    public String getRole() {
        return "BACHELOR_STUDENT";
    }

    public String getResearchSupervisorName() {
        return researchSupervisorName;
    }

    public void setResearchSupervisorName(String researchSupervisorName) {
        this.researchSupervisorName = researchSupervisorName;
    }
}
