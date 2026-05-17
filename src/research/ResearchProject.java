package research;

import exceptions.NotResearcherException;

import java.io.Serializable;
import java.util.*;


public class ResearchProject implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String projectId;
    private String topic;
    private final List<ResearchPaper> papers;
    private final List<String> participantIds; 

    public ResearchProject(String projectId, String topic) {
        this.projectId = projectId;
        this.topic = topic;
        this.papers = new ArrayList<>();
        this.participantIds = new ArrayList<>();
    }
    public void addParticipant(ResearcherDecorator researcher) throws NotResearcherException {
        if (researcher == null) {
            throw new NotResearcherException("null");
        }
        String uid = researcher.getId();
        if (!participantIds.contains(uid)) {
            participantIds.add(uid);
            System.out.printf("[ResearchProject] '%s' joined project '%s'.%n",
                researcher.getFullName(), topic);
        }
    }

    public void addParticipant(String researcherUserId) {
        if (!participantIds.contains(researcherUserId)) {
            participantIds.add(researcherUserId);
        }
    }

    public boolean removeParticipant(String researcherUserId) {
        return participantIds.remove(researcherUserId);
    }

    public void addPaper(ResearchPaper paper) {
        if (!papers.contains(paper)) {
            papers.add(paper);
        }
    }

    public boolean removePaper(String paperId) {
        return papers.removeIf(p -> p.getPaperId().equals(paperId));
    }
    public String getProjectId(){
        return projectId; 
    }
    public String getTopic(){ 
        return topic; 
    }
    public void setTopic(String t){
         this.topic = t; 
    }

    @Override
    public String toString() {
        return String.format(
            "ResearchProject[id=%s, topic='%s', papers=%d, participants=%s]",
            projectId, topic, papers.size(), participantIds
        );
    }
}