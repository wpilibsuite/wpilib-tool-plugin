package edu.wpi.first.tools;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.artifacts.ArtifactView;
import org.gradle.api.artifacts.ArtifactView.ViewConfiguration;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.artifacts.ArtifactAttributes;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.os.OperatingSystem;

public class ExtractConfiguration extends Copy {
    private final ArtifactViewAction viewAction = new ArtifactViewAction();

    private class AttributeContainerAction implements Action<AttributeContainer> {

        @Override
        public void execute(AttributeContainer attribute) {
            attribute.attribute(ArtifactAttributes.ARTIFACT_FORMAT, ArtifactTypeDefinition.DIRECTORY_TYPE);
        }

    }

    private class ArtifactViewAction implements Action<ViewConfiguration> {
        private final AttributeContainerAction containerAction = new AttributeContainerAction();

        @Override
        public void execute(ViewConfiguration view) {
            view.attributes(containerAction);
        }
    }

    private DirectoryProperty outputDirectory;

    @OutputDirectory
    public DirectoryProperty getOutputDirectory() {
        return outputDirectory;
    }

    @Inject
    public ExtractConfiguration() {
        WpilibToolsExtension extension = getProject().getExtensions().getByType(WpilibToolsExtension.class);

        outputDirectory = getProject().getObjects().directoryProperty();

        outputDirectory.set(getProject().getLayout().getBuildDirectory().dir("RawRuntimeLibs"));

        getOutputs().dir(outputDirectory);

        into(outputDirectory);

        from(new Callable<FileCollection>() {

            @Override
            public FileCollection call() {
                FileCollection collection = null;
                for (Configuration config : extension.getConfigurations()) {
                    ArtifactView view = config.getIncoming().artifactView(viewAction);
                    FileCollection localCollection = view.getFiles();
                    if (collection == null) {
                        collection = localCollection;
                    } else {
                        collection = collection.plus(localCollection);
                    }
                }
                return collection;
            }

        });

        if (OperatingSystem.current().isWindows()) {
            TaskProvider<Task> extractTask = getProject().getRootProject().getTasks().named("extractEmbeddedWindowsHelpers");
            
            dependsOn(extractTask);
            ExtractEmbeddedWindowsHelpers resolvedExtractTask = (ExtractEmbeddedWindowsHelpers)extractTask.get();
            from(resolvedExtractTask.getOutputFile(), (CopySpec copy) -> {
                String arch = "x86-64";
                copy.into("/windows/" + arch + "/shared");
            });
        }

        include("**/*.so");
        include("**/*.so.*");
        include("**/*.dll");
        include("**/*.dylib");

        exclude("**/*.so.debug");
    }
}