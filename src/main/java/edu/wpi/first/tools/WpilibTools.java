package edu.wpi.first.tools;

import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.DIRECTORY_TYPE;
import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.JAR_TYPE;
import static org.gradle.api.artifacts.type.ArtifactTypeDefinition.ZIP_TYPE;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.internal.artifacts.transform.UnzipTransform;
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

        Attribute<String> artifactType = Attribute.of("artifactType", String.class);

        project.getDependencies().registerTransform(UnzipTransform.class, transform -> {
           transform.getFrom().attribute(artifactType, ZIP_TYPE);
           transform.getTo().attribute(artifactType, DIRECTORY_TYPE);
        });

        project.getDependencies().registerTransform(UnzipTransform.class, transform -> {
            transform.getFrom().attribute(artifactType, JAR_TYPE);
            transform.getTo().attribute(artifactType, DIRECTORY_TYPE);
         });

        project.getExtensions().create("wpilibTools", WpilibToolsExtension.class, project);
    }
}
