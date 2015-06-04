/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agency;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;
import javax.sql.DataSource;
import org.apache.log4j.Logger;




/**
 *
 * @author martin
 */
public class AgentManagerImpl implements AgentManager {

    //"jdbc:derby://localhost:1527/AgencyDB"
    //"jdbc:derby:memory:Agency"
//    private final String databaseURL = "jdbc:derby:memory:Agency";
    
    private DataSource dataSource;
    
    private static final Logger log = Logger.getLogger(AgentManagerImpl.class);

    public AgentManagerImpl() {}
    
    public AgentManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void createAgent(Agent agent) {
   
        log.debug("createAgent called");
        
        if (agent == null) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("nullAgent"));
        }

        if (agent.getId() != null) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("agentAlreadyInDB"));
        }

        validateAgentsAttributes(agent);

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("INSERT INTO APP.Agents (\"name\",born,level,note) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {

                st.setString(1, agent.getName());
                st.setDate(2, new java.sql.Date(agent.getBorn().getTime()));
                st.setInt(3, agent.getLevel());
                st.setString(4, agent.getNote());

                int addedRows = st.executeUpdate();

                if (addedRows != 1) {
                    throw new InternalError("Internal Error: More rows inserted when trying to insert agent: " + agent);
                }

                ResultSet keyRS = st.getGeneratedKeys();
                agent.setId(getKey(keyRS, agent));
                agent.setMissionId(Long.valueOf(0));
            }
        } catch (SQLException ex) {
            log.error("DB connection problem", ex);
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("createAgentDB"), ex);
        }

    }

    private Long getKey(ResultSet keyRS, Agent agent) throws SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new InternalError("Internal Error: Generated key"
                        + "retriving failed when trying to insert grave " + agent
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new InternalError("Internal Error: Generated key"
                        + "retriving failed when trying to insert grave " + agent
                        + " - more keys found");
            }
            return result;
        } else {
            throw new InternalError("Internal Error: Generated key"
                    + "retriving failed when trying to insert grave " + agent
                    + " - no key found");
        }
    }

    @Override
    public void updateAgent(Agent agent) {

        log.debug("updateAgent called");
        
        if (agent == null) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("nullAgent"));
        }

        if (agent.getId() == null) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("agentNotInDB"));
        }

        if (agent.getName() == null) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("agentNullName"));
        }

        validateAgentsAttributes(agent);

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("UPDATE APP.Agents SET \"name\"=?,BORN=?,LEVEL=?,NOTE=? WHERE id=?")) {

                st.setString(1, agent.getName());
                st.setDate(2, new java.sql.Date(agent.getBorn().getTime()));
                st.setInt(3, agent.getLevel());
                st.setString(4, agent.getNote());
                st.setLong(5, agent.getId());

                if (st.executeUpdate() != 1) {
                    throw new IllegalArgumentException("cannot update agent: " + agent);
                }
            }
        } catch (SQLException ex) {
            log.error("DB connection problem", ex);
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("updateAgentDB"), ex);
        }

    }

    @Override
    public void deleteAgent(Agent agent) {

        log.debug("deleteAgent called");
        
        if (agent == null) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("nullAgent"));
        }

        if (agent.getId() == null) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("agentNotInDB"));
        }

        if (agent.getId() <= 0) {
            throw new IllegalArgumentException("cannot delete agent with zero or negative id");
        }

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("DELETE FROM APP.Agents WHERE id=?")) {

                st.setLong(1, agent.getId());

                if (st.executeUpdate() != 1) {
                    throw new InternalError("Internal Error: cannot delete agent: " + agent);
                }
            }
        } catch (SQLException ex) {
            log.error("DB connection problem", ex);
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("deleteAgentDB"), ex);
        }
        
    }

    @Override
    public Agent findAgentById(Long agentId) {

        log.debug("findAgentById called");
        
        if (agentId == null) {
            throw new IllegalArgumentException("cannot find an agent with a null id");
        }

        if (agentId <= 0) {
            throw new IllegalArgumentException("cannot find agent with zero or negative id");
        }

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,\"name\",born,level,note,missionId FROM APP.Agents WHERE id=?")) {

                st.setLong(1, agentId);
                ResultSet rs = st.executeQuery();
                
                if(rs.next()) {
                    Agent agent = resultSetToAgent(rs);
                    if(rs.next()) {
                        throw new InternalError("Internal error: More entities with the same id found " 
                                   + "(source id: " + agentId + ", found " + agent + " and " + resultSetToAgent(rs));
                    }
                    return agent;
                }
                
                return null;

            }
        } catch (SQLException ex) {
            log.error("DB connection problem", ex);
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("findAgentByIdDB"), ex);
        }
    }
    
    private Agent resultSetToAgent(ResultSet rs) throws SQLException {
        Agent agent = new Agent();
        
        agent.setId(rs.getLong("id"));
        agent.setName(rs.getString("name"));
        agent.setBorn(rs.getDate("born"));
        agent.setLevel(rs.getInt("level"));
        agent.setNote(rs.getString("note"));
        agent.setMissionId(rs.getLong("missionId"));
        
        return agent;
    }

    @Override
    public List<Agent> findAllAgents() {
        log.debug("findAllAgents called");
        
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,\"name\",born,level,note,missionId FROM APP.Agents")) {

               ResultSet rs = st.executeQuery();
               
               List<Agent> agents = new ArrayList<>();
               
               while(rs.next()) {
                   agents.add(resultSetToAgent(rs));
               }
               
               return agents;

            }
        } catch (SQLException ex) {
            log.error("DB connection problem", ex);
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("findAllAgentsDB"), ex);
        }
    }

    private void validateAgentsAttributes(Agent agent) {
        if (agent.getName() == null) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("agentNullName"));
        }
        
        if(agent.getName().equals("")) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("agentNullName"));
        }

        if (agent.getBorn() == null) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("agentNullBorn"));
        }

        if (agent.getBorn().after(Calendar.getInstance().getTime())) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("agentFutureBorn"));
        }

        Calendar less18 = Calendar.getInstance();
        less18.add(Calendar.YEAR, -18);
        less18.add(Calendar.SECOND, -30);
        if (agent.getBorn().after(less18.getTime()) || agent.getBorn().equals(less18.getTime())) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("agentTooYoung"));
        }

        if (agent.getLevel() <= 0) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("agentTooLowLevel"));
        }

        if (agent.getLevel() > 10) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("agentTooHighLevel"));
        }
    }

}
