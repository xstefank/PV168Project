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
public interface MissionManager {

    void createMission(Mission mission);

    void deleteMission(Mission mission);

    List<Mission> findAllMissions();

    Mission findMissionById(Long missionId);

    void updateMission(Mission mission);
    
}
