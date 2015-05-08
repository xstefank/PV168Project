/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import agency.Agent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author martin
 */
public class AgentsTableModel extends AbstractTableModel {

    private List<Agent> agents = new ArrayList<>();

    @Override
    public int getRowCount() {
        return agents.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        DateFormat df = new SimpleDateFormat(ResourceBundle.getBundle("strings").getString("dateFormat"));

        Agent agent = agents.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return agent.getName();
            case 1:
                return df.format(agent.getBorn());
            case 2:
                return agent.getLevel();
            case 3:
                return agent.getNote();
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
                return ResourceBundle.getBundle("strings").getString("bornColumn");
            case 2:
                return ResourceBundle.getBundle("strings").getString("levelColumn");
            case 3:
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
                return Integer.class;
            case 3:
                return String.class;
            default:
                throw new IllegalArgumentException("illegal columnIndex");
        }
    }
    
    

    public void addAgent(Agent agent) {
        agents.add(agent);
        int lastRow = agents.size() - 1;
        fireTableRowsInserted(lastRow, lastRow);
    }
    
    public void removeAgent(Agent agent) {
        agents.remove(agent);
        int lastRow = agents.size() - 1;
        fireTableRowsDeleted(lastRow, lastRow);
    }
    
    public Agent getSelectedAgent(int rowIndex) {
        return agents.get(rowIndex);
    }
    
    public List<Agent> getAllRows() {
        return Collections.unmodifiableList(agents);
    }
}
