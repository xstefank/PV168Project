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
public interface AgentManager {

    void createAgent(Agent agent);

    void deleteAgent(Agent agent);

    Agent findAgentById(Long agentId);

    List<Agent> findAllAgents();

    void updateAgent(Agent agent);
    
}
