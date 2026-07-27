package tr.teklifos.masterdata.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "tr.teklifos.masterdata.infrastructure")
public class MasterDataJpaConfig {}
