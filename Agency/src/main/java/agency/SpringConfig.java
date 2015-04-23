package agency;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.DERBY;
import org.springframework.test.jdbc.JdbcTestUtils;

/**
 * Spring Java configuration class. See http://static.springsource.org/spring/docs/current/spring-framework-reference/html/beans.html#beans-java
 * 
* @author
 */
@Configuration
@EnableTransactionManagement
public class SpringConfig {

    private final static Logger log = Logger.getLogger(SpringConfig.class);

    
//    @Bean
//    public DataSource dataSource() {
//            log.info("dataSource called");
//        return new EmbeddedDatabaseBuilder()
//                .setType(DERBY)
//                .ignoreFailedDrops(true)
//                .addScript("classpath:agencyDB.sql")
//                .addScript("classpath:test-data.sql")
//                .build();
//    }

    @Bean
    public DataSource dataSource() {
        //sítová databáze
        Properties myconf = new Properties();
        try {
            myconf.load(Agency.class.getResourceAsStream("/myconf.properties"));
        } catch(IOException ex) {
            log.error("Error loading properties", ex);
            return null;
        }
        
//        try {
//            Class.forName ("org.apache.derby.jdbc.ClientDriver");
//        } catch (ClassNotFoundException ex) {
//            System.out.println("ClassNotFoundException");
//        }
        
        BasicDataSource bds = new BasicDataSource();
        bds.setDriverClassName("org.apache.derby.jdbc.ClientDriver");
        bds.setUrl(myconf.getProperty("jdbc.url"));
        bds.setUsername(myconf.getProperty("jdbc.user"));
        bds.setPassword(myconf.getProperty("jdbc.password"));

        

        
//        JdbcTemplate template = new JdbcTemplate(bds);
//        JdbcTestUtils.executeSqlScript(template, new ClassPathResource("agencyDB.sql"), false);
//        JdbcTestUtils.executeSqlScript(template, new ClassPathResource("test-data.sql"), false);
        return bds;
    }
    
    
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public AgentManager agentManager() {
        AgentManagerImpl manager = new AgentManagerImpl();
        manager.setDataSource(dataSource());

        return manager;
    }

    @Bean
    public MissionManager missionManager() {
        MissionManagerImpl manager = new MissionManagerImpl();
        manager.setDataSource(new TransactionAwareDataSourceProxy(dataSource()));

        return manager;
    }

    @Bean
    public AgencyManager agencyManager() {
        AgencyManagerImpl manager = new AgencyManagerImpl();
        manager.setDataSource(dataSource());

        return manager;
    }
}
