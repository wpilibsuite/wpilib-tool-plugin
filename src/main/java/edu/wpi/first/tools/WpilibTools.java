package edu.wpi.first.tools;

import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE;
import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.DIRECTORY_TYPE;
import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.JAR_TYPE;
import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.ZIP_TYPE;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;
import org.gradle.internal.os.OperatingSystem;

public class WpilibTools implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        if (OperatingSystem.current().isWindows()) {
            var extractTaskName = "extractEmbeddedWindowsHelpers";
            try {
                project.getRootProject().getTasks().named(extractTaskName);
            } catch (UnknownTaskException notfound) {
                project.getRootProject().getTasks().register(extractTaskName, ExtractEmbeddedWindowsHelpers.class);
            }
        }

        project.getDependencies().registerTransform(UnzipTransform.class, transform -> {
           transform.getFrom().attribute(ARTIFACT_TYPE_ATTRIBUTE, ZIP_TYPE);
           transform.getTo().attribute(ARTIFACT_TYPE_ATTRIBUTE, DIRECTORY_TYPE);
        });

        project.getDependencies().registerTransform(UnzipTransform.class, transform -> {
            transform.getFrom().attribute(ARTIFACT_TYPE_ATTRIBUTE, JAR_TYPE);
            transform.getTo().attribute(ARTIFACT_TYPE_ATTRIBUTE, DIRECTORY_TYPE);
         });

        project.getExtensions().create("wpilibTools", WpilibToolsExtension.class, project);
    }
}
