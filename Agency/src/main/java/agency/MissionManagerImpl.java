/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agency;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

/**
 *
 * @author martin
 */
public class MissionManagerImpl implements MissionManager {

    private final Logger log = Logger.getLogger(Agency.class);

    private DataSource dataSource;

    public MissionManagerImpl() {}
    
    public MissionManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void createMission(Mission mission) {

        log.debug("createMission called");

        testMissionParameter(mission);

        if (mission.getId() != null) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("missionAlreadyInDB"));
        }

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("INSERT INTO APP.Missions (\"name\",beginDate,endDate,difficulty,capacity,note) VALUES (?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {

                st.setString(1, mission.getName());
                st.setDate(2, new java.sql.Date(mission.getBeginDate().getTime()));
                st.setDate(3, new java.sql.Date(mission.getEndDate().getTime()));
                st.setInt(4, mission.getDifficulty());
                st.setInt(5, mission.getCapacity());
                st.setString(6, mission.getNote());

                int addedRows = st.executeUpdate();

                if (addedRows != 1) {
                    throw new InternalError("Internal Error: More rows inserted when trying to insert mission: " + mission);
                }

                ResultSet keyRS = st.getGeneratedKeys();
                mission.setId(getKey(keyRS, mission));
            }
        } catch (SQLException ex) {
            log.error("DB connection problem", ex);
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("createMissionDB"), ex);
        }

    }

    @Override
    public void updateMission(Mission mission) {

        log.debug("updateMission called");

        testMissionParameter(mission);

        if (mission.getId() == null) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("missionNotInDB"));
        }

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("UPDATE APP.Missions SET \"name\"=?,beginDate=?,endDate=?,difficulty=?,capacity=?,note=? WHERE id=?")) {

                st.setString(1, mission.getName());
                st.setDate(2, new java.sql.Date(mission.getBeginDate().getTime()));
                st.setDate(3, new java.sql.Date(mission.getEndDate().getTime()));
                st.setInt(4, mission.getDifficulty());
                st.setInt(5, mission.getDifficulty());
                st.setInt(6, mission.getCapacity());
                st.setLong(7, mission.getId());

                if (st.executeUpdate() != 1) {
                    throw new IllegalArgumentException("Can not update mission: " + mission);
                }
            }
        } catch (SQLException ex) {
            log.error("DB connection problem", ex);
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("updateMissionDB"), ex);
        }
    }

    @Override
    public void deleteMission(Mission mission) {

        log.debug("deleteMission called");

        if (mission == null) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("nullMission"));
        }
        if (mission.getId() == null) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("missionNotInDB"));
        }

        //why comment ??
        /* Mission missionDB = findMissionById(mission.getId());

         if (!mission.getBeginDate().equals(missionDB.getBeginDate())) {
         throw new IllegalArgumentException("BeginDate does not match.");
         }
         if (!mission.getEndDate().equals(missionDB.getEndDate())) {
         throw new IllegalArgumentException("EndDate does not match.");
         }
         if (mission.getDifficulty() != missionDB.getDifficulty()) {
         throw new IllegalArgumentException("Difficulty does not match.");
         }
         if (mission.getCapacity() != missionDB.getCapacity()) {
         throw new IllegalArgumentException("Capacity does not match.");
         }
         if (!mission.getNote().equals(missionDB.getNote())) {
         throw new IllegalArgumentException("Note does not match.");
         }*/
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("DELETE FROM APP.Missions WHERE id=?")) {
                st.setLong(1, mission.getId());

                if (st.executeUpdate() != 1) {
                    throw new InternalError("Internal Error: Did not delete mission with id =" + mission.getId());
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("MissionDB"), ex);
        }

    }

    @Override
    public Mission findMissionById(Long missionId) {

        log.debug("findMissionById called");

        if (missionId == null) {
            throw new IllegalArgumentException("MissionId can not be null");
        }

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,\"name\",beginDate,endDate,capacity,difficulty,note FROM APP.Missions WHERE id=?")) {
                st.setLong(1, missionId);
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    Mission mission = resultSetToMission(rs);
                    if (rs.next()) {
                        throw new InternalError(
                                "Internal error: More entities with the same id found "
                                + "(source id: " + missionId + ", found " + mission + " and " + resultSetToMission(rs));
                    }
                    return mission;
                } else {
                    return null;
                }
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("findMissionByIdDB"), ex);
        }
    }

    @Override
    public List<Mission> findAllMissions() {

        log.debug("findAllMissions called");

        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id,\"name\",beginDate,endDate,capacity,difficulty,note FROM APP.Missions")) {
                ResultSet rs = st.executeQuery();
                List<Mission> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(resultSetToMission(rs));
                }
                return result;
            }
        } catch (SQLException ex) {
            log.error("db connection problem", ex);
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("findAllMissionsDB"), ex);
        }
    }

    private void testMissionParameter(Mission mission) {
        if (mission == null) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("nullMission"));
        }
        if (mission.getBeginDate() == null) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("missionNullBeginDate"));
        }
        if (mission.getEndDate() == null) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("missionNullEndDate"));
        }
        if (mission.getBeginDate().after(mission.getEndDate())) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("missionEndBeforeBegin"));
        }
        if (mission.getCapacity() < 1) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("missionTooLowCapacity"));
        }
        if (mission.getDifficulty() < 1) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("missionTooLowDifficulty"));
        }
        if (mission.getDifficulty() > 10) {
            throw new IllegalArgumentException(ResourceBundle.getBundle("strings").getString("missionTooHighDifficulty"));
        }
    }

    private Long getKey(ResultSet keyRS, Mission mission) throws SQLException {
        if (keyRS.next()) {
            if (keyRS.getMetaData().getColumnCount() != 1) {
                throw new InternalError("Internal Error: Generated key"
                        + "retriving failed when trying to insert grave " + mission
                        + " - wrong key fields count: " + keyRS.getMetaData().getColumnCount());
            }
            Long result = keyRS.getLong(1);
            if (keyRS.next()) {
                throw new InternalError("Internal Error: Generated key"
                        + "retriving failed when trying to insert grave " + mission
                        + " - more keys found");
            }
            return result;
        } else {
            throw new InternalError("Internal Error: Generated key"
                    + "retriving failed when trying to insert grave " + mission
                    + " - no key found");
        }
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
