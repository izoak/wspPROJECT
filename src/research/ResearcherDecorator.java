package research;

import exceptions.LowHIndexException;
import exceptions.NotResearcherException;
import models.User;

import java.util.*;


public abstract class ResearcherDecorator extends User {
    private static final long serialVersionUID = 1L;

    public static final int MIN_SUPERVISOR_H_INDEX = 3;

    protected final User wrappedUser;

    private int hIndex;
    private final List<ResearchPaper>   papers;
    private final List<ResearchProject> projects;
    protected ResearcherDecorator(User wrappedUser) {
        super(
            wrappedUser.getId(),
            wrappedUser.getFirstName(),
            wrappedUser.getLastName(),
            wrappedUser.getUsername(),
            wrappedUser.getPasswordHash()
        );
        this.wrappedUser = wrappedUser;
        this.hIndex  = 0;
        this.papers  = new ArrayList<>();
        this.projects = new ArrayList<>();
    }

    @Override
    public String getRole() {
        return wrappedUser.getRole() + "+Researcher";
    }

    @Override
    public String getFullName() {
        return wrappedUser.getFullName();
    }

    public void addPaper(ResearchPaper paper) {
        if (!papers.contains(paper)) {
            papers.add(paper);
            recalculateHIndex();
        }
    }

    public boolean removePaper(String paperId) {
        boolean removed = papers.removeIf(p -> p.getPaperId().equals(paperId));
        if (removed) recalculateHIndex();
        return removed;
    }

    public void joinProject(ResearchProject project) throws NotResearcherException {
        if (!projects.contains(project)) {
            projects.add(project);
            project.addParticipant(this);
        }
    }

        
    public int calculateHIndex() {
        List<Integer> sorted = papers.stream().map(ResearchPaper::getCitations)
            .sorted(Comparator.reverseOrder())
            .toList();

        int h = 0;
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i) >= i + 1) {
                h = i + 1;
            } else {
                break;
            }
        }
        this.hIndex = h;
        return h;
    }

    public void printPapers(Comparator<ResearchPaper> comparator) {
        if (papers.isEmpty()) {
            System.out.printf("[%s] No papers published yet.%n", getFullName());
            return;
        }
        List<ResearchPaper> sorted = new ArrayList<>(papers);
        sorted.sort(comparator);

        System.out.printf("── Papers by %s (h-index: %d) ──────────────────────%n",
            getFullName(), hIndex);
        for (int i = 0; i < sorted.size(); i++) {
            ResearchPaper p = sorted.get(i);
            System.out.printf("  %2d. [%s] '%s' | %s | cit: %d | pages: %d%n",
                i + 1,
                p.getPublicationDate(),
                p.getTitle(),
                p.getJournal(),
                p.getCitations(),
                p.getPages()
            );
        }
    }

    public void printPapers() {
        printPapers(new PaperByCitationsComparator());
    }

    public void validateSupervisorEligibility() throws LowHIndexException {
        recalculateHIndex();
        if (hIndex < MIN_SUPERVISOR_H_INDEX) {
            throw new LowHIndexException(getFullName(), hIndex, MIN_SUPERVISOR_H_INDEX);
        }
    }

    public static void printTopResearchers(List<ResearcherDecorator> researchers, int topN) {
        if (researchers == null || researchers.isEmpty()) {
            System.out.println("[TopResearchers] No researchers found.");
            return;
        }
        researchers.forEach(ResearcherDecorator::recalculateHIndex);
        List<ResearcherDecorator> sorted = new ArrayList<>(researchers);
        sorted.sort(Comparator.comparingInt(ResearcherDecorator::getHIndex).reversed());

        int limit = Math.min(topN, sorted.size());
        System.out.printf("── Top %d Researchers ───────────────────────────────%n", limit);
        for (int i = 0; i < limit; i++) {
            ResearcherDecorator r = sorted.get(i);
            System.out.printf("  %2d. %-25s h-index: %d  papers: %d%n",
                i + 1, r.getFullName(), r.getHIndex(), r.getPapers().size());
        }
    }

    private void recalculateHIndex() {
        calculateHIndex();
    }

    public int getHIndex(){ 
        return hIndex; 
    }
    public List<ResearchPaper>   getPapers(){ 
        return List.copyOf(papers); 
    }
    public List<ResearchProject> getProjects()  { 
        return List.copyOf(projects); 
    }
    public User getWrappedUser(){ 
        return wrappedUser; 
    }

    @Override
    public String toString() {
        return String.format(
            "ResearcherDecorator[user=%s, role=%s, hIndex=%d, papers=%d, projects=%d]",
            getFullName(), getRole(), hIndex, papers.size(), projects.size()
        );
    }
}