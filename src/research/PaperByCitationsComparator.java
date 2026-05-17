package research;
import java.util.Comparator;
public class PaperByCitationsComparator implements Comparator<ResearchPaper> {

    @Override
    public int compare(ResearchPaper a, ResearchPaper b) {
        int cmp = Integer.compare(b.getCitations(), a.getCitations()); // descending
        if (cmp != 0) return cmp;
        return a.getTitle().compareToIgnoreCase(b.getTitle());
    }
}