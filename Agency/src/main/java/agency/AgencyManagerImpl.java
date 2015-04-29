/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agency;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.util.ArrayList;
import org.apache.log4j.Logger;


/**
 *
 * @author martin
 */
public class AgencyManagerImpl implements AgencyManager {

    private Logger log = org.apache.log4j.Logger.getLogger(Agency.class);

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }    

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }
    
    @Override
    public Mission findMissionWithAgent(Agent agent) {
        
        checkDataSource();        
        
        if (agent == null) {
            throw new IllegalArgumentException("agent is null");
        }        
        
        if (agent.getId() == null) {
            throw new IllegalArgumentException("agent id is null");
        }        
        
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement(
                                             "SELECT APP.Missions.id, APP.Missions.\"name\", beginDate, endDate, capacity, difficulty, APP.Missions.note " +
                                             "FROM APP.Missions JOIN APP.Agents ON APP.Missions.id = APP.Agents.missionId " +
                                             "WHERE APP.Agents.id = ?")) {
                                                 
                st.setLong(1, agent.getId());
                ResultSet rs = st.executeQuery();
              
                if(!rs.next()) {
                    return null;
                }
                
                return resultSetToMission(rs);
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new IllegalStateException("Error when finding mission with agent " + agent, ex);
        }
    }
    
    @Override
    public List<Agent> findAgentsOnMission(Mission mission) {
        
        if (mission == null) {
            throw new IllegalArgumentException("agent is null");
        }        
        
        if (mission.getId() == null) {
            throw new IllegalArgumentException("agent id is null");
        } 
        
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement(
                                            "SELECT APP.Agents.id,APP.Agents.\"name\",born,level,APP.Agents.note " + 
                                            "FROM APP.Missions JOIN APP.Agents ON APP.Missions.id = APP.Agents.missionId " +
                                            "WHERE APP.Missions.id = ?")) {
                st.setLong(1, mission.getId());
                ResultSet rs = st.executeQuery();
                List<Agent> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(resultSetToAgent(rs));
                }
                return result;
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new IllegalStateException("Error when finding agents on mission " + mission, ex);
        }
    }
    
    @Override
    public boolean assignAgentToMission(Agent agent, Mission mission) {
        
        if (agent == null) {
            throw new IllegalArgumentException("agent is null");
        }        
        
        if (agent.getId() == null) {
            throw new IllegalArgumentException("agent id is null");
        } 
        
        if (mission == null) {
            throw new IllegalArgumentException("agent is null");
        }        
        
        if (mission.getId() == null) {
            throw new IllegalArgumentException("agent id is null");
        } 
        
        if (mission.getCapacity() <= findAgentsOnMission(mission).size()){
            return false;
        }
        
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement(
                                            "UPDATE APP.Agents SET APP.Agents.missionId = ? WHERE APP.Agents.id = ? AND APP.Agents.missionId IS NULL AND level >= ?")) {
                
                st.setLong(1, mission.getId());
                st.setLong(2, agent.getId());
                st.setInt(3, mission.getDifficulty());
                
                int res = st.executeUpdate();
                if(res != 1) {
                    return false;
                }
                
                return true;
                                                
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new IllegalStateException("Error when assigning agent " + agent + " to mission " + mission, ex);
        }
    }
    
    @Override
    public void withdrawAgentFromMission(Agent agent, Mission mission) {
        
        if (agent == null) {
            throw new IllegalArgumentException("agent is null");
        }        
        
        if (agent.getId() == null) {
            throw new IllegalArgumentException("agent id is null");
        } 
        
        if (mission == null) {
            throw new IllegalArgumentException("agent is null");
        }        
        
        if (mission.getId() == null) {
            throw new IllegalArgumentException("agent id is null");
        } 
        
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement(
                                            "UPDATE APP.Agents SET APP.Agents.missionId = NULL WHERE APP.Agents.id = ? AND APP.Agents.missionId = ?")) {
                
                st.setLong(1, agent.getId());
                st.setLong(2, mission.getId());
                
                int res = st.executeUpdate();
                if(res != 1) {
                    throw new IllegalArgumentException("Error removing agent from mission");
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new IllegalStateException("Error when withdrawing agnet " + agent + " from mission " + mission, ex);
        }
    }
    
    @Override
    public List<Agent> findAvailableAgents() {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement(
                                            "SELECT id,\"name\",born,level,note " + 
                                            "FROM APP.Agents " +
                                            "WHERE missionId IS NULL")) {

                ResultSet rs = st.executeQuery();
                List<Agent> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(resultSetToAgent(rs));
                }

                return result;
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new IllegalStateException("Error when retrieving all available agents", ex);
        }
    }
    
    @Override
    public List<Agent> findAvailableAgentsForMission(Mission mission) {
        
        if (mission == null) {
            throw new IllegalArgumentException("agent is null");
        }        
        
        if (mission.getId() == null) {
            throw new IllegalArgumentException("agent id is null");
        } 
        
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement(
                                            "SELECT id,\"name\",born,level,note " + 
                                            "FROM APP.Agents " +
                                            "WHERE APP.Agents.level >= ? AND APP.Agents.missionId IS NULL")) {

                st.setInt(1, mission.getDifficulty());
                ResultSet rs = st.executeQuery();
                List<Agent> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(resultSetToAgent(rs));
                }
                return result;
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new IllegalStateException("Error when retrieving available agents for mission" + mission, ex);
        }
    }
    
    private Agent resultSetToAgent(ResultSet rs) throws SQLException {
        Agent agent = new Agent();
        
        agent.setId(rs.getLong("id"));
        agent.setName(rs.getString("name"));
        agent.setBorn(rs.getDate("born"));
        agent.setLevel(rs.getInt("level"));
        agent.setNote(rs.getString("note"));
        
        return agent;
    }
    
    private Mission resultSetToMission(ResultSet rs) throws SQLException {
        Mission mission = new Mission();
        mission.setId(rs.getLong("id"));
        mission.setName(rs.getString("name"));
        mission.setBeginDate(rs.getDate("beginDate"));
        mission.setEndDate(rs.getDate("endDate"));
        mission.setCapacity(rs.getInt("capacity"));
        mission.setDifficulty(rs.getInt("difficulty"));
        mission.setNote(rs.getString("note"));
        return mission;
    }
}
