package se.storkforge.petconnect.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public void initialize(@NotNull ConfigurableApplicationContext context) {
        loadEnvFile("OpenAI.env");
        loadEnvFile("2auth.env");
    }

    private void loadEnvFile(String filename) {
        Dotenv dotenv = Dotenv.configure()
                .filename(filename)
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );
    }
}
