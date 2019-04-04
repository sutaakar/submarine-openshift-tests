# submarine-openshift-tests

This is just a temporary repo, will be deleted once test will be placed into proper place within Submarine repos.

## How to execute test

The integration test is currently bound to Surefire plugin. It can be executed as part of the build by running `mvn clean install`.
This is going to change, the test will be invoked probably by profile (due to external dependencies).
Another way is to run the test using IDE as usual JUnit test.

Test is using XTF framework for communication with OpenShift (which is based on Fabric8)

To properly configure OpenShift and related images user needs to specify these properties:
* xtf.openshift.url
* xtf.openshift.admin.username - Username of user with admin rights
* xtf.openshift.admin.password
* xtf.openshift.master.username - Username for user without admin rights (can be used admin user from above)
* xtf.openshift.master.password
* xtf.openshift.binary.path - Path to OC binary client (test/framework will be refactored in future to get rid of this)
* image.kaas.builder.s2i - tag for S2I image, for example buil from https://github.com/kiegroup/submarine-cloud/tree/master/s2i/kaas-quarkus-centos-s2i
* image.kaas.runtime - Tag for runtime image, for example built from https://github.com/kiegroup/submarine-cloud/tree/master/s2i/kaas-quarkus-centos

These properties need to be set as system property or they can be placed in test.properties file (with appropriate values). 
