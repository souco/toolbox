package cc.souco.toolbox.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfigurer implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "schemas");
        registry.addViewController("/schemas").setViewName("db/schemas");
        registry.addViewController("/tables").setViewName("db/tables");
    }
}
