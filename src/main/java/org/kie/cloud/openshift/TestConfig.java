package org.kie.cloud.openshift;

import cz.xtf.core.config.XTFConfig;

public class TestConfig {

    private static final String IMAGE_KAAS_BUILDER_S2I = "image.kaas.builder.s2i";
    private static final String IMAGE_KAAS_RUNTIME = "image.kaas.runtime";

    public static String getKaasS2iBuilderImage() {
        return getMandatoryProperty(IMAGE_KAAS_BUILDER_S2I);
    }

    public static String getKaasRuntimeImage() {
        return getMandatoryProperty(IMAGE_KAAS_RUNTIME);
    }

    private static String getMandatoryProperty(String propertyName) {
        String propertyValue = XTFConfig.get(propertyName);
        if (propertyValue == null || propertyValue.isEmpty()) {
            throw new RuntimeException("Required property with name " + propertyName + " is not set.");
        }
        return propertyValue;
    }
}
