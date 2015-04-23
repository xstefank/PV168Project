/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agency;

import java.util.List;

/**
 *
 * @author martin
 */
public interface AgencyManager {

    boolean assignAgentToMission(Agent agent, Mission mission);      //changed due to UML

    List<Agent> findAgentsOnMission(Mission mission);

    List<Agent> findAvailableAgents();

    List<Agent> findAvailableAgentsForMission(Mission mission);

    Mission findMissionWithAgent(Agent agent);

    void withdrawAgentFromMission(Agent agent, Mission mission);
    
}
