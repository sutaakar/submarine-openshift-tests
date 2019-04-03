import java.net.MalformedURLException;
import java.net.URL;

import io.fabric8.openshift.api.model.Route;

public class HttpDeployment {

    private Project project;
    private String serviceName;

    public HttpDeployment(Project project, String serviceName) {
        this.project = project;
        this.serviceName = serviceName;
    }

    public URL getRouteUrl() {
        Route httpRoute = project.getMaster().getRoutes().stream()
                                         .filter(route -> route.getSpec().getTo().getName().equals(serviceName))
                                         .filter(route -> route.getSpec().getTls() == null)
                                         .findAny()
                                         .orElseThrow(() -> new RuntimeException("No HTTP route found for service " + serviceName));
        String routeUrl = "http://" + httpRoute.getSpec().getHost() + ":80";
        try {
            return new URL(routeUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error while converting route URL " + routeUrl + " to URL object.", e);
        }
    }
}
