package edu.wpi.first.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

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
                if (!file.isFile()) {
                    continue;
                }
                project.exec((ex) -> {
                    ex.commandLine("strip", "--strip-all", "--discard-all", file.toString());
                });
            }
        }

        if (OperatingSystem.current().isMacOsX()) {
            // Set rpath correctly in all binaries
            Directory directory = outputDirectory.get();

            List<File> allFilesList = new ArrayList<>();
            List<String> fileNameList = new ArrayList<>();
            for (File file : directory.getAsFileTree()) {
                if (!file.isFile()) {
                    continue;
                }
                allFilesList.add(file);
                fileNameList.add(file.getName());
            }

            List<String> filesToFixup = new ArrayList<>();

            for (File file : allFilesList) {

                // Get list of all dependent binaries
                ByteArrayOutputStream standardOutput = new ByteArrayOutputStream();

                project.exec((ex) -> {
                    ex.commandLine("otool", "-L", file.toString());
                    ex.setStandardOutput(standardOutput);
                });

                filesToFixup.clear();

                String outputStr = standardOutput.toString();
                String currentFileName = file.getName();

                // See if any extracted files exist in deps list
                for (String lookupFile : fileNameList) {
                    if (lookupFile.equals(currentFileName)) {
                        continue;
                    }

                    if (outputStr.contains(lookupFile)) {
                        filesToFixup.add(lookupFile);
                    }
                }

                // Fixup any dependencies
                for (String fixupFile : filesToFixup) {
                    project.exec((ex) -> {
                        ex.commandLine("install_name_tool", "-change", fixupFile, "@loader_path/" + fixupFile, file.toString());
                    });
                }
            }
        }
    }
}
