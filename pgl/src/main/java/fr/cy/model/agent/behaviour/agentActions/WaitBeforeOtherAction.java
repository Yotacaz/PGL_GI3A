package fr.cy.model.agent.behaviour.agentActions;

import fr.cy.model.agent.Agent;
import fr.cy.model.agent.AgentSettings;

public class WaitBeforeOtherAction extends WaitAction {
    private static final long serialVersionUID = 1L;

    private final AgentAction nextAction;
    public WaitBeforeOtherAction(Agent agent, double totalTimeToWait, AgentAction nextAction) {
        super(agent, totalTimeToWait);
        this.nextAction = nextAction;
    }
 
    @Override
    public double perform(AgentSettings agentSettings, double availableTime) {
        double timeWaitedThisTick = super.perform(agentSettings, availableTime);
        if (isCompleted() && nextAction != null) {
            getAgent().setCurrentAction(nextAction);
        }
        return timeWaitedThisTick;
    }

    @Override
    public boolean isCompleted() {
        return super.isCompleted() && (nextAction == null || nextAction.isCompleted());
    }

}
