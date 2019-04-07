import java.net.MalformedURLException;
import java.net.URL;

import io.restassured.RestAssured;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExperimentalIntegrationTest {

    private static Project project;
    private static HttpDeployment kaasDeloyment;

    @BeforeClass
    public static void setUp() throws MalformedURLException {
        URL assetsUrl = new URL("https://github.com/kiegroup/submarine-examples");
        String gitContextDir = "jbpm-quarkus-example";

        String randomProjectName = RandomStringUtils.randomAlphanumeric(4).toLowerCase();
        project = Project.create(randomProjectName);
        kaasDeloyment = Deployer.deployKaasUsingS2iAndWait(project, assetsUrl, gitContextDir);
    }

    @AfterClass
    public static void tearDown() {
        project.delete();
    }

    @Test
    public void testOrdersCrud() {
      RestAssured.given()
          .header("Content-Type", "application/json")
          .body("{\"approver\" : \"john\", \"order\" : {\"orderNumber\" : \"12345\", \"shipped\" : false}}")
      .when()
          .post(kaasDeloyment.getRouteUrl().toExternalForm() + "/orders")
      .then()
          .statusCode(200);
    }
}
