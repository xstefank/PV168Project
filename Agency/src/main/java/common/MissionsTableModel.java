/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import agency.Mission;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Peter Topor
 */
public class MissionsTableModel extends AbstractTableModel{
    private List<Mission> missions = new ArrayList<>();

    @Override
    public int getRowCount() {
        return missions.size();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        DateFormat df = new SimpleDateFormat(ResourceBundle.getBundle("strings").getString("dateFormat"));

        Mission mission = missions.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return mission.getName();
            case 1:
                return df.format(mission.getBeginDate());
            case 2:
                return df.format(mission.getEndDate());
            case 3:
                return mission.getDifficulty();
            case 4:
                return mission.getCapacity();
            case 5:
                return mission.getNote();
            default:
                throw new IllegalArgumentException("illegal columnIndex");
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return ResourceBundle.getBundle("strings").getString("nameColumn");
            case 1:
                return ResourceBundle.getBundle("strings").getString("beginDateColumn");
            case 2:
                return ResourceBundle.getBundle("strings").getString("endDateColumn");
            case 3:
                return ResourceBundle.getBundle("strings").getString("difficultyColumn");
            case 4:
                return ResourceBundle.getBundle("strings").getString("capacityColumn");
            case 5:
                return ResourceBundle.getBundle("strings").getString("noteColumn");
            default:
                throw new IllegalArgumentException("illegal columnIndex");
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
                return String.class;
            case 2:
                return String.class;
            case 3:
                return Integer.class;
            case 4:
                return Integer.class;
            case 5:
                return String.class;
            default:
                throw new IllegalArgumentException("illegal columnIndex");
        }
    }
    
    

    public void addMission(Mission mission) {
        missions.add(mission);
        int lastRow = missions.size() - 1;
        fireTableRowsInserted(lastRow, lastRow);
    }
    
    public void removeMission(Mission mission) {
        missions.remove(mission);
        int lastRow = missions.size() - 1;
        fireTableRowsDeleted(lastRow, lastRow);
    }
    
    public Mission getSelectedMission(int rowIndex) {
        return missions.get(rowIndex);
    }
    
    public List<Mission> getAllRows() {
        return Collections.unmodifiableList(missions);
    }
    
    public int getMissionIndex(Long missionID) {
        for (Mission mission : missions) {
            if(mission.getId() == missionID) {
                return missions.indexOf(mission);
            }
        }
        
        throw new IllegalArgumentException("Agent is nor assigned on any mission");
        
    }
}
