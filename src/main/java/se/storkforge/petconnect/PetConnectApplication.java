package se.storkforge.petconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import se.storkforge.petconnect.config.EnvInitializer;

@SpringBootApplication(scanBasePackages = "se.storkforge.petconnect")
@EntityScan("se.storkforge.petconnect.entity")
@EnableJpaRepositories("se.storkforge.petconnect.repository")
@EnableCaching
public class PetConnectApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PetConnectApplication.class);
        app.addInitializers(new EnvInitializer());
        app.run(args);
    }

}