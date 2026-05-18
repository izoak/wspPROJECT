package research;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

public class ResearchPaper implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String paperId;
    private String title;
    private List<String> authors;   // author names / IDs
    private int citations;
    private int pages;
    private LocalDate publicationDate;
    private String journal;

    public ResearchPaper(String paperId, String title, String journal, LocalDate publicationDate) {
        this.paperId = paperId;
        this.title = title;
        this.journal = journal;
        this.publicationDate = publicationDate;
        this.authors = new ArrayList<>();
        this.citations = 0;
        this.pages = 0;
    }

    public void addAuthor(String authorName) {
        if (!authors.contains(authorName)) {
            authors.add(authorName);
        }
    }

    public int getCitation() {
        return citations;
    }

    public String getPaperId(){ 
        return paperId; 
    }
    public String getTitle(){ 
        return title; 
    }
    public void setTitle(String t){ 
        this.title = t;
    }

    public List<String> getAuthors(){ 
        return List.copyOf(authors); 
    }

    public int  getCitations(){ 
        return citations; 
    }
    public void setCitations(int c) {
        if (c < 0) throw new IllegalArgumentException("Citations cannot be negative");
        this.citations = c;
    }
    public void incrementCitations(){ 
        this.citations++; 
    }

    public int  getPages(){ 
        return pages; 
    }
    public void setPages(int p) {
        if (p <= 0) throw new IllegalArgumentException("Pages must be positive");
        this.pages = p;
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }
    public void setPublicationDate(LocalDate date) {
        this.publicationDate = date;
    }

    public String getJournal(){ 
        return journal; 
    }
    public void   setJournal(String j){ 
        this.journal = j; 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResearchPaper p)) return false;
        return paperId.equals(p.paperId);
    }

    @Override
    public int hashCode() { 
        return Objects.hash(paperId); 
    }

    @Override
    public String toString() {
        return String.format(
            "ResearchPaper[id=%s, title='%s', authors=%s, journal='%s', date=%s, citations=%d, pages=%d]",
            paperId, title, authors, journal, publicationDate, citations, pages
        );
    }
}