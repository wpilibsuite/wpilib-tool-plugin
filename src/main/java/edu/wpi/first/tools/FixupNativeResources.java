package edu.wpi.first.tools;

import java.io.File;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.os.OperatingSystem;

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
        Project project = getProject();

        getProject().copy(new Action<CopySpec>() {

            @Override
            public void execute(CopySpec copySpec) {
                copySpec.from(inputDirectory);
                copySpec.into(outputDirectory);
            }

        });

        if (OperatingSystem.current().isLinux()) {
            // Strip all binaries
            Directory directory = outputDirectory.get();
            for (File file : directory.getAsFileTree()) {
                project.exec((ex) -> {
                    ex.commandLine("strip", "--strip-all", "--discard-all", file.toString());
                });
            }
        }

        // TODO Platform specific fixups
    }

}
