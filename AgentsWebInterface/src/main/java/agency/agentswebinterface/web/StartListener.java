package agency.agentswebinterface.web;

import agency.AgentManager;
import agency.MissionManager;
import agency.SpringConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@WebListener
public class StartListener implements ServletContextListener {

    private final static Logger log = Logger.getLogger(StartListener.class);

    @Override
    public void contextInitialized(ServletContextEvent ev) {
        log.info("web application inicialized");
        ServletContext servletContext = ev.getServletContext();
        ApplicationContext springContext = new AnnotationConfigApplicationContext(SpringConfig.class);
        servletContext.setAttribute("agentManager", springContext.getBean("agentManager", AgentManager.class));
        servletContext.setAttribute("missionManager", springContext.getBean("missionManager", MissionManager.class));
        log.info("managers created");
        
    }

    @Override
    public void contextDestroyed(ServletContextEvent ev) {
        log.info("the end of the application");
    }
}
