package org.kie.cloud.openshift;

import cz.xtf.core.openshift.OpenShift;
import cz.xtf.core.openshift.OpenShifts;

public class Project {

    private String name;
    private OpenShift master;

    private Project(String name) {
        this.name = name;
        this.master = OpenShifts.master(name);
    }

    public String getName() {
        return name;
    }

    public OpenShift getMaster() {
        return master;
    }

    public void delete() {
        OpenShifts.master().deleteProject(name);
    }

    public static Project create(String name) {
        OpenShifts.master().createProjectRequest(name);
        return new Project(name);
    }
}
