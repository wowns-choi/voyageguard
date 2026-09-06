package com.voyageguard.sales.infrastructure.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Sales 전용 DataSource/EntityManagerFactory/TransactionManager - 이유는 PlanningDataSourceConfig
 * 주석 참고. maximum-pool-size는 낙관적 락 REQUIRES_NEW 부하테스트 때문에 다른 BC보다 크게
 * 잡혀있음(spring.datasource.sales.maximum-pool-size, 기존 hikari 설정과 동일한 값 유지).
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "com.voyageguard.sales",
        entityManagerFactoryRef = "salesEntityManagerFactory",
        transactionManagerRef = "salesTransactionManager"
)
public class SalesDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.sales")
    public HikariDataSource salesDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean salesEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(salesDataSource());
        emf.setPackagesToScan("com.voyageguard.sales");
        emf.setPersistenceUnitName("sales");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.show_sql", "true");
        emf.setJpaPropertyMap(props);

        return emf;
    }

    @Bean
    public PlatformTransactionManager salesTransactionManager(
            @Qualifier("salesEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
