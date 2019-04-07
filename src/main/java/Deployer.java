import java.net.URL;
import java.util.concurrent.TimeUnit;

import cz.xtf.builder.builders.BuildConfigBuilder;
import cz.xtf.builder.builders.ImageStreamBuilder;
import cz.xtf.core.openshift.OpenShiftBinary;
import cz.xtf.core.openshift.OpenShifts;
import io.fabric8.openshift.api.model.ImageSourceBuilder;
import io.fabric8.openshift.api.model.ImageSourcePath;
import io.fabric8.openshift.api.model.ImageStream;

public class Deployer {

    /**
     * Deploy KaaS application into the project using S2I and wait until application starts.
     *
     * @param project Project where the application will be deployed to.
     * @param assetsUrl URL pointing to the GIT repo containing Kie assets.
     * @return Deployment object containing reference to the deployed application URL.
     */
    public static HttpDeployment deployKaasUsingS2iAndWait(Project project, URL assetsUrl) {
        OpenShiftBinary masterBinary = OpenShifts.masterBinary(project.getName());

        String s2iResultImageStreamName = buildKaasS2iApplication(project, assetsUrl);
        String runtimeImageBuildName = buildKaasS2iRuntimeImage(project, s2iResultImageStreamName);

        masterBinary.execute("new-app", runtimeImageBuildName + ":latest");
        project.getMaster().waiters().areExactlyNPodsRunning(1, runtimeImageBuildName).interval(TimeUnit.MINUTES, 1L).waitFor();

        masterBinary.execute("expose", "svc/" + runtimeImageBuildName);

        // Temporary implementation, service name is equal to runtimeImageBuildName
        return new HttpDeployment(project, runtimeImageBuildName);
    }

    /**
     * Build the KaaS application image and push it to an image stream.
     *
     * @param project
     * @param assetsUrl URL pointing to the GIT repo containing Kie assets.
     * @return Name of the image stream containing application image.
     */
    private static String buildKaasS2iApplication(Project project, URL assetsUrl) {
        String s2iImageStreamName = "kaas-builder-s2i-image";
        String s2iImageStreamTag = "1.0";
        String buildName = "kaas-s2i-build";
        String resultImageStreamName = "kaas-s2i";

        createInsecureImageStream(project, s2iImageStreamName, s2iImageStreamTag, TestConfig.getKaasS2iBuilderImage());
        createEmptyImageStream(project, resultImageStreamName);

        BuildConfigBuilder s2iConfigBuilder = new BuildConfigBuilder(buildName);
        s2iConfigBuilder.setOutput(resultImageStreamName);
        s2iConfigBuilder.gitSource(assetsUrl.toExternalForm());
        s2iConfigBuilder.sti().fromImageStream(project.getName(), s2iImageStreamName, s2iImageStreamTag);

        project.getMaster().createBuildConfig(s2iConfigBuilder.build());
        project.getMaster().startBuild(buildName);
        project.getMaster().waiters().hasBuildCompleted(buildName).timeout(TimeUnit.MINUTES, 20L).waitFor();
        return resultImageStreamName;
    }

    /**
     * Build runtime image of the KaaS application and push it to an image stream.
     *
     * @param project
     * @param s2iResultImageStreamName Image stream name containing KaaS application created by S2I build.
     * @return Name of the image stream containing runtime image.
     */
    private static String buildKaasS2iRuntimeImage(Project project, String s2iResultImageStreamName) {
        String finalImageStreamName = "kaas-builder-image";
        String finalImageStreamTag = "1.0";
        String buildName = "kaas-runtime-build";
        String resultImageStreamName = "kaas-runtime";

        createInsecureImageStream(project, finalImageStreamName, finalImageStreamTag, TestConfig.getKaasRuntimeImage());
        createEmptyImageStream(project, resultImageStreamName);

        // XTF has a bug, cannot create the build without GIT or binary source. Using Fabric8 to create the build.
        io.fabric8.openshift.api.model.ImageSource imageSource = new ImageSourceBuilder().withNewFrom()
                                                                                             .withKind("ImageStreamTag")
                                                                                             .withName(s2iResultImageStreamName + ":latest")
                                                                                             .withNamespace(project.getName())
                                                                                         .endFrom()
                                                                                         .withPaths(new ImageSourcePath(".", "/home/submarine/bin"))
                                                                                         .build();

        io.fabric8.openshift.api.model.BuildConfig runtimeConfig = new io.fabric8.openshift.api.model.BuildConfigBuilder().withNewMetadata()
                                                                                                                              .withName(buildName)
                                                                                                                          .endMetadata()
                                                                                                                          .withNewSpec()
                                                                                                                              .withNewOutput()
                                                                                                                                  .withNewTo()
                                                                                                                                      .withKind("ImageStreamTag")
                                                                                                                                      .withName(resultImageStreamName + ":latest")
                                                                                                                                  .endTo()
                                                                                                                              .endOutput()
                                                                                                                              .withNewSource()
                                                                                                                                  .withType("Image")
                                                                                                                                  .withImages(imageSource)
                                                                                                                              .endSource()
                                                                                                                              .withNewStrategy()
                                                                                                                                  .withType("Source")
                                                                                                                                  .withNewSourceStrategy()
                                                                                                                                      .withNewFrom()
                                                                                                                                          .withKind("ImageStreamTag")
                                                                                                                                          .withName(finalImageStreamName + ":" + finalImageStreamTag)
                                                                                                                                          .withNamespace(project.getName())
                                                                                                                                      .endFrom()
                                                                                                                                  .endSourceStrategy()
                                                                                                                              .endStrategy()
                                                                                                                          .endSpec()
                                                                                                                          .build();

        project.getMaster().createBuildConfig(runtimeConfig);
        project.getMaster().startBuild(buildName);
        project.getMaster().waiters().hasBuildCompleted(buildName).timeout(TimeUnit.MINUTES, 2L).waitFor();
        return resultImageStreamName;
    }

    // Helper methods

    /**
     * Create image stream pointing to external image.
     *
     * @param project
     * @param name Image stream name
     * @param tag Image stream tag
     * @param externalImage External image URL.
     */
    private static void createInsecureImageStream(Project project, String name, String tag, String externalImage) {
        ImageStream s2iImageStream = new ImageStreamBuilder(name).addTag(tag, externalImage, true).build();
        project.getMaster().createImageStream(s2iImageStream);
    }

    /**
     * Create empty image stream. Can be used as a target image stream for S2I build.
     *
     * @param project
     * @param name Image stream name
     */
    private static void createEmptyImageStream(Project project, String name) {
        ImageStream imageStream = new ImageStreamBuilder(name).build();
        project.getMaster().createImageStream(imageStream);
    }
}
