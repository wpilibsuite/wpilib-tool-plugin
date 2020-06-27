package edu.wpi.first.tools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;

public class WpilibToolsExtension {
    private final Project project;
    private final Set<Configuration> configurations;
    private final TaskProvider<ExtractConfiguration> extractConfigTask;
    private final TaskProvider<FixupNativeResources> fixupTask;
    private final TaskProvider<HashNativeResources> hashTask;
    private final TaskProvider<AssembleNativeResources> assembleTask;
    private final PlatformMapper platformMapper;
    private final NativeConfigurator nativeConfigurator;
    private final JavaFxHelpers javaFxHelpers;

    @Inject
    public WpilibToolsExtension(Project project,
        TaskProvider<ExtractConfiguration> extractConfigTask,
        TaskProvider<FixupNativeResources> fixupTask,
        TaskProvider<HashNativeResources> hashTask,
        TaskProvider<AssembleNativeResources> assembleTask) {
        this.project = project;
        this.extractConfigTask = extractConfigTask;
        this.fixupTask = fixupTask;
        this.hashTask = hashTask;
        this.assembleTask = assembleTask;
        this.platformMapper = new PlatformMapper();
        this.nativeConfigurator = new NativeConfigurator(platformMapper);
        this.javaFxHelpers = new JavaFxHelpers(platformMapper);

        configurations = new HashSet<>();
    }

    public void createNativeConfigurations() {
        configurations.add(nativeConfigurator.createNativeConfiguration(project));
    }

    public void addNativeResourcesToSourceSet(SourceSet sourceSet) {
        Map<String, Object> map = new HashMap<>();
        TaskProvider<AssembleNativeResources> resourcesTask = getAssembleResourcesTask();
        map.put("builtBy", resourcesTask);
        sourceSet.getOutput().dir(map, resourcesTask);
    }

    public void addExtraNativeResourceConfiguration(Configuration configuration) {
        configurations.add(configuration);
    }

    Set<Configuration> getConfigurations() {
        return configurations;
    }

    public TaskProvider<ExtractConfiguration> getExtractConfigurationTask() {
        return extractConfigTask;
    }

    public TaskProvider<FixupNativeResources> getFixupResourcesTask() {
        return fixupTask;
    }

    public TaskProvider<HashNativeResources> getHashResourcesTask() {
        return hashTask;
    }

    public TaskProvider<AssembleNativeResources> getAssembleResourcesTask() {
        return assembleTask;
    }

    public PlatformMapper getPlatformMapper() {
        return platformMapper;
    }

    public NativeConfigurator getDeps() {
        return nativeConfigurator;
    }

    public JavaFxHelpers getJavaFx() {
        return javaFxHelpers;
    }
}