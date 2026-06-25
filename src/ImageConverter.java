import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public class ImageConverter {

    private float jpegQuality = 0.90f;

    public ImageConverter() {
    }

    public ImageConverter(float jpegQuality) {
        setJpegQuality(jpegQuality);
    }

    public void setJpegQuality(float jpegQuality) {
        if (jpegQuality < 0f || jpegQuality > 1f) {
            throw new IllegalArgumentException("Quality must be 0.0 to 1.0");
        }
        this.jpegQuality = jpegQuality;
    }

    public void convert(File inputFile, File outputFile, ImageFormat format) throws IOException {
        if (!inputFile.exists() || !inputFile.isFile()) {
            throw new IOException("Input file not found: " + inputFile.getAbsolutePath());
        }

        BufferedImage image = ImageIO.read(inputFile);
        if (image == null) {
            throw new IOException("Unsupported or corrupted image: " + inputFile.getName());
        }

        File parent = outputFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        String ioFormat = format.getImageIOFormat();

        if (format == ImageFormat.JPG || format == ImageFormat.JPEG) {
            writeJpeg(image, outputFile);
        } else {
            BufferedImage toWrite = image;
            if (format == ImageFormat.BMP || format == ImageFormat.GIF || format == ImageFormat.TIFF) {
                toWrite = ensureOpaque(image);
            }
            boolean written = ImageIO.write(toWrite, ioFormat, outputFile);
            if (!written) {
                throw new IOException("No writer found for " + format);
            }
        }
    }

    public File convertInPlace(File inputFile, ImageFormat format) throws IOException {
        String name = stripExtension(inputFile.getName()) + "." + format.getExtension();
        File outputFile = new File(inputFile.getParent(), name);
        convert(inputFile, outputFile, format);
        return outputFile;
    }

    public int convertDirectory(File inputDir, File outputDir, ImageFormat format) throws IOException {
        if (!inputDir.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + inputDir);
        }
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("Cannot create output dir: " + outputDir);
        }

        File[] files = inputDir.listFiles(f -> {
            if (!f.isFile()) return false;
            int dot = f.getName().lastIndexOf('.');
            if (dot < 0) return false;
            try {
                ImageFormat.fromExtension(f.getName().substring(dot + 1));
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        });

        if (files == null || files.length == 0) {
            return 0;
        }

        int success = 0;
        for (File file : files) {
            try {
                File out = new File(outputDir, stripExtension(file.getName()) + "." + format.getExtension());
                convert(file, out, format);
                success++;
            } catch (IOException e) {
                System.err.println("Failed: " + file.getName() + " - " + e.getMessage());
            }
        }
        return success;
    }

    private void writeJpeg(BufferedImage image, File outputFile) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IOException("No JPEG writer");
        }
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(jpegQuality);

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(ensureOpaque(image), null, null), param);
        } finally {
            writer.dispose();
        }
    }

    private BufferedImage ensureOpaque(BufferedImage src) {
        if (src.getTransparency() == BufferedImage.OPAQUE) {
            return src;
        }
        BufferedImage opaque = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = opaque.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, opaque.getWidth(), opaque.getHeight());
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return opaque;
    }

    private String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot > 0) ? filename.substring(0, dot) : filename;
    }
}
