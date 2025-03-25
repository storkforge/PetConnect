package se.storkforge.petconnect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;


import java.util.List;
import java.util.Locale;

@Configuration
public class LocaleConfig {

    @Bean
    public LocaleResolver localeResolver() {

        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.US);

        localeResolver.setSupportedLocales(List.of(
                Locale.ENGLISH,                 // en
                Locale.of("es"),       // Spanish
                Locale.of("fr"),      // French
                Locale.of("sv"),     // Swedish
                Locale.of("ar"),    // Arabic
                Locale.of("hi")    // Hindi
        ));
        return localeResolver;
    }
}