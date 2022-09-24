package edu.wpi.first.tools;

import javax.inject.Inject;

import org.gradle.api.Project;

public class PlatformMapper {
    private NativePlatforms currentPlatform;
    private final Project project;

    @Inject
    public PlatformMapper(Project project) {
        this.project = project;
        getCurrentPlatform();
    }

    public synchronized NativePlatforms getCurrentPlatform() {
        if (currentPlatform != null) {
            return currentPlatform;
        }

        Object override = project.findProperty("ArchOverride");

        if (override != null) {
            System.out.println("Overwriting platform to " + override);
            currentPlatform = NativePlatforms.forName((String)override);
            return currentPlatform;
        }

        String osName = System.getProperty("os.name").toLowerCase();
        String os = "";

        if(osName.contains("windows")) {
            os = "win";
        } else if(osName.contains("mac")) {
            os = "mac";
        } else if(osName.contains("linux")) {
            os = "linux";
        } else {
            throw new UnsupportedOperationException("Unknown OS: " + osName);
        }

        String osArch = System.getProperty("os.arch");
        String arch = "";

        if(osArch.contains("x86_64") || osArch.contains("amd64")) {
            arch = "x64";
        } else if(osArch.contains("x86")) {
            arch = "x32";
        } else if(osArch.contains("arm64") || osArch.contains("aarch64")) {
            arch = "arm64";
        } else if(osArch.contains("arm")) {
            arch = "arm32";
        }else {
            throw new UnsupportedOperationException(osArch);
        }

        currentPlatform = NativePlatforms.forName(os + arch);
        return currentPlatform;
    }

    public String getWpilibClassifier() {
        NativePlatforms platform = getCurrentPlatform();
        switch(platform) {
            case WIN32: return "windowsx86";
            case WIN64: return "windowsx86-64";
            case MAC64: return "osxx86-64";
            case MACARM64: return "osxarm64";
            case LINUX64: return "linuxx86-64";
            case LINUXARM64: return "linuxarm64";
            case LINUXARM32: return "linuxarm32";
            case LINUXATHENA: return "linuxathena";
            default: throw new IllegalArgumentException();
        }
    }

    public String getJavaCppClassifier() {
        NativePlatforms platform = getCurrentPlatform();
        switch(platform) {
            case WIN32: return "windows-x86";
            case WIN64: return "windows-x86_64";
            case MAC64: return "macosx-x86_64";
            case MACARM64: return "macosx-arm64";
            case LINUX64: return "linux-x86_64";
            default: throw new IllegalArgumentException();
        }
    }

    public String getJavaFxClassifier() {
        NativePlatforms platform = getCurrentPlatform();
        switch(platform) {
            case WIN32: return "win32";
            case WIN64: return "win";
            case MAC64: return "mac";
            case MACARM64: return "mac-aarch64";
            case LINUX64: return "linux";
            default: throw new IllegalArgumentException();
        }
    }
}
