import java.io.File;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0 || "--gui".equals(args[0])) {
            SwingUtilities.invokeLater(() -> {
                ImageConverterGUI gui = new ImageConverterGUI();
                gui.setVisible(true);
            });
            return;
        }

        if ("--help".equals(args[0]) || "-h".equals(args[0])) {
            printHelp();
            return;
        }

        float quality = 0.90f;
        for (int i = 0; i < args.length - 1; i++) {
            if ("--quality".equals(args[i]) || "-q".equals(args[i])) {
                try {
                    quality = Integer.parseInt(args[i + 1]) / 100.0f;
                } catch (NumberFormatException e) {
                    System.err.println("Invalid quality: " + args[i + 1]);
                    System.exit(1);
                }
            }
        }

        ImageConverter converter = new ImageConverter(quality);

        if ("--batch".equals(args[0]) || "-b".equals(args[0])) {
            if (args.length < 4) {
                System.err.println("Batch usage: --batch <inputDir> <format> <outputDir>");
                System.exit(1);
            }
            try {
                int count = converter.convertDirectory(
                    new File(args[1]), 
                    new File(args[3]), 
                    ImageFormat.fromExtension(args[2])
                );
                System.out.println("Converted: " + count);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                System.exit(1);
            }
            return;
        }

        if (args.length < 2) {
            System.err.println("Usage: <input> <format> [output]");
            System.exit(1);
        }

        try {
            File input = new File(args[0]);
            ImageFormat format = ImageFormat.fromExtension(args[1]);
            File output = (args.length >= 3 && !args[2].startsWith("-")) ? new File(args[2]) : null;

            if (output != null) {
                converter.convert(input, output, format);
                System.out.println("Saved: " + output.getAbsolutePath());
            } else {
                File res = converter.convertInPlace(input, format);
                System.out.println("Saved: " + res.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void printHelp() {
        System.out.println("Image Converter Tool");
        System.out.println("Usage:");
        System.out.println("  java -jar ImageConverter.jar            (Opens GUI mode)");
        System.out.println("  java -jar ImageConverter.jar --gui      (Opens GUI mode)");
        System.out.println("  java -jar ImageConverter.jar <input_file> <format> [output_file]");
        System.out.println("  java -jar ImageConverter.jar --batch <input_dir> <format> <output_dir>");
        System.out.println("Options:");
        System.out.println("  -q, --quality <0-100>   JPEG compression quality");
        System.out.println("Formats: png, jpg, jpeg, bmp, gif, webp, tiff");
    }
}
