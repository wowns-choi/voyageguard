package com.voyageguard.planning.infrastructure.config;

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
 * Planning 전용 DataSource/EntityManagerFactory/TransactionManager.
 *
 * BC마다 이 3개를 따로 두는 이유: DB를 BC별로 물리 분리했기 때문 - DataSource는 "DB 하나에
 * 연결하는 파이프"라서 여러 DB를 동시에 못 다루고, EntityManagerFactory는 그 DataSource로
 * 어떤 @Entity들을 관리할지(패키지 스캔 범위)를 결정하고, TransactionManager는 @Transactional이
 * 커밋/롤백을 위임할 대상이다. planning.application의 각 서비스는 반드시
 * @Transactional(transactionManager = "planningTransactionManager")를 명시해야 함 - 안 그러면
 * TransactionManager가 4개라 스프링이 어느 걸 쓸지 몰라서 에러남.
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "com.voyageguard.planning",
        entityManagerFactoryRef = "planningEntityManagerFactory",
        transactionManagerRef = "planningTransactionManager"
)
public class PlanningDataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.planning")
    public HikariDataSource planningDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean planningEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(planningDataSource());
        emf.setPackagesToScan("com.voyageguard.planning"); // 이 BC의 @Entity만 관리(다른 BC 엔티티는 아예 모름)
        emf.setPersistenceUnitName("planning");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.show_sql", "true");
        emf.setJpaPropertyMap(props);

        return emf;
    }

    @Bean
    public PlatformTransactionManager planningTransactionManager(
            @Qualifier("planningEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
