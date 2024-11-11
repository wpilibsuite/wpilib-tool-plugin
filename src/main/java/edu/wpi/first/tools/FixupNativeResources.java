package edu.wpi.first.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.Scanner;
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
            NativePlatforms currentPlat = getProject().getExtensions().getByType(WpilibToolsExtension.class).getCurrentPlatform();
            String stripCommand = "strip";
            if (currentPlat.equals(NativePlatforms.LINUXARM32)) {
                String localStripCommand = "armv6-bullseye-linux-gnueabihf-strip";
                try {
                    project.exec(ex -> {
                        ex.commandLine(localStripCommand);
                    });
                } catch (Exception ex) {
                    getLogger().warn("Strip for arm32 was not found. Skipping");
                    return;
                }
                stripCommand = localStripCommand;
            } else if (currentPlat.equals(NativePlatforms.LINUXARM64)) {
                String localStripCommand = "aarch64-bullseye-linux-gnu-strip";
                try {
                    project.exec(ex -> {
                        ex.commandLine(localStripCommand);
                    });
                } catch (Exception ex) {
                    getLogger().warn("Strip for arm64 was not found. Skipping");
                    return;
                }
                stripCommand = localStripCommand;
            }

            String fStripCommand = stripCommand;

            // Strip all binaries
            Directory directory = outputDirectory.get();
            for (File file : directory.getAsFileTree()) {
                if (!file.isFile()) {
                    continue;
                }
                project.exec((ex) -> {
                    ex.commandLine(fStripCommand, "--strip-all", "--discard-all", file.toString());
                });
            }
        }

        if (OperatingSystem.current().isMacOsX()) {
            // Set rpath correctly in all binaries
            Directory directory = outputDirectory.get();

            List<String> filesToFixup = new ArrayList<>();

            for (File file : directory.getAsFileTree()) {
                if (!file.isFile()) {
                    continue;
                }

                // Strip binaries
                project.exec((ex) -> {
                    ex.commandLine("strip", "-x", "-S", file.toString());
                });

                // Get list of all dependent binaries
                ByteArrayOutputStream standardOutput = new ByteArrayOutputStream();

                project.exec((ex) -> {
                    ex.commandLine("otool", "-L", file.toString());
                    ex.setStandardOutput(standardOutput);
                });

                filesToFixup.clear();

                String outputStr = standardOutput.toString();
                String currentFileName = file.getName();

                // Search dependencies list, look for any non absolute path resolved libraries
                try (Scanner stringScanner = new Scanner(outputStr)) {
                    String currentLine = null;
                    while (stringScanner.hasNextLine()) {
                        currentLine = stringScanner.nextLine();
                        if (currentLine.contains(currentFileName)) {
                            continue;
                        }

                        String trimmedLine = currentLine.trim();

                        if (trimmedLine.startsWith("/")) {
                            continue;
                        }

                        String libName = trimmedLine.split(" ")[0];
                        filesToFixup.add(libName);
                    }
                }

                // Fixup any dependencies
                for (String fixupFile : filesToFixup) {
                    String outputName = fixupFile;
                    // Handle the special case of opencv libraries already containing rpath
                    System.out.println(outputName);
                    if (outputName.startsWith("@rpath/")) {
                        System.out.println("Replaciong");
                        outputName = outputName.substring("@rpath/".length());
                    }
                    String outputNameFinal = outputName;
                    project.exec((ex) -> {
                        ex.commandLine("install_name_tool", "-change", fixupFile, "@loader_path/" + outputNameFinal,
                                file.toString());
                    });
                }

                // Overwrite signature because they were invalidated by strip and install-name-tool.
                project.exec((ex) -> {
                    ex.commandLine("codesign", "--force", "--sign",  "-", file.toString());
                    ex.setStandardOutput(standardOutput);
                });
            }
        }
    }

}
