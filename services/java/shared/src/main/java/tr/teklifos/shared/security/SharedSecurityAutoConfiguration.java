package tr.teklifos.shared.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@EnableConfigurationProperties(TeklifosInternalProperties.class)
@Import({ResourceServerSecurityConfig.class})
public class SharedSecurityAutoConfiguration {}
