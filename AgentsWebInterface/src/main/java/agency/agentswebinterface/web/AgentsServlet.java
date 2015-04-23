/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agency.agentswebinterface.web;

import agency.Agency;
import agency.Agent;
import agency.AgentManager;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 *
 * @author martin
 */
@WebServlet(AgentsServlet.URL_MAPPING + "/*")
public class AgentsServlet extends HttpServlet {

    private static final String LIST_JSP = "/list.jsp";
    public static final String URL_MAPPING = "/agents";

    private final static Logger log = Logger.getLogger(AgentsServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        showAgentsList(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("utf-8");
        
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        String action = request.getPathInfo();
        switch (action) {
            case "/addRedirect":
                log.info("Add of new agent redirect");

                showAddPage(request, response);

//                showAgentsList(request, response);
                return;
            case "/add":
                String nameInput = request.getParameter("name");
                String bornInput = request.getParameter("born");
                String levelInput = request.getParameter("level");
                String noteInput = request.getParameter("note");

                if (nameInput == null || nameInput.length() == 0
                        || bornInput == null || bornInput.length() == 0
                        || levelInput == null || levelInput.length() == 0
                        || noteInput == null || noteInput.length() == 0) {
                    request.setAttribute("error", "Some parameter is missing");

                    showAddPage(request, response);
                    return;
                }

                

                try {
                    Agent agent = new Agent();
                    agent.setName(nameInput);
                    agent.setBorn(df.parse(bornInput));
                    agent.setLevel(Integer.parseInt(levelInput));
                    agent.setNote(noteInput);

                    getAgentManager().createAgent(agent);
                    log.debug("created agent: " + agent);

                    //redirect-after-POST je ochrana před vícenásobným odesláním formuláře
                    response.sendRedirect(request.getContextPath() + URL_MAPPING);
                    return;

                } catch (ParseException pe) {
                    log.error("Invalid date input", pe);

                    request.setAttribute("error", "Invalid Born attribute: " + pe.getMessage() + ", please enter date in format \"dd-MM-yyyy\"");
                    showAddPage(request, response);
                    return;
                } catch (NumberFormatException nfe) {
                    log.error("Invalid level input", nfe);

                    request.setAttribute("error", "Invalid Level attribute: " + nfe.getMessage() + ", please enter level in range 1 to 10");
                    showAddPage(request, response);
                    return;
                } catch(IllegalArgumentException iae) {
                    log.error("illegal argument input", iae);

                    request.setAttribute("error", iae.getMessage());
                    showAddPage(request, response);
                    return;
                }
            case "/delete":
                try {
                    Long id = Long.valueOf(request.getParameter("id"));
                    getAgentManager().deleteAgent(getAgentManager().findAgentById(id));
                    log.debug("deleted agent: " + id);

                    response.sendRedirect(request.getContextPath() + URL_MAPPING);
                    return;
                } catch (NumberFormatException e) {
                    log.error("Cannot delete agent", e);

                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());

//                    request.setAttribute("error", e.getMessage());
//                    request.getRequestDispatcher("/index.jsp").forward(request, response);
                    return;
                }
            case "/update":
                log.info("Update of agent: " + request.getParameter("id") + " redirect");

                showEditPage(request.getParameter("id"), request, response);

                showAgentsList(request, response);
                return;
            case "/edit":
                //TODO
                log.info("Edit of agent called");

                Agent agent = getAgentManager().findAgentById(Long.valueOf(request.getParameter("id")));
                nameInput = request.getParameter("name");
                bornInput = request.getParameter("born");
                levelInput = request.getParameter("level");
                noteInput = request.getParameter("note");

                if (nameInput == null || nameInput.length() == 0
                        || bornInput == null || bornInput.length() == 0
                        || levelInput == null || levelInput.length() == 0
                        || noteInput == null || noteInput.length() == 0) {
                    request.setAttribute("error", "Some parameter is missing");

                    showEditPage(request.getParameter("id"), request, response);
                    return;
                }

                try {
                    agent.setName(nameInput);
                    agent.setBorn(df.parse(bornInput));
                    agent.setLevel(Integer.parseInt(levelInput));
                    agent.setNote(noteInput);

                    getAgentManager().updateAgent(agent);
                    log.debug("updated agent: " + agent);

                    response.sendRedirect(request.getContextPath() + URL_MAPPING);
                    return;

                } catch (ParseException pe) {
                    log.error("Invalid date input", pe);

                    request.setAttribute("error", "Invalid Born attribute: " + pe.getMessage() + ", please enter date in format \"dd-MM-yyyy\"");
                    showEditPage(request.getParameter("id"), request, response);
                    return;
                } catch (NumberFormatException nfe) {
                    log.error("Invalid level input", nfe);

                    request.setAttribute("error", "Invalid Level attribute: " + nfe.getMessage() + ", please enter level in range 1 to 10");
                    showEditPage(request.getParameter("id"), request, response);
                    return;
                } catch(IllegalArgumentException iae) {
                    log.error("illegal argument input", iae);

                    request.setAttribute("error", iae.getMessage());
                    showEditPage(request.getParameter("id"), request, response);
                    return;
                }
            default:
                log.error("Unknown action " + action);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown action " + action);
        }
    }

    /**
     * Gets BookManager from ServletContext, where it was stored by {@link StartListener}.
     *
     * @return BookManager instance
     */
    private AgentManager getAgentManager() {
        return (AgentManager) getServletContext().getAttribute("agentManager");
    }

    /**
     * Stores the list of books to request attribute "books" and forwards to the JSP to display it.
     */
    private void showAgentsList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.setAttribute("agents", getAgentManager().findAllAgents());
            request.getRequestDispatcher(LIST_JSP).forward(request, response);
        } catch (ServletException e) {
            log.error("Cannot show agents", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void showEditPage(String parameter, HttpServletRequest request, HttpServletResponse response) throws IOException {

        try {
            request.setAttribute("agent", getAgentManager().findAgentById(Long.valueOf(request.getParameter("id"))));
            request.getRequestDispatcher("/edit.jsp").forward(request, response);
        } catch (ServletException e) {
            log.error("Cannot show edit page", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void showAddPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            request.getRequestDispatcher("/add.jsp").forward(request, response);
        } catch (ServletException e) {
            log.error("Cannot show add page", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
