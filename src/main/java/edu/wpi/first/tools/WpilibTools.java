package edu.wpi.first.tools;

import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.DIRECTORY_TYPE;
import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.ZIP_TYPE;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.transform.TransformSpec;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.internal.artifacts.transform.UnzipTransform;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.os.OperatingSystem;

public class WpilibTools implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        if (project.equals(project.getRootProject())) {
            if (OperatingSystem.current().isWindows()) {
                project.getTasks().register("extractEmbeddedWindowsHelpers", ExtractEmbeddedWindowsHelpers.class);
            }
        }

        TaskProvider<ExtractConfiguration> extractConfig = project.getTasks().register("extractNativeConfiguration",
                ExtractConfiguration.class);
        TaskProvider<FixupNativeResources> fixupTask = project.getTasks().register("fixupNativeResources",
                FixupNativeResources.class);
        fixupTask.configure((FixupNativeResources fixup) -> {
            fixup.dependsOn(extractConfig);
            fixup.getInputDirectory().set(extractConfig.get().getOutputDirectory());
        });

        TaskProvider<HashNativeResources> hashTask = project.getTasks().register("hashNativeResources",
                HashNativeResources.class);
        hashTask.configure((HashNativeResources hash) -> {
            hash.dependsOn(fixupTask);
            hash.getInputDirectory().set(fixupTask.get().getOutputDirectory());
        });

        TaskProvider<AssembleNativeResources> assembleResourcesTask = project.getTasks()
                .register("assembleNativeResources", AssembleNativeResources.class);
        assembleResourcesTask.configure((AssembleNativeResources copy) -> {
            copy.from(fixupTask.get());
            copy.from(hashTask.get());

            copy.into(project.getLayout().getBuildDirectory().dir("AssembledResources"));
        });

        Attribute<String> artifactType = Attribute.of("artifactType", String.class);

        project.getDependencies().registerTransform(UnzipTransform.class, (TransformSpec transform) -> {
           transform.getFrom().attribute(artifactType, ZIP_TYPE);
           transform.getTo().attribute(artifactType, DIRECTORY_TYPE);
        });

        project.getExtensions().create("wpilibTools", WpilibToolsExtension.class, project, extractConfig, fixupTask, hashTask, assembleResourcesTask);        
    }
}