package research;

import java.util.Comparator;

public class PaperByDateComparator implements Comparator<ResearchPaper> {

    @Override
    public int compare(ResearchPaper a, ResearchPaper b) {
        int cmp = b.getPublicationDate().compareTo(a.getPublicationDate()); // descending
        if (cmp != 0) return cmp;
        return a.getTitle().compareToIgnoreCase(b.getTitle());
    }
}