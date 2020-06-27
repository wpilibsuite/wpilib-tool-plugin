package edu.wpi.first.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public class ExtractEmbeddedWindowsHelpers extends DefaultTask {
    private final RegularFileProperty outputFile;

    @OutputFile 
    public RegularFileProperty getOutputFile() {
        return outputFile;
    }


    @Inject
    public ExtractEmbeddedWindowsHelpers(ObjectFactory factory) {
        outputFile = factory.fileProperty();
        getOutputs().file(outputFile);
        outputFile.set(getProject().getLayout().getBuildDirectory().file("WindowsLoaderHelper.dll"));
        setGroup("Tool Helpers");
        setDescription("Extracts the native windows helper libraries");
    }

    private static boolean is32BitIntel() {
        String arch = System.getProperty("os.arch");
        return "x86".equals(arch) || "i386".equals(arch);
    }

    @TaskAction
    public void extract() throws IOException {
        File resolvedFile = outputFile.getAsFile().get();

        String streamFileName = "/x86-64/WindowsLoaderHelper.dll";
        if (is32BitIntel()) {
            streamFileName = "/x86/WindowsLoaderHelper.dll";
        }
        
        try (InputStream is = ExtractEmbeddedWindowsHelpers.class.getResourceAsStream(streamFileName)) {
            Files.copy(is, resolvedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }


}
