/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agency;

import com.ibatis.common.jdbc.ScriptRunner;
import common.DBUtils;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
 * @author Peter Topor
 */
public class MissionManagerImplTest {
    
    private MissionManagerImpl manager;
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
    
    @Before
    public void setUp() throws SQLException, FileNotFoundException, IOException {
        dataSource = prepareDataSource();
        
        DBUtils.executeSqlScript(dataSource, Agency.class.getResource("/agencyDBD.sql"));
        
        manager = new MissionManagerImpl();
        manager.setDataSource(dataSource);
    }
    
    /**
     * Test of createMission method, of class MissionManagerImpl.
     */
    @Test
    public void createExistingMission() {
        Mission mission = newMission();
        manager.createMission(mission);
        try {
            manager.createMission(mission);
            fail("Can create existing mission.");
        } catch (IllegalArgumentException ex) {
        }
    }
    
    @Test
    public void createNullMission() {
        try {
            manager.createMission(null);
            fail("Can create null mission.");
        } catch (IllegalArgumentException ex) {
        }  
    }
         
    @Test
    public void createMissionWithNullParameter() {
        Mission mission = newMission();
        
        mission.setBeginDate(null);
        try {
            manager.createMission(mission);
            fail("Can create mission with null beginDate.");
        } catch (IllegalArgumentException ex) {
        }  
        
        mission.setBeginDate(new GregorianCalendar(2001, 7, 15).getTime());
        mission.setEndDate(null);
        try {
            manager.createMission(mission);
            fail("Can create mission with null endDate.");
        } catch (IllegalArgumentException ex) {
        }
    }
        
    @Test
    public void createEndBeforeBegin() {
        Mission mission = newMission();
        mission.setEndDate(new GregorianCalendar(2001, 7, 14).getTime());
        try {
            manager.createMission(mission);
            fail("Can create mission that ends before it starts.");
        } catch (IllegalArgumentException ex) {
        }
    }
     
    @Test
    public void createSameBeginAndEnd() {
        Mission mission = newMission();
        mission.setEndDate(new GregorianCalendar(2001, 7, 15).getTime());
        try {
            manager.createMission(mission);    
        } catch (IllegalArgumentException ex) {
            fail("Can not create mission that starts and ends the same day.");
        }
    }
    
    @Test 
    public void createNoCapacity() {
        Mission mission = newMission();
        mission.setCapacity(0);
        try {
            manager.createMission(mission);
            fail("Can create mission with no capacity.");
        } catch (IllegalArgumentException ex) {
        }
     }
    
    @Test 
    public void createMinCapacity() {
        Mission mission = newMission();
        mission.setCapacity(1);
        try {
            manager.createMission(mission);           
        } catch (IllegalArgumentException ex) {
            fail("Can not create mission with min (1) capacity.");
        }
     }
     
    @Test
    public void createLowDifficulty() {
        Mission mission = newMission();
        mission.setDifficulty(0);
        try {
            manager.createMission(mission);
            fail("Can create mission with too low difficulty.");
        } catch (IllegalArgumentException ex) {
        }
    }
    
    @Test
    public void createMinDifficulty() {
        Mission mission = newMission();
        mission.setDifficulty(1);
        try {
            manager.createMission(mission);
            
        } catch (IllegalArgumentException ex) {
            fail("Can not create mission with min (1) difficulty.");
        }
    }
    
    @Test
    public void createHighDifficulty() {
        Mission mission = newMission();
        mission.setDifficulty(11);
        try {
            manager.createMission(mission);
            fail("Can create mission with too high difficulty.");
        } catch (IllegalArgumentException ex) {
        }
    }
    
    @Test
    public void createMaxDifficulty() {
        Mission mission = newMission();
        mission.setDifficulty(10);
        try {
            manager.createMission(mission);
        } catch (IllegalArgumentException ex) {
            fail("Can not create mission with max (10) difficulty.");
        }
    }
    
    @Test
    public void createValidParameters() {
        Mission mission = newMission();
        try {
            manager.createMission(mission);
        } catch (IllegalArgumentException ex) {
            fail("Method createMission should not throw IllegalArgumentException.");
        }
        Long missionId = mission.getId();
        assertNotNull("Can create mission with null id", missionId);
        
    }

    /**
     * Test of updateMission method, of class MissionManagerImpl.
     */
    @Test
    public void updateNullMission() {
        try {
            manager.updateMission(null);
            fail("Can update null mission.");
        } catch (IllegalArgumentException ex) {
        }  
    }
    
    @Test
    public void updateMissionWithNullParameter() {
        Mission mission = newMission();
        try {
            manager.createMission(mission);
        } catch (IllegalArgumentException ex) {
            fail("Method createMission should not throw IllegalArgumentException.");
        }
        
        mission.setBeginDate(null);
        try {
            manager.updateMission(mission);
            fail("Can update mission with null beginDate.");
        } catch (IllegalArgumentException ex) {
        }  
        
        mission.setBeginDate(new GregorianCalendar(2001, 7, 15).getTime());
        mission.setEndDate(null);
        try {
            manager.updateMission(mission);
            fail("Can update mission with null endDate.");
        } catch (IllegalArgumentException ex) {
        }  
    }      
        
    @Test
    public void updateEndBeforeBegin() {
        Mission mission = newMission();
        manager.createMission(mission);
        mission.setEndDate(new GregorianCalendar(2001, 7, 14).getTime());
        try {
            manager.updateMission(mission);
            fail("Can update mission that ends before it starts.");
        } catch (IllegalArgumentException ex) {
        }
     }
     
    @Test
    public void updateSameBeginAndEnd() {
        Mission mission = newMission();
        manager.createMission(mission);
        mission.setEndDate(new GregorianCalendar(2001, 7, 15).getTime());
        try {
            manager.updateMission(mission);    
        } catch (IllegalArgumentException ex) {
            fail("Can not update mission that starts and ends the same day.");
        }
    }
    
    @Test 
    public void updateNoCapacity() {
        Mission mission = newMission();
        manager.createMission(mission);
        mission.setCapacity(0);
        try {
            manager.updateMission(mission);
            fail("Can update mission with no capacity.");
        } catch (IllegalArgumentException ex) {
        }
    }
     
    @Test 
    public void updateMinCapacity() {
        Mission mission = newMission();
        manager.createMission(mission);
        mission.setCapacity(1);
        try {
            manager.updateMission(mission);        
        } catch (IllegalArgumentException ex) {
            fail("Can not update mission with min (1) capacity.");
        }
    }
    
    @Test
    public void updateLowDifficulty() {
        Mission mission = newMission();
        manager.createMission(mission);
        mission.setDifficulty(0);
        try {
            manager.updateMission(mission);
            fail("Can upadte mission with too low difficulty.");
        } catch (IllegalArgumentException ex) {
        }
    }
    
    @Test
    public void updateMinDifficulty() {
        Mission mission = newMission();
        manager.createMission(mission);
        mission.setDifficulty(1);
        try {
            manager.updateMission(mission);           
        } catch (IllegalArgumentException ex) {
            fail("Can not upadte mission with min (1) difficulty.");
        }
    }
    
    @Test
    public void updateHighDifficulty() {
        Mission mission = newMission();
        manager.createMission(mission);
        mission.setDifficulty(11);
        try {
            manager.updateMission(mission);
            fail("Can update mission with too high difficulty.");
        } catch (IllegalArgumentException ex) {
        }
    }
    
    @Test
    public void updateMaxDifficulty() {
        Mission mission = newMission();
        manager.createMission(mission);
        mission.setDifficulty(10);
        try {
            manager.updateMission(mission);
        } catch (IllegalArgumentException ex) {
            fail("Can not update mission with max (10) difficulty.");
        }
    }
    
    @Test
    public void updateNullId() {
        Mission mission = newMission();
        manager.createMission(mission);
        mission.setId(null);
        try {
            manager.updateMission(mission);
            fail("Can update mission with null id.");
        } catch (IllegalArgumentException ex) {
        }
    }
    
    @Test
    public void updateValidParamaters() {
        Mission mission = newMission();
        try {
            manager.updateMission(mission);
            fail("Can update not existing mission.");
        } catch (IllegalArgumentException ex) {
        }
        manager.createMission(mission);
        mission.setDifficulty(7);
        mission.setCapacity(2);
        manager.updateMission(mission);
        Mission actual = manager.findMissionById(mission.getId());
        assertEquals("Updated mission different then expected.", mission, actual);
        
    }

    /**
     * Test of deleteMission method, of class MissionManagerImpl.
     */
    @Test
    public void daleteNullMission() {
        try {
            manager.deleteMission(null);
            fail("Can delete null mission.");
        } catch (IllegalArgumentException ex) {
        }  
    }
    
/*    @Test
    public void deleteBeginNotMatch() {
        Mission mission = newMission();
            manager.createMission(mission);
        mission.setBeginDate(new GregorianCalendar(2001, 9, 15).getTime());
        try {
            manager.deleteMission(mission);  
            fail("Can delete when beginDate does not match.");
        } catch (IllegalArgumentException ex) {
        }
    }
    
    @Test
    public void deleteEndNotMatch() {
        Mission mission = newMission();
        manager.createMission(mission);
        mission.setEndDate(new GregorianCalendar(2001, 9, 15).getTime());
        try {
            manager.deleteMission(mission);   
            fail("Can delete when endDate does not match.");
        } catch (IllegalArgumentException ex) {
        }
    }
    
    @Test 
    public void deleteCapacityNotMatch() {
        Mission mission = newMission();
        manager.createMission(mission);
        mission.setCapacity(1);
        try {
            manager.deleteMission(mission);
            fail("Can  delete when capacity does not match");
        } catch (IllegalArgumentException ex) {
        }
     }
     
    @Test
    public void deleteDifficultyNotMatch() {
        Mission mission = newMission();
        manager.createMission(mission);
        mission.setDifficulty(1);
        try {
            manager.deleteMission(mission);
            fail("Can delete when difficulty does not match.");
        } catch (IllegalArgumentException ex) {
        }
    }
    
    @Test
    public void deleteNoteNotMatch() {
        Mission mission = newMission();
        manager.createMission(mission);
        mission.setNote("something different");
        try {
            manager.deleteMission(mission);
            fail("Can delete when difficulty does not match.");
        } catch (IllegalArgumentException ex) {
        }
    }*/
    
    @Test
    public void deleteNullId() {
        Mission mission = newMission();
        try {
            manager.deleteMission(mission);
            fail("Can delete mission with null id.");
        } catch (IllegalArgumentException ex) {
        }
    }
    
    @Test
    public void deleteMission() {
        Mission mission = newMission();
        manager.createMission(mission);
        manager.deleteMission(mission);
        
        try {
            manager.deleteMission(mission);
            fail("Can delete mission that is not in database.");
        } catch (InternalError ex) {    
        }
        
        List<Mission> expected = new ArrayList<>();
        List<Mission> actual = manager.findAllMissions();
        assertEquals("Mission was not deleted.", actual, expected);
    }

    /**
     * Test of findMissionById method, of class MissionManagerImpl.
     */
    @Test
    public void findNullId() {
        try {
            manager.findMissionById(null);
            fail("Can find mission with null id.");
        } catch (IllegalArgumentException ex) {
        }
    }
    
    @Test
    public void testFindMissionById() {
        Mission mission = newMission();
        Mission mission2 = newMission();
        mission2.setDifficulty(7);
        mission2.setCapacity(3);
        mission2.setNote("test mission 2");
        manager.createMission(mission);
        manager.createMission(mission2);
        
        try {
            manager.findMissionById(null);
            fail("Can find mission with null id");
        } catch (IllegalArgumentException ex) {
        }
        Mission actual = manager.findMissionById(mission.getId());
        assertEquals("Found wrong mission", mission, actual);
    }

    /**
     * Test of findAllMissions method, of class MissionManagerImpl.
     */
    @Test
    public void testFindAllMissions() {
        Mission mission = newMission();
        Mission mission2 = newMission();
        mission2.setDifficulty(7);
        mission2.setCapacity(3);
        mission2.setNote("test mission 2");
        manager.createMission(mission);
        manager.createMission(mission2);
        
        List<Mission> expected = new ArrayList<>(Arrays.asList(mission,mission2));
        List<Mission> actual = manager.findAllMissions();
        
        Collections.sort(expected, idComparator);
        Collections.sort(actual, idComparator);
        
        assertEquals("Returned list different then expected.", expected, actual);
    }

    private Mission newMission() {
        Mission mission = new Mission();
        
        mission.setName("The Avengers");
        mission.setBeginDate(new GregorianCalendar(2001, 7, 15).getTime());
        mission.setEndDate(new GregorianCalendar(2001, 7, 16).getTime());
        mission.setCapacity(5);
        mission.setDifficulty(5);
        mission.setNote("test mission");
        
        return mission;
    }
    
    private static final Comparator<Mission> idComparator = new Comparator<Mission>() {

        @Override
        public int compare(Mission o1, Mission o2) {
            return Long.valueOf(o1.getId()).compareTo(Long.valueOf(o2.getId()));
        }
    };
}