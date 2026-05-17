package research;

import models.User;

 
public class ConcreteResearcher extends ResearcherDecorator {
    private static final long serialVersionUID = 1L;

    public ConcreteResearcher(User user) {
        super(user);
    }

    @Override
    public String getRole() {
        return wrappedUser.getRole() + "+Researcher";
    }
}