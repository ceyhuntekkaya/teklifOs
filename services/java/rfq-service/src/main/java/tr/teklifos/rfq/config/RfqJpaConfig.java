package tr.teklifos.rfq.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "tr.teklifos.rfq.infrastructure")
public class RfqJpaConfig {}
