/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agency;

import common.DBUtils;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.Logger;

import org.apache.derby.jdbc.EmbeddedDriver;

/**
 *
 * @author martin
 */
public class Agency {

    private static DataSource dataSource;
    private static DataSource prepareDataSource() throws SQLException, IOException {
        Properties myconf = new Properties();
        myconf.load(Agency.class.getResourceAsStream("/myconf.properties"));

        BasicDataSource ds = new BasicDataSource();
        
        ds.setUrl(myconf.getProperty("jdbc.url"));
        ds.setUsername(myconf.getProperty("jdbc.user"));
        ds.setPassword(myconf.getProperty("jdbc.password"));

        return ds;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException, IOException {
        // TODO code application logic here
        
        Logger log = Logger.getLogger(Agency.class);
        log.debug("LOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOG");
        
        System.out.println("Agency main method");  
        
        SpringConfig sc = new SpringConfig();
        dataSource = sc.dataSource();//prepareDataSource();
//        DBUtils.executeSqlScript(dataSource, Agency.class.getResource("/agencyDBD.sql"));
//        DBUtils.executeSqlScript(dataSource, Agency.class.getResource("/test-data.sql"));
        
        AgentManagerImpl manager = new AgentManagerImpl();
        manager.setDataSource(dataSource);
        
        System.out.println("AGENTI:");
        List<Agent> result = manager.findAllAgents();
        for(Agent agent : result) {
            System.out.println(agent);
        }
        
        /*System.out.println("Script executed");
        //--------------------------
        Agent agent = new Agent();
        agent.setName("Hawkeye");
        agent.setBorn(new GregorianCalendar(1980, Calendar.AUGUST, 8).getTime());
        agent.setLevel(4);
        agent.setNote("The archer");
        
        AgentManagerImpl manager = new AgentManagerImpl();
        manager.setDataSource(dataSource);
        
        
        manager.createAgent(agent);
        
        manager.deleteAgent(agent);
//        manager.deleteAgent(agent);
        */
        
       /*Mission mission = new Mission();
        
        mission.setBeginDate(new GregorianCalendar(2001, 7, 15).getTime());
        mission.setEndDate(new GregorianCalendar(2001, 7, 16).getTime());
        mission.setCapacity(5);
        mission.setDifficulty(5);
        mission.setNote("test mission");
        
        MissionManagerImpl manager = new MissionManagerImpl();
        
        manager.createMission(mission);
        
        manager.deleteMission(mission);
        manager.deleteMission(mission);*/
        
        
       /*try (Connection conn = DriverManager.getConnection("jdbc:derby://localhost:1527/AgencyDB", "agent", "agent")) {
            try (PreparedStatement st = conn.prepareStatement("DELETE FROM APP.Agents WHERE id=?",
                    Statement.RETURN_GENERATED_KEYS)) {

                st.setLong(1, agent.getId());
               st.executeUpdate();
            }
        } catch (SQLException ex) {
            //log.error("DB connection problem", ex);
            throw new IllegalStateException("Error connecting to the DB", ex);
        }*/
       
    }
    
}
