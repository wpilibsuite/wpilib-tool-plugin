package edu.wpi.first.tools;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

public class FixupNativeResources extends DefaultTask {
    private DirectoryProperty inputDirectory;
    private DirectoryProperty outputDirectory;

    @InputDirectory
    public DirectoryProperty getInputDirectory() {
        return inputDirectory;
    }

    @OutputDirectory
    public DirectoryProperty getOutputDirectory() {
        return outputDirectory;
    }

    @Inject
    public FixupNativeResources() {
        ObjectFactory factory = getProject().getObjects();
        inputDirectory = factory.directoryProperty();
        outputDirectory = factory.directoryProperty();
        outputDirectory.set(getProject().getLayout().getBuildDirectory().dir("RuntimeLibs"));
    }

    @TaskAction
    public void execute() {
        getProject().copy(new Action<CopySpec>() {

            @Override
            public void execute(CopySpec copySpec) {
                copySpec.from(inputDirectory);
                copySpec.into(outputDirectory);
            }

        });

        // TODO Platform specific fixups
    }

}