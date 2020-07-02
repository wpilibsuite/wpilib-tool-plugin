package edu.wpi.first.tools;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;

public class WpilibToolsExtension {
    private final Project project;
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

    }

    public void createNativeConfigurations() {
        var cfg = nativeConfigurator.createNativeConfiguration(project);
        this.extractConfigTask.configure(c -> {
            c.getConfigurations().add(cfg);
        });
    }


    public void addNativeResourcesToSourceSet(SourceSet sourceSet) {
        Map<String, Object> map = new HashMap<>();
        TaskProvider<AssembleNativeResources> resourcesTask = getAssembleResourcesTask();
        map.put("builtBy", resourcesTask);
        sourceSet.getOutput().dir(map, resourcesTask);
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

    public class NewTaskSetConfiguration {
        public String taskPostfix;
        public String configurationName;
        public String rootTaskFolder;
        public String resourceFileName;
    }

    public class NewTaskSet {
        public TaskProvider<ExtractConfiguration> extractConfiguration;
        public TaskProvider<FixupNativeResources> fixup;
        public TaskProvider<HashNativeResources> hash;
        public TaskProvider<AssembleNativeResources> assemble;
    }

    public NewTaskSet createNewNativeExtractionConfiguration(Action<NewTaskSetConfiguration> configuration) {
        NewTaskSetConfiguration newConfig = new NewTaskSetConfiguration();
        configuration.execute(newConfig);

        Objects.requireNonNull(newConfig.taskPostfix);
        Objects.requireNonNull(newConfig.configurationName);
        Objects.requireNonNull(newConfig.rootTaskFolder);
        Objects.requireNonNull(newConfig.resourceFileName);

        final NewTaskSet retSet = new NewTaskSet();
        retSet.extractConfiguration = project.getTasks().register("extractNativeConfiguration" + newConfig.taskPostfix, ExtractConfiguration.class);
        retSet.extractConfiguration.configure(c -> {
            c.getConfigurations().add(project.getConfigurations().getByName(newConfig.configurationName));
            c.getOutputDirectory().set(new File(newConfig.rootTaskFolder, "RawRuntimeLibs"));
        });

        retSet.fixup = project.getTasks().register("fixupNativeResources" + newConfig.taskPostfix, FixupNativeResources.class);
        retSet.fixup.configure(c -> {
            c.dependsOn(retSet.extractConfiguration);
            c.getInputDirectory().set(retSet.extractConfiguration.get().getOutputDirectory());
            c.getOutputDirectory().set(new File(newConfig.rootTaskFolder, "RuntimeLibs"));
        });

        retSet.hash = project.getTasks().register("hashNativeResources" + newConfig.taskPostfix, HashNativeResources.class);
        retSet.hash.configure(c -> {
            c.dependsOn(retSet.fixup);
            c.getInputDirectory().set(retSet.fixup.get().getOutputDirectory());
            c.getHashFile().set(new File(newConfig.rootTaskFolder, newConfig.resourceFileName));
        });

        retSet.assemble = project.getTasks().register("assembleNativeResources" + newConfig.taskPostfix, AssembleNativeResources.class);
        retSet.assemble.configure(c -> {
            c.from(retSet.fixup.get());
            c.from(retSet.hash.get());
            c.into(new File(newConfig.rootTaskFolder, "AssembledResources"));
        });
        return retSet;
    }
}
