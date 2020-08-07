package edu.wpi.first.tools;

import java.util.NoSuchElementException;

public enum NativePlatforms {
    WIN32("win32"),
    WIN64("win64"),
    MAC("mac64"),
    LINUX64("linux64"),
    LINUXAARCH("linuxaarch"),
    LINUXRASPBIAN("linuxarm32"),
    LINUXATHENA("linuxathena");


    private final String platformName;

    public String getPlatformName() {
        return platformName;
    }

    NativePlatforms(String platformName) {
        this.platformName = platformName;
    }

    public static NativePlatforms forName(String platformName) {
        for (NativePlatforms value : values()) {
            if (value.getPlatformName().equals(platformName)) {
                return value;
            }
        }
        throw new NoSuchElementException(platformName);
    }
}
