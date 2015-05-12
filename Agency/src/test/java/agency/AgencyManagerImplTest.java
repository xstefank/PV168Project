/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agency;

import common.DBUtils;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author martin
 */
public class AgencyManagerImplTest {

    private AgencyManagerImpl manager;
    private AgentManagerImpl agentManager;
    private MissionManagerImpl missionManager;
    private DataSource dataSource;

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
     * @throws SQLException
     * @throws java.io.IOException
     */
    @Before
    public void setUp() throws SQLException, IOException {
        dataSource = prepareDataSource();

        DBUtils.executeSqlScript(dataSource, Agency.class.getResource("/agencyDBD.sql"));

        manager = new AgencyManagerImpl();
        manager.setDataSource(dataSource);
        agentManager = new AgentManagerImpl();
        agentManager.setDataSource(dataSource);
        missionManager = new MissionManagerImpl();
        missionManager.setDataSource(dataSource);

    }

    /**
     * Test of findMissionWithAgent method, of class AgencyManagerImpl.
     */
    @Test
    public void testFindMissionWithAgent() {
        System.out.println("testFindMissionWithAgent");

        Mission result;

        //create agents
        Agent agent = newAgent01();
        Agent agent02 = newAgent02();

        //check whether there is mission for agent which is not in the DB
        try {
            manager.findMissionWithAgent(agent);
            fail("Found mission for agent which is not in the DB");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        //add agents to the DB
        agentManager.createAgent(agent);
        agentManager.createAgent(agent02);

        //test null value
        try {
            manager.findMissionWithAgent(null);
            fail("Found mission for a null agent");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        //this should find nothing
        result = manager.findMissionWithAgent(agent);
        assertNull("Found mission for agent even if there are no missions started", result);

        //add new mission
        Mission mission = newMission01();
        missionManager.createMission(mission);

        //test whether other agents on mission affect agents without mission
        manager.assignAgentToMission(agent, mission);
        result = manager.findMissionWithAgent(agent02);
        assertNull("Found mission for agent even if the agent is not on any mission", result);

        //valid insert and find
        agent02.setLevel(4);
        agentManager.updateAgent(agent02);
        manager.assignAgentToMission(agent02, mission);
        result = manager.findMissionWithAgent(agent);
        assertEquals("Invalid mission returned", mission, result);
        assertNotSame("Valid mission is not the copy of itself", agent, result);
        assertDeepEquals(mission, result);

        //withdraw agent and test for his mission
        manager.withdrawAgentFromMission(agent, mission);
        result = manager.findMissionWithAgent(agent);
        assertNull("Faund mission for agent who was just withdrawed from one", result);
    }

    /**
     * Test of findAgentsOnMission method, of class AgencyManagerImpl.
     */
    @Test
    public void testFindAgentsOnMission() {
        System.out.println("testFindAgentsOnMission");

        List<Agent> result;

        //test null argument
        try {
            manager.findAgentsOnMission(null);
        } catch (IllegalArgumentException ex) {
            //OK
        }

        //create new mission
        Mission mission = newMission01();

        //test mission that is not in database
        try {
            result = manager.findAgentsOnMission(mission);
            fail("found agents for mission that is not in the the database" + result);
        } catch (IllegalArgumentException ex) {
            //OK
        }

        missionManager.createMission(mission);      //add mission to DB

        //this should be OK - emptyList
        result = manager.findAgentsOnMission(mission);
        assertEquals("Found agents for mission without any agent", Collections.<Agent>emptyList(), result);

        //add some agents to the DB
        Agent agent = newAgent01();
        Agent agent02 = newAgent02();
        agentManager.createAgent(agent);
        agentManager.createAgent(agent02);

        //test one agent on the mission
        manager.assignAgentToMission(agent, mission);
        result = manager.findAgentsOnMission(mission);
        assertEquals(Arrays.asList(agent), result);
        assertDeepEquals(agent, result.get(0));

        //test more agents on the mission
        agent02.setLevel(mission.getDifficulty());
        agentManager.updateAgent(agent02);
        manager.assignAgentToMission(agent02, mission);
        result = manager.findAgentsOnMission(mission);

        List<Agent> expected = Arrays.asList(agent, agent02);

        Collections.sort(result, idComparator);
        Collections.sort(expected, idComparator);

        assertEquals(expected, result);
        assertDeepEquals(expected, result);

        //test withdraw one agent from mission
        manager.withdrawAgentFromMission(agent, mission);
        result = manager.findAgentsOnMission(mission);
        assertEquals(Arrays.asList(agent02), result);
        assertDeepEquals(agent02, result.get(0));
    }

    /**
     * Test of assignAgentToMission method, of class AgencyManagerImpl.
     */
    @Test
    public void testAssignAgentToMission() {
        System.out.println("testAssignAgentToMission");

        //create agent - level 5
        Agent agent = newAgent01();
        agentManager.createAgent(agent);

        //create mission - difficulty 3
        Mission mission = newMission01();
        missionManager.createMission(mission);

        //test null agent
        try {
            manager.assignAgentToMission(null, mission);
            fail("Added null agent on a mission");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        //test null mission
        try {
            manager.assignAgentToMission(agent, null);
            fail("Added agent on a null mission");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        //test agent with no necessary level
        agent.setLevel(mission.getDifficulty() - 1);
        agentManager.updateAgent(agent);

        boolean result;

        try {
            result = manager.assignAgentToMission(agent, mission);
            assertFalse("Agent with no neccesery level for the mission difficulty assigned to the mission", result);
        } catch (IllegalArgumentException ex) {
            //OK
        }

        //create and test agent without putting him to DB - level 2
        Agent agent02 = newAgent02();

        try {
            manager.assignAgentToMission(agent02, mission);
            fail("Agent which is not in the system assigned to the mission");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        //create and test mission without putting it into the DB
        Mission mission02 = newMission02();

        try {
            manager.assignAgentToMission(agent, mission02);
            fail("Agent assigned to the mission which is not in the system");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        agent.setLevel(mission.getDifficulty());    //update the agent level
        agentManager.updateAgent(agent);

        //this should be OK
        result = manager.assignAgentToMission(agent, mission);
        assertTrue("Fail adding valid agent to the valid mission", result);

        //add remaining agent and mission in DB
        agentManager.createAgent(agent02);
        missionManager.createMission(mission02);

        //test assignment of an agent to another mission while he is already on one
        try {
            assertFalse("Agent, who is already on the mission assigned to another one", manager.assignAgentToMission(agent, mission02));
        } catch (IllegalArgumentException ex) {
            //OK
        }

        //assign another agent to the mission with agents on
        agent02.setLevel(mission.getDifficulty());
        agentManager.updateAgent(agent02);
        result = manager.assignAgentToMission(agent02, mission);
        assertTrue("Fail assigning another agent on mission with already some agents", result);

        //withdraw agent and assign him on another mission
        manager.withdrawAgentFromMission(agent, mission);
        result = manager.assignAgentToMission(agent, mission02);
        assertTrue("Fail assigning agent after withdrawing him from one", result);

        //test capacity assign more agents
        mission.setCapacity(1);
        missionManager.updateMission(mission);
        Agent agent03 = newAgent01();
        agentManager.createAgent(agent03);

        try {
            result = manager.assignAgentToMission(agent03, mission);
            assertFalse("More agents assigned on the mission then is possible due to it's capacity", result);
        } catch (IllegalArgumentException ex) {
            //OK
        }
    }

    /**
     * Test of withdrawAgentFromMission method, of class AgencyManagerImpl.
     */
    @Test
    public void testWithdrawAgentFromMission() {
        System.out.println("testWithdrawAgentFromMission");

        //create agent without adding to him to the DB
        Agent agent = newAgent01();

        //create mission without adding it to he DB
        Mission mission = newMission01();

        //test null agent
        try {
            manager.withdrawAgentFromMission(null, mission);
            fail("Null agent withdrawed from a mission");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        //test null mission
        try {
            manager.withdrawAgentFromMission(agent, null);
            fail("Agent withdrawed from a null mission");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        //withdraw agent that is not in the DB
        try {
            manager.withdrawAgentFromMission(agent, mission);
            fail("Agent that is not in the DB wa withdrawed from mission");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        agentManager.createAgent(agent);            //add agent to the DB
        missionManager.createMission(mission);    //add mission to the DB

        //withraw agent with no mission
        try {
            manager.withdrawAgentFromMission(agent, mission);
            fail("Agent without a mission was withdrawed from mission");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        Mission mission02 = newMission02();             //create another mission without adding it to the DB

        //withdraw agent from mission that is not in the DB
        try {
            manager.withdrawAgentFromMission(agent, mission02);
            fail("agent withdrawed from mission that is not in the DB");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        missionManager.createMission(mission02);            //add another mission to the DB
        manager.assignAgentToMission(agent, mission);   //assign agent on the first mission

        //withdraw agent from another mission than he or she is on
        try {
            manager.withdrawAgentFromMission(agent, mission02);
            fail("Agent withdrawed from another mission than he or she is on");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        //this should be OK
        manager.withdrawAgentFromMission(agent, mission);
        List<Agent> result = manager.findAgentsOnMission(mission);
        assertEquals("Error withdrawing agent from mission he is on", result, Collections.<Agent>emptyList());
    }

    /**
     * Test of findAvailableAgents method, of class AgencyManagerImpl.
     */
    @Test
    public void testFindAvailableAgents() {
        System.out.println("testFindAvailableAgents");

        //test with no agents in DB
        List<Agent> result = manager.findAvailableAgents();
        assertEquals("Method returned agents while there are no agents in the DB", Collections.<Agent>emptyList(), result);

        //create agent
        Agent agent = newAgent01();
        agentManager.createAgent(agent);

        //create mission
        Mission mission = newMission01();
        missionManager.createMission(mission);

        //test for the agent
        result = manager.findAvailableAgents();
        assertEquals("Method returned wrong count or list of agents", Arrays.asList(agent), result);
        assertDeepEquals(agent, result.get(0));

        //assign agent on mission and test for no available agents
        manager.assignAgentToMission(agent, mission);

        result = manager.findAvailableAgents();
        assertEquals("Method returned agent(s), while there is no avaiable agent in the DB", Collections.<Agent>emptyList(), result);

        //create another agent
        Agent agent02 = newAgent02();
        agentManager.createAgent(agent02);

        //test for available agent while another one is on the mission
        result = manager.findAvailableAgents();
        assertEquals(Arrays.asList(agent02), result);
        assertDeepEquals(agent02, result.get(0));

        agent02.setLevel(mission.getDifficulty());
        agentManager.updateAgent(agent02);
        manager.assignAgentToMission(agent02, mission);     //assign another agent on the mission

        //test no avaiable agents (all on the missions)
        result = manager.findAvailableAgents();
        assertEquals("Method returned agent(s), while there is no avaiable agent in the DB", Collections.<Agent>emptyList(), result);
    }

    /**
     * Test of findAvailableAgentsForMission method, of class AgencyManagerImpl.
     */
    @Test
    public void testFindAvailableAgentsForMission() {
        System.out.println("testFindAvailableAgentsForMission");

        List<Agent> result;

        //test null mission argument
        try {
            manager.findAvailableAgentsForMission(null);
            fail("Cannot find agents on the null mission");
        } catch (IllegalArgumentException ex) {
            //OK
        }

        //create agent - level 5
        Agent agent = newAgent01();
        agentManager.createAgent(agent);

        //create another agent - level 2
        Agent agent02 = newAgent02();
        agentManager.createAgent(agent02);

        //create mission - difficulty 3, do not put it into DB
        Mission mission = newMission01();

        //test mission that is not in the DB
        try {
            result = manager.findAvailableAgentsForMission(mission);
            fail("You cannot find agents for a mission that is not in the DB, found" + result.toString());
        } catch (IllegalArgumentException ex) {
            //OK
        }

        missionManager.createMission(mission);     //add mission in the DB

        //test - only agent pass the difficulty for mission
        result = manager.findAvailableAgentsForMission(mission);
        assertEquals("Wrong Collection of agents returned", Arrays.asList(agent), result);
        assertDeepEquals(agent, result.get(0));

        //add another mission - difficulty 2
        Mission mission02 = newMission02();
        missionManager.createMission(mission02);

        //test - both agents pass difficulty of mission02
        result = manager.findAvailableAgentsForMission(mission02);
        List<Agent> expected = Arrays.asList(agent, agent02);

        Collections.sort(result, idComparator);
        Collections.sort(expected, idComparator);

        assertEquals(expected, result);
        assertDeepEquals(expected, result);

        //set the higher difficulty for the first mission
        mission.setDifficulty(7);
        missionManager.updateMission(mission);

        //no agent should pass the difficulty now
        result = manager.findAvailableAgentsForMission(mission);
        assertEquals("Wrong Collection of agents returned", Collections.<Agent>emptyList(), result);
    }

    private static Agent newAgent(String name, Date born, int level, String note) {
        Agent agent = new Agent();

        agent.setName(name);
        agent.setBorn(born);
        agent.setLevel(level);
        agent.setNote(note);

        return agent;
    }

    private static Agent newAgent01() {
        Calendar cal = new GregorianCalendar(1980, Calendar.JANUARY, 1);

        return newAgent("James Bond", cal.getTime(), 5, "Best agent ever");
    }

    private static Agent newAgent02() {
        Calendar cal = new GregorianCalendar(1982, Calendar.DECEMBER, 5);

        return newAgent("Black widow", cal.getTime(), 2, "One of the Avengers");
    }

    private void assertDeepEquals(Agent agent, Agent result) {
        assertEquals("Agent has a different id", agent.getId(), result.getId());
        assertEquals("Agent has a different name", agent.getName(), result.getName());
        assertEquals("Agent has a different born date", agent.getBorn(), result.getBorn());
        assertEquals("Agent has a different level", agent.getLevel(), result.getLevel());
        assertEquals("Agent has a different note", agent.getNote(), result.getNote());
    }

    private static Mission newMission(String name, Date missionBegin, Date missionEnd, int difficulty, int capacity, String note) {
        Mission mission = new Mission();

        mission.setName(name);
        mission.setBeginDate(missionBegin);
        mission.setEndDate(missionEnd);
        mission.setDifficulty(difficulty);
        mission.setCapacity(capacity);
        mission.setNote(note);

        return mission;
    }

    private static Mission newMission01() {
        Calendar missionBegin = new GregorianCalendar(2015, Calendar.JANUARY, 1);
        Calendar missionEnd = new GregorianCalendar(2015, Calendar.MARCH, 11);

        return newMission("The Avengers", missionBegin.getTime(), missionEnd.getTime(), 3, 3, "code name - PV168");
    }

    private static Mission newMission02() {
        Calendar missionBegin = new GregorianCalendar(2014, Calendar.DECEMBER, 6);
        Calendar missionEnd = new GregorianCalendar(2015, Calendar.FEBRUARY, 9);

        return newMission("Age of Ultron", missionBegin.getTime(), missionEnd.getTime(), 2, 2, "code name - Dark World");
    }

    private void assertDeepEquals(Mission agent, Mission result) {
        assertEquals("Mission has a different id", agent.getId(), result.getId());
        assertEquals("Mission has a different begin date", agent.getBeginDate(), result.getBeginDate());
        assertEquals("Mission has a different end date", agent.getEndDate(), result.getEndDate());
        assertEquals("Mission has a different difficulty", agent.getDifficulty(), result.getDifficulty());
        assertEquals("Mission has a different capacity", agent.getCapacity(), result.getCapacity());
        assertEquals("Mission has a different note", agent.getNote(), result.getNote());
    }

    private void assertDeepEquals(List<Agent> expList, List<Agent> resList) {
        for (int i = 0; i < expList.size(); i++) {
            assertDeepEquals(expList.get(i), resList.get(i));
        }
    }

    private static final Comparator<Agent> idComparator = (Agent o1, Agent o2) -> o1.getId().compareTo(o2.getId());

}
