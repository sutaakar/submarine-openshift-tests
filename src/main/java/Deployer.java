import java.net.URL;
import java.util.concurrent.TimeUnit;

import cz.xtf.builder.builders.ImageStreamBuilder;
import cz.xtf.core.openshift.OpenShiftBinary;
import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.openshift.api.model.ImageStream;

public class Deployer {

    public static HttpDeployment deployKaasUsingS2iAndWait(Project project, URL assetsUrl) {
        OpenShiftBinary masterBinary = OpenShifts.masterBinary(project.getName());

        String s2iApplicationBuildName = buildKaasS2iApplication(project, masterBinary, assetsUrl);
        String runtimeImageBuildName = buildKaasS2iRuntimeImage(project, masterBinary, s2iApplicationBuildName);

        masterBinary.execute("new-app", runtimeImageBuildName + ":latest");
        project.getMaster().waiters().areExactlyNPodsRunning(1, runtimeImageBuildName).interval(TimeUnit.MINUTES, 1L).waitFor();

        masterBinary.execute("expose", "svc/" + runtimeImageBuildName);

        // Temporary implementation, service name is equal to runtimeImageBuildName
        return new HttpDeployment(project, runtimeImageBuildName);
    }

    private static String buildKaasS2iApplication(Project project, OpenShiftBinary masterBinary, URL assetsUrl) {
        String s2iImageStreamName = "kaas-builder-s2i-image";
        String s2iImageStreamTag = "1.0";
        String resultBuildName = "kaas-s2i";

        createInsecureImageStream(project, s2iImageStreamName, s2iImageStreamTag, TestConfig.getKaasS2iBuilderImage());

        masterBinary.execute("new-build", s2iImageStreamName + ":" + s2iImageStreamTag + "~" + assetsUrl.toExternalForm(), "--name=" + resultBuildName);
        project.getMaster().waiters().hasBuildCompleted(resultBuildName).timeout(TimeUnit.MINUTES, 20L).waitFor();
        return resultBuildName;
    }

    private static String buildKaasS2iRuntimeImage(Project project, OpenShiftBinary masterBinary, String s2iApplicationBuildName) {
        String finalImageStreamName = "kaas-builder-image";
        String finalImageStreamTag = "1.0";
        String resultBuildName = "kaas-runtime";

        createInsecureImageStream(project, finalImageStreamName, finalImageStreamTag, TestConfig.getKaasRuntimeImage());

        masterBinary.execute("new-build", "--name=" + resultBuildName, "--source-image=" + s2iApplicationBuildName, "--source-image-path=/home/submarine/bin:.", "--image-stream=" + finalImageStreamName + ":" + finalImageStreamTag);
        project.getMaster().waiters().hasBuildCompleted(resultBuildName).timeout(TimeUnit.MINUTES, 2L).waitFor();
        return resultBuildName;
    }

    // Helper methods

    /**
     * Create image stream pointing to external image.
     *
     * @param master
     * @param name Image stream name
     * @param tag Image stream tag
     * @param externalImage External image URL.
     */
    private static void createInsecureImageStream(Project project, String name, String tag, String externalImage) {
        ImageStream s2iImageStream = new ImageStreamBuilder(name).addTag(tag, externalImage, true).build();
        project.getMaster().createImageStream(s2iImageStream);
    }
}
