import java.net.MalformedURLException;
import java.net.URL;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExperimentalIntegrationTest {

    private static Project project;
    private static HttpDeployment kaasDeloyment;

    @BeforeClass
    public static void setUp() throws MalformedURLException {
        URL assetsUrl = new URL("https://github.com/mswiderski/sample-kaas-assets");

        project = Project.create("ksuta-subm2");
        kaasDeloyment = Deployer.deployKaasUsingS2iAndWait(project, assetsUrl);
    }

    @AfterClass
    public static void tearDown() {
        project.delete();
    }

    @Test
    public void testSomething() {
        RestAssured.given()
            .header("Content-Type", "application/json")
        .when()
            .post(kaasDeloyment.getRouteUrl().toExternalForm() + "/extras")
        .then()
            .statusCode(200)
            .body("id", Matchers.greaterThan(0));
    }
}
