package com.voyageguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

// DataSource/EntityManagerFactory/TransactionManager 기본 자동설정(단일 DB 전제)을 끄고,
// BC별 xxxDataSourceConfig(planning/sales/payment/auth 각 infrastructure/config 밑)가
// 각자 자기 DB만 담당하는 버전을 직접 등록한다 - DB를 BC별로 물리 분리했기 때문.
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        DataJpaRepositoriesAutoConfiguration.class
})
@EnableScheduling // OutboxRelay의 @Scheduled 폴링을 실제로 동작시키기 위해 필요
public class VoyageguardApplication {
	public static void main(String[] args) {
		SpringApplication.run(VoyageguardApplication.class, args);
	}
}
