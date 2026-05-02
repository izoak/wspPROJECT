package university.models.users;

import university.enums.Gender;
import university.exceptions.LowHIndexException;
import university.models.research.Researcher;

public class BachelorStudent extends Student {

    private static final long serialVersionUID = 1L;
    private static final int SUPERVISOR_MIN_H_INDEX = 3;

    private Researcher researchSupervisor;

    public BachelorStudent(String name, String email, String password, Gender gender,
                           String studentId, int yearOfStudy, String major) {
        super(name, email, password, gender, studentId, yearOfStudy, major);
    }

    public void setResearchSupervisor(Researcher supervisor)
            throws LowHIndexException {
        if (getYearOfStudy() != 4) {
            throw new IllegalStateException(
                    "Only 4th-year bachelor students can have a research supervisor.");
        }
        if (supervisor.getHIndex() < SUPERVISOR_MIN_H_INDEX) {
            String name = supervisor.toString();
            throw new LowHIndexException(name, supervisor.getHIndex());
        }
        this.researchSupervisor = supervisor;
        System.out.printf("[BACHELOR] Research supervisor assigned to %s%n", getName());
    }

    public Researcher getResearchSupervisor() {
        return researchSupervisor;
    }

    @Override
    public String toString() {
        return String.format("BachelorStudent{name='%s', year=%d, major='%s', gpa=%.2f, supervisor=%s}",
                getName(), getYearOfStudy(), getMajor(), getGpa(),
                researchSupervisor != null ? "assigned" : "none");
    }
}