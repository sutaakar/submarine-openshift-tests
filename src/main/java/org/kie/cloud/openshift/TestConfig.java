package org.kie.cloud.openshift;

import cz.xtf.core.config.XTFConfig;

public class TestConfig {

    private static final String IMAGE_KAAS_QUARKUS_BUILDER_S2I = "image.kaas.quarkus.builder.s2i";
    private static final String IMAGE_KAAS_QUARKUS_RUNTIME = "image.kaas.quarkus.runtime";
    private static final String IMAGE_KAAS_SPRINGBOOT_BUILDER_S2I = "image.kaas.springboot.builder.s2i";
    private static final String IMAGE_KAAS_SPRINGBOOT_RUNTIME = "image.kaas.springboot.runtime";

    public static String getKaasS2iQuarkusBuilderImage() {
        return getMandatoryProperty(IMAGE_KAAS_QUARKUS_BUILDER_S2I);
    }

    public static String getKaasQuarkusRuntimeImage() {
        return getMandatoryProperty(IMAGE_KAAS_QUARKUS_RUNTIME);
    }

    public static String getKaasS2iSpringBootBuilderImage() {
        return getMandatoryProperty(IMAGE_KAAS_SPRINGBOOT_BUILDER_S2I);
    }

    public static String getKaasSpringBootRuntimeImage() {
        return getMandatoryProperty(IMAGE_KAAS_SPRINGBOOT_RUNTIME);
    }

    private static String getMandatoryProperty(String propertyName) {
        String propertyValue = XTFConfig.get(propertyName);
        if (propertyValue == null || propertyValue.isEmpty()) {
            throw new RuntimeException("Required property with name " + propertyName + " is not set.");
        }
        return propertyValue;
    }
}
