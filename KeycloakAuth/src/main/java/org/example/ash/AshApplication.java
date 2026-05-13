package org.example.ash;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@EnableScheduling
//@EnableFeignClients(basePackages = "org.example.ash.client")
@EnableCaching
@EnableJpaAuditing
//@EnableRetry
@Slf4j
public class AshApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AshApplication.class);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartUp(env);
    }

    private static void logApplicationStartUp(Environment environment) {
        String protocol = "http";
        String serverPort = environment.getProperty("server.port");
        String contextPath = environment.getProperty("server.servlet.context-path");
        if (StringUtils.isBlank(contextPath)) {
            contextPath = "/";
        }

        String hostAddress = "localhost";

        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
        }

        log.info("\n----------------------------------\n\t" +
                        "Application '{}' is running! Access URLs: \n\t" +
                        "Local: \t\t{}://localhost:{}{}\n\t" +
                        "External: \t\t{}://{}:{}{}\n\t" +
                        "Profile(s): \t\t{}\n\t",
                environment.getProperty("spring.application.name"),
                protocol, serverPort, contextPath,
                protocol, hostAddress, serverPort, contextPath,
                environment.getActiveProfiles()
        );
    }

}
