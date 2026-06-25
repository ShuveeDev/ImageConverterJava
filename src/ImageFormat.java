import java.util.Arrays;

public enum ImageFormat {
    PNG("png"),
    JPG("jpg"),
    JPEG("jpeg"),
    BMP("bmp"),
    GIF("gif"),
    WEBP("webp"),
    TIFF("tiff");

    private final String extension;

    ImageFormat(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    public static ImageFormat fromExtension(String extension) {
        String ext = extension.startsWith(".") ? extension.substring(1) : extension;
        for (ImageFormat format : values()) {
            if (format.extension.equalsIgnoreCase(ext)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Unsupported format: " + extension);
    }

    public String getImageIOFormat() {
        if (this == JPG || this == JPEG) {
            return "jpeg";
        }
        return extension.toLowerCase();
    }
}
