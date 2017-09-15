package ua.adeptius.asterisk.spring_config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import ua.adeptius.asterisk.Main;
import ua.adeptius.asterisk.dao.Settings;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
//@EnableTransactionManagement
//@PropertySource({ "classpath:config.properties" })
public class HibernateConfig {



//    @Value("${jdbc.url}")
    private String url = Main.settings.getDbUrl() + "calltrackdb";
//    @Value("${jdbc.username}")
    private String username = Main.settings.getDbUsername();
//    @Value("${jdbc.password}")
    private String password = Main.settings.getDbPassword();


    @Bean
    public DataSource dataSource() throws Exception{
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass("com.mysql.jdbc.Driver");
        dataSource.setJdbcUrl(url+"?useUnicode=true&characterEncoding=utf8&characterSetResults=UTF-8");
        dataSource.setUser(username);
        dataSource.setPassword(password);
        dataSource.setInitialPoolSize(10);
        dataSource.setMinPoolSize(5);
        dataSource.setMaxPoolSize(20);
        dataSource.setAcquireIncrement(1);
        return dataSource;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
//    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPackagesToScan("ua.adeptius.asterisk.model"); // Где лежат сущности
        sessionFactory.setHibernateProperties(getHibernateProperties());
        return sessionFactory;
    }

    private Properties getHibernateProperties() {
        Properties properties = new Properties();
        properties.put(AvailableSettings.DIALECT, "org.hibernate.dialect.MySQLDialect");
        properties.put(AvailableSettings.SHOW_SQL, false);
        properties.put(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "org.springframework.orm.hibernate5.SpringSessionContext");
        properties.put("hibernate.connection.CharSet", "UTF-8");
        properties.put("hibernate.connection.useUnicode", true);
        properties.put("hibernate.connection.characterEncoding", "UTF-8");
        return properties;
    }

//    @Bean
//    public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
//        HibernateTransactionManager txManager = new HibernateTransactionManager();
//        txManager.setSessionFactory(sessionFactory);
//        return txManager;
//    }
}
