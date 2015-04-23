/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agency;

import common.DBUtils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
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
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 *
 * @author martin
 */
public class AgentManagerImplTest {

    private AgentManagerImpl manager;
    private DataSource dataSource;

    @Rule
    public ExpectedException exception = ExpectedException.none();

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
     * @throws java.io.FileNotFoundException
     */
    @Before
    public void setUp() throws SQLException, FileNotFoundException, IOException {
        dataSource = prepareDataSource();
        
        DBUtils.executeSqlScript(dataSource, Agency.class.getResource("/agencyDBD.sql"));
        
        manager = new AgentManagerImpl();
        manager.setDataSource(dataSource);
    }

    /*
     * Tests of createAgent method, of class AgentManagerImpl. Valid arguments
     */
    @Test
    public void testCreateAgentValid() {
        //create new agent
        Agent agent = newAgent01();
        manager.createAgent(agent);
        Long agentId = agent.getId();

        //test a valid input of agent to the DB
        assertNotNull("Valid agent has a null id", agentId);
        Agent result = manager.findAgentById(agentId);

        assertNotNull("validly added agent not returned", result);
        assertEquals("Valid agent is different than when inserted", agent, result);
        assertNotSame("Valid agent is not the copy of itself", agent, result);
        assertDeepEquals(agent, result);
    }

    @Test
    public void testCreateAgentValidNullNote() {
        //test - null note - should be OK
        Agent agent = newAgent01();
        agent.setNote(null);

        manager.createAgent(agent);
        Agent result = manager.findAgentById(agent.getId());

        assertNotNull("Validly added agent with a null note not returned", result);
        assertEquals("Valid agent with null note is different than when inserted", agent, result);
        assertNotSame("Valid agent is not the copy of itself", agent, result);
        assertNull("Validly added agent has a wrong note attribute, should be null", result.getNote());
        assertDeepEquals(agent, result);
    }

    /*
     * Additional tests of createAgent method, of class AgentManagerImpl. Invalid arguments
     */
    @Test
    public void testCreateAgentWithNull() {
        exception.expect(IllegalArgumentException.class);
        manager.createAgent(null);
    }

    @Test
    public void testCreateAgentWithNullName() {
        Agent agent = newAgent01();
        agent.setName(null);

        exception.expect(IllegalArgumentException.class);
        manager.createAgent(agent);
    }

    @Test
    public void testCreateAgentWithInvalidBornNull() {
        Agent agent = newAgent01();
        agent.setBorn(null);

        exception.expect(IllegalArgumentException.class);
        manager.createAgent(agent);
    }

    @Test
    public void testCreateAgentWithInvalidBornFuture() {
        Agent agent = newAgent01();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 5);
        agent.setBorn(cal.getTime());

        exception.expect(IllegalArgumentException.class);
        manager.createAgent(agent);
    }

    @Test
    public void testCreateAgentWithInvalidBornLess18() {
        Agent agent = newAgent01();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -18);
        agent.setBorn(cal.getTime());

        exception.expect(IllegalArgumentException.class);
        manager.createAgent(agent);
    }

    @Test
    public void testCreateAgentWithInvalidLevelNegative() {
        Agent agent = newAgent01();
        agent.setLevel(-5);

        exception.expect(IllegalArgumentException.class);
        manager.createAgent(agent);
    }

    @Test
    public void testCreateAgentWithInvalidLevelZero() {
        Agent agent = newAgent01();
        agent.setLevel(0);

        exception.expect(IllegalArgumentException.class);
        manager.createAgent(agent);
    }

    @Test
    public void testCreateAgentWithInvalidLevelTooHigh() {
        Agent agent = newAgent01();
        agent.setLevel(11);

        exception.expect(IllegalArgumentException.class);
        manager.createAgent(agent);
    }

    /*
     * Tests of updateAgent method, of class AgentManagerImpl. Valid arguments
     */
    @Test
    public void testUpdateAgentValidName() {
        //create agents
        Agent agent = newAgent01();
        Agent agent02 = newAgent02();

        manager.createAgent(agent);
        manager.createAgent(agent02);

        Agent reference = manager.findAgentById(agent.getId());
        assertNotNull("Agent has not been added to the DB", reference);
        reference.setName("Hawkeye");

        manager.updateAgent(reference);
        assertEquals("Error update of agent name", "Hawkeye", reference.getName());
        assertEquals("Error in born attribute while updating agent's name", new GregorianCalendar(1980, Calendar.JANUARY, 1).getTime(), reference.getBorn());
        assertEquals("Error in level attribute while updating agent's name", 5, reference.getLevel());
        assertEquals("Error in note attribute while updating agent's name", "Best agent ever", reference.getNote());

        // Check if updates didn't affect other records
        assertDeepEquals(agent02, manager.findAgentById(agent02.getId()));
    }

    @Test
    public void testUpdateAgentValidBorn() {
        //create agents
        Agent agent = newAgent01();
        Agent agent02 = newAgent02();

        manager.createAgent(agent);
        manager.createAgent(agent02);

        Agent reference = manager.findAgentById(agent.getId());
        assertNotNull("Agent has not been added to the DB", reference);

        Calendar cal = new GregorianCalendar(1980, Calendar.FEBRUARY, 2);
        reference.setBorn(cal.getTime());

        manager.updateAgent(agent);
        assertEquals("Error update of agent birth date", cal.getTime(), reference.getBorn());
        assertEquals("Error in name attribute while updating agent's birth date", "James Bond", reference.getName());
        assertEquals("Error in level attribute while updating agent's birth date", 5, reference.getLevel());
        assertEquals("Error in note attribute while updating agent's birth date", "Best agent ever", reference.getNote());

        // Check if updates didn't affect other records
        assertDeepEquals(agent02, manager.findAgentById(agent02.getId()));
    }

    @Test
    public void testUpdateAgentValidLevel() {
        //create agents
        Agent agent = newAgent01();
        Agent agent02 = newAgent02();

        manager.createAgent(agent);
        manager.createAgent(agent02);

        Agent reference = manager.findAgentById(agent.getId());
        assertNotNull("Agent has not been added to the DB", reference);
        reference.setLevel(4);

        manager.updateAgent(reference);
        assertEquals("Error update of agent level", 4, reference.getLevel());
        assertEquals("Error in name attribute while updating agent's level", "James Bond", reference.getName());
        assertEquals("Error in born attribute while updating agent's name", new GregorianCalendar(1980, Calendar.JANUARY, 1).getTime(), reference.getBorn());
        assertEquals("Error in note attribute while updating agent's name", "Best agent ever", reference.getNote());

        // Check if updates didn't affect other records
        assertDeepEquals(agent02, manager.findAgentById(agent02.getId()));
    }

    @Test
    public void testUpdateAgentValidNote() {
        //create agents
        Agent agent = newAgent01();
        Agent agent02 = newAgent02();

        manager.createAgent(agent);
        manager.createAgent(agent02);

        Agent reference = manager.findAgentById(agent.getId());
        assertNotNull("Agent has not been added to the DB", reference);
        reference.setNote("Not a good agent after all");

        manager.updateAgent(reference);
        assertEquals("Error update of agent note", "Not a good agent after all", reference.getNote());
        assertEquals("Error in name attribute while updating agent's note", "James Bond", reference.getName());
        assertEquals("Error in born attribute while updating agent's note", new GregorianCalendar(1980, Calendar.JANUARY, 1).getTime(), reference.getBorn());
        assertEquals("Error in level attribute while updating agent's note", 5, reference.getLevel());

        //test null note
        reference.setNote(null);

        manager.updateAgent(reference);
        assertEquals("Error update of agent note", null, reference.getNote());
        assertEquals("Error in name attribute while updating agent's note", "James Bond", reference.getName());
        assertEquals("Error in born attribute while updating agent's note", new GregorianCalendar(1980, Calendar.JANUARY, 1).getTime(), reference.getBorn());
        assertEquals("Error in level attribute while updating agent's note", 5, reference.getLevel());

        // Check if updates didn't affect other records
        assertDeepEquals(agent02, manager.findAgentById(agent02.getId()));
    }

    /*
     * Additional tests of updateAgent method, of class AgentManagerImpl.
     * Invalid arguments
     */
    @Test
    public void testUpdateAgentWithNull() {
        exception.expect(IllegalArgumentException.class);
        manager.updateAgent(null);
    }

    @Test
    public void testUpdateAgentWithNullId() {
        Agent agent = newAgent01();
        manager.createAgent(agent);
        assertNotNull("Error adding agent to the DB", agent.getId());

        Agent reference = manager.findAgentById(agent.getId());
        reference.setId(null);

        exception.expect(IllegalArgumentException.class);
        manager.updateAgent(reference);
    }

    @Test
    public void testUpdateAgentWithInvalidId() {
        Agent agent = newAgent01();
        manager.createAgent(agent);
        assertNotNull("Error adding agent to the DB", agent.getId());

        Agent reference = manager.findAgentById(agent.getId());
        reference.setId(-1L);

        exception.expect(IllegalArgumentException.class);
        manager.updateAgent(reference);
    }

    @Test
    public void testUpdateAgentWithFakeId() {
        Agent agent = newAgent01();
        manager.createAgent(agent);
        assertNotNull("Error adding agent to the DB", agent.getId());

        Agent reference = manager.findAgentById(agent.getId());
        reference.setId(agent.getId() + 1);

        exception.expect(IllegalArgumentException.class);
        manager.updateAgent(reference);
    }

    @Test
    public void testUpdateAgentWithZeroId() {
        Agent agent = newAgent01();
        manager.createAgent(agent);
        assertNotNull("Error adding agent to the DB", agent.getId());

        Agent reference = manager.findAgentById(agent.getId());
        reference.setId(0L);

        exception.expect(IllegalArgumentException.class);
        manager.updateAgent(reference);
    }

    @Test
    public void testUpdateAgentWithNullName() {
        Agent agent = newAgent01();
        manager.createAgent(agent);
        assertNotNull("Error adding agent to the DB", agent.getId());

        Agent reference = manager.findAgentById(agent.getId());
        reference.setName(null);

        exception.expect(IllegalArgumentException.class);
        manager.updateAgent(reference);
    }

    @Test
    public void testUpdateAgentWithInvalidBornNull() {
        Agent agent = newAgent01();
        manager.createAgent(agent);
        assertNotNull("Error adding agent to the DB", agent.getId());

        Agent reference = manager.findAgentById(agent.getId());
        reference.setBorn(null);

        exception.expect(IllegalArgumentException.class);
        manager.updateAgent(reference);
    }

    @Test
    public void testUpdateAgentWithInvalidBornFuture() {
        Agent agent = newAgent01();
        manager.createAgent(agent);
        assertNotNull("Error adding agent to the DB", agent.getId());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 5);

        Agent reference = manager.findAgentById(agent.getId());
        reference.setBorn(cal.getTime());

        exception.expect(IllegalArgumentException.class);
        manager.updateAgent(reference);
    }

    @Test
    public void testUpdateAgentWithInvalidBornLess18() {
        Agent agent = newAgent01();
        manager.createAgent(agent);
        assertNotNull("Error adding agent to the DB", agent.getId());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -18);

        Agent reference = manager.findAgentById(agent.getId());
        reference.setBorn(cal.getTime());

        exception.expect(IllegalArgumentException.class);
        manager.updateAgent(reference);
    }

    @Test
    public void testUpdateAgentWithInvalidLevelNegative() {
        Agent agent = newAgent01();
        manager.createAgent(agent);
        assertNotNull("Error adding agent to the DB", agent.getId());

        Agent reference = manager.findAgentById(agent.getId());
        reference.setLevel(-5);

        exception.expect(IllegalArgumentException.class);
        manager.updateAgent(reference);
    }

    @Test
    public void testUpdateAgentWithInvalidLevelZero() {
        Agent agent = newAgent01();
        manager.createAgent(agent);
        assertNotNull("Error adding agent to the DB", agent.getId());

        Agent reference = manager.findAgentById(agent.getId());
        reference.setLevel(0);

        exception.expect(IllegalArgumentException.class);
        manager.updateAgent(reference);
    }

    @Test
    public void testUpdateAgentWithInvalidLevelTooHigh() {
        Agent agent = newAgent01();
        manager.createAgent(agent);
        assertNotNull("Error adding agent to the DB", agent.getId());

        Agent reference = manager.findAgentById(agent.getId());
        reference.setLevel(15);

        exception.expect(IllegalArgumentException.class);
        manager.updateAgent(reference);
    }

    /**
     * Test of deleteAgent method, of class AgentManagerImpl. Valid arguments
     */
    @Test
    public void testDeleteAgent() {

        //create agents
        Agent agent = newAgent01();
        Agent agent02 = newAgent02();

        manager.createAgent(agent);
        manager.createAgent(agent02);

        //test whether the agents was indeed inserted
        assertNotNull("Error adding agent to the DB", manager.findAgentById(agent.getId()));
        assertNotNull("Error adding agent to the DB", manager.findAgentById(agent02.getId()));

        manager.deleteAgent(agent);     //delete first agent

        assertNull(manager.findAgentById(agent.getId()));               //first agent should be deleted
        assertNotNull(manager.findAgentById(agent02.getId()));      //second agent should be still in the DB
    }

    /**
     * Additional tests of deleteAgent method, of class AgentManagerImpl. Invalid arguments
     */
    @Test
    public void testDeleteAgentWithNull() {
        exception.expect(IllegalArgumentException.class);
        manager.deleteAgent(null);
    }

    @Test
    public void testDeleteAgentWithNullId() {
        Agent agent = newAgent01();
        agent.setId(null);

        exception.expect(IllegalArgumentException.class);
        manager.deleteAgent(agent);
    }

    @Test
    public void testDeleteAgentWithInvalidIdNegative() {
        Agent agent = newAgent01();
        agent.setId(-1L);

        exception.expect(IllegalArgumentException.class);
        manager.deleteAgent(agent);
    }

    @Test
    public void testDeleteAgentWithInvalidIdZero() {
        Agent agent = newAgent01();
        agent.setId(0L);

        exception.expect(IllegalArgumentException.class);
        manager.deleteAgent(agent);
    }

    /**
     * Tests of findAgentById method, of class AgentManagerImpl.
     */
    @Test
    public void testFindAgentByIdValid() {

        assertNull("Error loading agent who is not in the DB", manager.findAgentById(1L));      //this should be OK - there is no agent in the DB yet

        //create agent
        Agent agent = newAgent01();
        manager.createAgent(agent);

        //test - find agent recently added, this should be OK
        Long agentId = agent.getId();
        Agent result = manager.findAgentById(agentId);
        assertEquals("Error adding or finding agent by Id attribute", agent, result);
        assertNotSame("Returned agent is not the copy of himself", agent, result);
        assertDeepEquals(agent, result);
    }

    //Additional tests of findAgentById method, of class AgentManagerImpl.
    //Invalid arguments
    @Test
    public void testFindAgentByIdWithNull() {
        exception.expect(IllegalArgumentException.class);
        manager.findAgentById(null);
    }

    @Test
    public void testFindAgentByIdWithInvalidIdNegative() {
        exception.expect(IllegalArgumentException.class);
        manager.findAgentById(-1L);
    }

    @Test
    public void TestFindAgentByIdWithInvalidIdZero() {
        exception.expect(IllegalArgumentException.class);
        manager.findAgentById(0L);
    }

    /**
     * Test of findAllAgents method, of class AgentManagerImpl.
     */
    @Test
    public void testFindAllAgents() {

        //create agents
        Agent agent01 = newAgent01();
        Agent agent02 = newAgent02();

        manager.createAgent(agent01);
        manager.createAgent(agent02);

        //this should be OK
        List<Agent> expResult = new ArrayList<>(Arrays.asList(agent01, agent02));
        List<Agent> result = manager.findAllAgents();

        Collections.sort(result, idComparator);
        Collections.sort(expResult, idComparator);

        assertEquals(expResult, result);
        assertDeepEquals(expResult, result);
    }

    @Test
    public void testFindAllAgentsNoAgents() {
        assertTrue(manager.findAllAgents().isEmpty());      //this should be OK - there are no agents in the DB

        //add and remove agent
        Agent agent = newAgent01();
        manager.createAgent(agent);
        manager.deleteAgent(agent);

        assertTrue(manager.findAllAgents().isEmpty());
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
        Calendar cal2 = new GregorianCalendar(1982, Calendar.DECEMBER, 5);

        return newAgent("Black widow", cal2.getTime(), 3, "One of the Avengers");
    }

    private void assertDeepEquals(Agent agent, Agent result) {
        assertEquals("Agent has a different id", agent.getId(), result.getId());
        assertEquals("Agent has a different name", agent.getName(), result.getName());
        assertEquals("Agent has a different born date", agent.getBorn(), result.getBorn());
        assertEquals("Agent has a different level", agent.getLevel(), result.getLevel());
        assertEquals("Agent has a different note", agent.getNote(), result.getNote());
    }

    private void assertDeepEquals(List<Agent> expList, List<Agent> resList) {
        for (int i = 0; i < expList.size(); i++) {
            assertDeepEquals(expList.get(i), resList.get(i));
        }
    }

    private static Comparator<Agent> idComparator = new Comparator<Agent>() {

        @Override
        public int compare(Agent o1, Agent o2) {
            return Long.valueOf(o1.getId()).compareTo(Long.valueOf(o2.getId()));
        }
    };
}
