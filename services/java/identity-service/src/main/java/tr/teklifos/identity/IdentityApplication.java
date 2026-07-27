package tr.teklifos.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
        scanBasePackages = {"tr.teklifos.identity", "tr.teklifos.shared"},
        exclude = {tr.teklifos.shared.security.SharedSecurityAutoConfiguration.class})
@EnableScheduling
public class IdentityApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityApplication.class, args);
    }
}
