package tr.teklifos.rfq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"tr.teklifos.rfq", "tr.teklifos.shared"})
@EnableScheduling
public class RfqApplication {

    public static void main(String[] args) {
        SpringApplication.run(RfqApplication.class, args);
    }
}
