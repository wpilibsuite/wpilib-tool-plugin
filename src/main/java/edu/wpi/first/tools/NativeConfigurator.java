package edu.wpi.first.tools;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;


public class NativeConfigurator {
    private final PlatformMapper platformMapper;
    private String defaultJavaFxVersion = "11";
    private String wpilibVersion = "+";

    public NativeConfigurator(PlatformMapper mapper) {
        this.platformMapper = mapper;
    }

    public Dependency add(DependencyHandler handler, NativePlatforms platform, Object dependencyNotation) {
        return handler.add(platform.getPlatformName(), dependencyNotation);
    }

    public Configuration createNativeConfiguration(Project project) {
        NativePlatforms currentPlatform = platformMapper.getCurrentPlatform();
        Configuration configuration = project.getConfigurations().create(currentPlatform.getPlatformName());
        Configuration wpilibConfiguration = project.getConfigurations().create(currentPlatform.getPlatformName() + "-wpilib");
        project.getConfigurations().getByName("compileOnly").extendsFrom(configuration);
        project.getConfigurations().getByName("runtimeOnly").extendsFrom(configuration);
        project.getConfigurations().getByName("testCompile").extendsFrom(configuration);
        return wpilibConfiguration;
    }

    public String wpilibJava(String name) {
        return wpilibJava(name, wpilibVersion);
    }

    public String wpilibJava(String name, String version) {
        return "edu.wpi.first." + name + ":" + name + "-java:" + version;
    }

    public Dependency wpilib(DependencyHandler handler, String name, String version) {
        NativePlatforms platform = platformMapper.getCurrentPlatform();
        return handler.add(platform.getPlatformName() + "-wpilib",  "edu.wpi.first." + name + ":" + name + "-cpp:" + version + ":" + platformMapper.getWpilibClassifier() + "@zip");
    }

    public Dependency wpilib(DependencyHandler handler, String name) {
        return wpilib(handler, name, this.wpilibVersion);
    }

    public Dependency wpilibConfig(DependencyHandler handler, String configurationName, String name) {
        return wpilibConfig(handler, configurationName, name, this.wpilibVersion);
    }

    public Dependency wpilibConfig(DependencyHandler handler, String configurationName, String name, String version) {
        return handler.add(configurationName,  "edu.wpi.first." + name + ":" + name + "-cpp:" + version + ":" + platformMapper.getWpilibClassifier() + "@zip");
    }



    public Dependency cscore(DependencyHandler handler, String version) {
        NativePlatforms platform = platformMapper.getCurrentPlatform();
        return handler.add(platform.getPlatformName() + "-wpilib",  "edu.wpi.first.cscore:cscore-jnicvstatic:" + version + ":" + platformMapper.getWpilibClassifier()  + "@zip");
    }

    public Dependency cscore(DependencyHandler handler) {
        return cscore(handler, this.wpilibVersion);
    }

    public Dependency cscoreConfig(DependencyHandler handler, String configurationName) {
        return cscoreConfig(handler, configurationName, this.wpilibVersion);
    }

    public Dependency cscoreConfig(DependencyHandler handler, String configurationName, String version) {
        return handler.add(configurationName,  "edu.wpi.first.cscore:cscore-jnicvstatic:" + version + ":" + platformMapper.getWpilibClassifier() + "@zip");
    }

    public Dependency javafx(DependencyHandler handler, String name, String version) {
        NativePlatforms platform = platformMapper.getCurrentPlatform();
        String groupName = "org.openjfx";
        if (platform.equals(NativePlatforms.WIN32)) {
            groupName = "edu.wpi.first.openjfx";
        }
        return handler.add(platform.getPlatformName(),
            groupName + ":javafx-" + name + ":" + version + ":" + platformMapper.getJavaFxClassifier());
    }

    public Dependency javafx(DependencyHandler handler, String name) {
        return javafx(handler, name, defaultJavaFxVersion);
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
