package net.adoptopenjdk.icedteaweb.image;

import net.adoptopenjdk.icedteaweb.jdk89access.SunMiscLauncher;

import javax.swing.ImageIcon;

public enum ImageGallery {
    INFO("net/adoptopenjdk/icedteaweb/image/info64.png"),
    QUESTION("net/adoptopenjdk/icedteaweb/image/question64.png"),
    WARNING("net/adoptopenjdk/icedteaweb/image/warn64.png"),
    INFO_SMALL("net/adoptopenjdk/icedteaweb/image/info32.png"),
    QUESTION_SMALL("net/adoptopenjdk/icedteaweb/image/question32.png"),
    WARNING_SMALL("net/adoptopenjdk/icedteaweb/image/warn32.png"),
    LOGIN("net/adoptopenjdk/icedteaweb/image/login64.png");

    private String path;

    ImageGallery(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }

    public ImageIcon asImageIcon() {
        return SunMiscLauncher.getSecureImageIcon(path);
    }
}
