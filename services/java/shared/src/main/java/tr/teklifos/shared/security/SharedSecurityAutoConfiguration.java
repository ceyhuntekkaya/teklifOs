package tr.teklifos.shared.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({ResourceServerSecurityConfig.class})
public class SharedSecurityAutoConfiguration {}
