package edu.wpi.first.tools;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;


public class NativeConfigurator {
    private final PlatformMapper platformMapper;
    private String defaultJavaFxVersion = "11";
    private String wpilibVersion = "+";
    private final DependencyHandler handler;

    public NativeConfigurator(PlatformMapper mapper, DependencyHandler handler) {
        this.platformMapper = mapper;
        this.handler = handler;
    }

    // public Dependency add(DependencyHandler handler, NativePlatforms platform, Object dependencyNotation) {
    //     return handler.add(platform.getPlatformName(), dependencyNotation);
    // }

    // public Configuration createNativeConfiguration(Project project) {
    //     NativePlatforms currentPlatform = platformMapper.getCurrentPlatform();
    //     Configuration configuration = project.getConfigurations().create(currentPlatform.getPlatformName());
    //     Configuration wpilibConfiguration = project.getConfigurations().create(currentPlatform.getPlatformName() + "-wpilib");
    //     project.getConfigurations().getByName("compileOnly").extendsFrom(configuration);
    //     project.getConfigurations().getByName("runtimeOnly").extendsFrom(configuration);
    //     project.getConfigurations().getByName("testCompile").extendsFrom(configuration);
    //     return wpilibConfiguration;
    // }

    public Dependency wpilibJava(String name) {
        return wpilibJava(name, wpilibVersion);
    }

    public Dependency wpilibJava(String name, String version) {
        return handler.create("edu.wpi.first." + name + ":" + name + "-java:" + version);
    }

    public Dependency wpilib(String name, String version) {
        return handler.create("edu.wpi.first." + name + ":" + name + "-cpp:" + version + ":" + platformMapper.getWpilibClassifier() + "@zip");
    }

    public Dependency wpilib(String name) {
        return wpilib(name, this.wpilibVersion);
    }

    public Dependency cscore(String version) {
        return handler.create("edu.wpi.first.cscore:cscore-jnicvstatic:" + version + ":" + platformMapper.getWpilibClassifier()  + "@zip");
    }

    public Dependency cscore() {
        return cscore(this.wpilibVersion);
    }

    public Dependency javafx(String name, String version) {
        NativePlatforms platform = platformMapper.getCurrentPlatform();
        String groupName = "org.openjfx";
        if (platform.equals(NativePlatforms.WIN32)) {
            groupName = "edu.wpi.first.openjfx";
        }
        return handler.create(groupName + ":javafx-" + name + ":" + version + ":" + platformMapper.getJavaFxClassifier());
    }

    public Dependency javafx(String name) {
        return javafx(name, defaultJavaFxVersion);
    }

    public void setDefaultJavaFxVersion(String version) {
        defaultJavaFxVersion = version;
    }

    public void setWpilibVersion(String version) {
        this.wpilibVersion = version;
    }

    // public static Dependency nativeDependency(DependencyHandler handler, String group, String name, String version, Function<NativePlatforms, String> classiferFunction) {
    //     NativePlatforms currentPlatform = PlatformMapper.getCurrentPlatform();
    //     return handler.add(currentPlatform.getPlatformName(),
    //         group + ":" + name + ":" + version + ":" + classiferFunction.apply(currentPlatform));
    // }
}
