package Gateway.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.web.servlet.function.RequestPredicates.path;
import static org.springframework.web.servlet.function.RouterFunctions.route;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouterFunction<ServerResponse> applicationServiceRoute() {
        return route(
                path("/api/applications/**"), // Predicate
                request -> {
                    // Forward the request to ApplicationService
                    // Using simple redirect for MVC Gateway
                    return ServerResponse.temporaryRedirect(
                            java.net.URI.create("http://localhost:8083" + request.path())
                    ).build();
                }
        );
    }
}
