package se.storkforge.petconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("se.storkforge.petconnect.entity")
@EnableJpaRepositories("se.storkforge.petconnect.repository")
public class PetConnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetConnectApplication.class, args);
    }
}