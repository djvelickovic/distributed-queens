package com.crx.kids.project.node;

import com.crx.kids.project.common.NodeInfo;
import com.crx.kids.project.node.common.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@org.springframework.context.annotation.Configuration
@EnableAsync
@EnableSwagger2
public class NodeApplication {

    private static final Logger logger = LoggerFactory.getLogger(NodeApplication.class);

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }

    // bootstrap port
    // bootsrap address
    public static void main(String[] args) {
        Configuration.bootstrap = new NodeInfo(System.getProperty("bootstrap.addr"), Integer.parseInt(System.getProperty("bootstrap.port")));
        Configuration.myself = new NodeInfo(System.getProperty("server.address"), Integer.parseInt(System.getProperty("server.port")));

        logger.info("I m node {}. first of my name..", Configuration.myself);
        logger.info("Bootstrap configuration {}..", Configuration.bootstrap);

        SpringApplication.run(NodeApplication.class, args);

    }
}
