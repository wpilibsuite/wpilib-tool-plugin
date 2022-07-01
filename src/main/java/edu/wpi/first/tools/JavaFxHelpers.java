package edu.wpi.first.tools;

import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;

public class JavaFxHelpers {
    private final PlatformMapper platformMapper;
    private String defaultVersion = "11";

    public JavaFxHelpers(PlatformMapper platformMapper) {
        this.platformMapper = platformMapper;
    }

    public Dependency add(DependencyHandler handler, String name, String version) {
        NativePlatforms platform = platformMapper.getCurrentPlatform();
        String groupName = "org.openjfx";
        return handler.add(platform.getPlatformName(),
            groupName + ":javafx-" + name + ":" + version + ":" + platformMapper.getJavaFxClassifier());
    }

    public Dependency add(DependencyHandler handler, String name) {
        return add(handler, name, defaultVersion);
    }

    public void setDefaultVersion(String version) {
        defaultVersion = version;
    }
}
