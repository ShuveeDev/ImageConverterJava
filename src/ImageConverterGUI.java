import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class ImageConverterGUI extends JFrame {

    private final ImageConverter converter = new ImageConverter();
    private File selectedInput = null;
    private File selectedOutputDir = null;
    private boolean isBatchMode = false;
    private String currentLang = "UA";

    private MatrixRainPanel mainPanel;
    private JLabel titleLabel;
    private JButton fileBtn;
    private JLabel fileStatusLabel;
    private JComboBox<ImageFormat> formatCombo;
    private JSlider qualitySlider;
    private JLabel qualityValueLabel;
    private JPanel qualityPanel;
    private JButton outputDirBtn;
    private JLabel outputDirStatusLabel;
    private JButton convertBtn;
    private JLabel statusLabel;
    private JToggleButton singleModeBtn;
    private JToggleButton batchModeBtn;
    private JPanel authorPanel;
    
    private JLabel labelSource;
    private JLabel labelFormat;
    private JLabel labelQualityTitle;
    private JLabel labelDestination;

    private final Color COLOR_BG = new Color(5, 7, 5);
    private final Color COLOR_CARD = new Color(15, 20, 15, 220);
    private final Color COLOR_NEON = new Color(0, 255, 102);
    private final Color COLOR_NEON_DIM = new Color(0, 150, 60);
    private final Color COLOR_TEXT = new Color(200, 255, 210);
    private final Color COLOR_TEXT_MUTED = new Color(110, 150, 120);
    private final Font FONT_MONO_TITLE = new Font("Monospaced", Font.BOLD, 22);
    private final Font FONT_MONO_NORMAL = new Font("Monospaced", Font.BOLD, 12);

    public ImageConverterGUI() {
        setTitle("MATRIX Image Converter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 720);
        setLocationRelativeTo(null);
        setResizable(false);

        initStyles();
        initComponents();
        layoutComponents();
        setupListeners();
        updateQualityVisibility();
        updateLanguageTexts();
    }

    private void initStyles() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}
    }

    private void playBeep() {
        new Thread(() -> {
            try {
                byte[] buf = new byte[800];
                for (int i = 0; i < buf.length; i++) {
                    double angle = i / (8000.0 / 1200.0) * 2.0 * Math.PI;
                    buf[i] = (byte) (Math.sin(angle) * 60.0 * (1.0 - (double) i / buf.length));
                }
                AudioFormat af = new AudioFormat(8000f, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();
                sdl.write(buf, 0, buf.length);
                sdl.drain();
                sdl.close();
            } catch (Exception ignored) {}
        }).start();
    }

    private String t(String key) {
        if ("UA".equals(currentLang)) {
            switch (key) {
                case "title": return "КОНВЕРТЕР ЗОБРАЖЕНЬ";
                case "operator": return "ОПЕРАТОР";
                case "access": return "РІВЕНЬ ДОСТУПУ: АДМІН";
                case "single": return "ОДИН ФАЙЛ";
                case "batch": return "ПАКЕТНИЙ РЕЖИМ";
                case "source": return ":: ДЖЕРЕЛО ФАЙЛІВ";
                case "select_input": return "ВИБРАТИ ФАЙЛ / ПАПКУ";
                case "no_file": return "СТАТУС: ОЧІКУВАННЯ // ФАЙЛ НЕ ОБРАНО";
                case "no_dir": return "СТАТУС: ОЧІКУВАННЯ // ПАПКУ НЕ ОБРАНО";
                case "format": return ":: ФОРМАТ КОДУВАННЯ";
                case "quality": return ":: ЯКІСТЬ JPEG";
                case "dest": return ":: ПАПКА ЗБЕРЕЖЕННЯ";
                case "select_dest": return "ОБРАТИ ІНШУ ПАПКУ";
                case "dest_inplace": return "ЗБЕРЕГТИ ПОРУЧ З ОРИГІНАЛОМ";
                case "convert": return "РОЗПОЧАТИ КОНВЕРТАЦІЮ";
                case "ready": return "СИСТЕМА ГОТОВА";
                case "converting": return "КОНВЕРТАЦІЯ...";
                case "err_source": return "ПОМИЛКА: ОБЕРІТЬ ДЖЕРЕЛО!";
                case "success_batch": return "УСПІШНО: КОНВЕРТОВАНО {0} ФАЙЛІВ";
                case "success_single": return "ЗБЕРЕЖЕНО: ";
            }
        } else if ("PL".equals(currentLang)) {
            switch (key) {
                case "title": return "KONWERTER OBRAZÓW";
                case "operator": return "OPERATOR";
                case "access": return "POZIOM DOSTĘPU: ADMIN";
                case "single": return "JEDEN PLIK";
                case "batch": return "TRYB HURTOWY";
                case "source": return ":: ŹRÓDŁO PLIKÓW";
                case "select_input": return "WYBIERZ PLIK / FOLDER";
                case "no_file": return "STATUS: OCZEKIWANIE // BRAK PLIKU";
                case "no_dir": return "STATUS: OCZEKIWANIE // BRAK FOLDERU";
                case "format": return ":: FORMAT DOCELOWY";
                case "quality": return ":: JAKOŚĆ JPEG";
                case "dest": return ":: FOLDER ZAPISU";
                case "select_dest": return "WYBIERZ INNY FOLDER";
                case "dest_inplace": return "ZAPISZ OBOK ORYGINAŁU";
                case "convert": return "ROZPOCZNIJ KONWERSJĘ";
                case "ready": return "SYSTEM GOTOWY";
                case "converting": return "KONWERTOWANIE...";
                case "err_source": return "BŁĄD: WYBIERZ ŹRÓDŁO!";
                case "success_batch": return "SUKCES: SKONWERTOWANO {0} PLIKÓW";
                case "success_single": return "ZAPISANO: ";
            }
        } else {
            switch (key) {
                case "title": return "IMAGE CONVERTER";
                case "operator": return "OPERATOR";
                case "access": return "ACCESS LEVEL: ADMIN";
                case "single": return "SINGLE FILE";
                case "batch": return "BATCH MODE";
                case "source": return ":: SOURCE PATH";
                case "select_input": return "SELECT FILE / FOLDER";
                case "no_file": return "STATUS: IDLE // NO FILE LOADED";
                case "no_dir": return "STATUS: IDLE // NO FOLDER LOADED";
                case "format": return ":: TARGET FORMAT";
                case "quality": return ":: JPEG QUALITY";
                case "dest": return ":: DESTINATION PATH";
                case "select_dest": return "CHOOSE OUTPUT DIR";
                case "dest_inplace": return "SAVE NEXT TO ORIGINAL";
                case "convert": return "START CONVERSION";
                case "ready": return "SYSTEM ONLINE";
                case "converting": return "CONVERTING...";
                case "err_source": return "ERR: SELECT SOURCE FIRST!";
                case "success_batch": return "SUCCESS: CONVERTED {0} FILES";
                case "success_single": return "SAVED: ";
            }
        }
        return "";
    }

    private void updateLanguageTexts() {
        titleLabel.setText(t("title"));
        singleModeBtn.setText(t("single"));
        batchModeBtn.setText(t("batch"));
        labelSource.setText(t("source"));
        fileBtn.setText(t("select_input"));
        labelFormat.setText(t("format"));
        labelQualityTitle.setText(t("quality"));
        labelDestination.setText(t("dest"));
        outputDirBtn.setText(t("select_dest"));
        convertBtn.setText(t("convert"));
        
        if (selectedInput == null) {
            fileStatusLabel.setText(isBatchMode ? t("no_dir") : t("no_file"));
        } else {
            fileStatusLabel.setText("LOADED: " + selectedInput.getName());
        }

        if (selectedOutputDir == null) {
            outputDirStatusLabel.setText(t("dest_inplace"));
        } else {
            outputDirStatusLabel.setText("DEST: " + selectedOutputDir.getName() + "/");
        }

        statusLabel.setText(t("ready"));
    }

    private void initComponents() {
        mainPanel = new MatrixRainPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        titleLabel = new JLabel();
        titleLabel.setFont(FONT_MONO_TITLE);
        titleLabel.setForeground(COLOR_NEON);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        singleModeBtn = createMatrixToggleButton("", true);
        batchModeBtn = createMatrixToggleButton("", false);

        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(singleModeBtn);
        modeGroup.add(batchModeBtn);

        labelSource = createSectionLabel("");
        fileBtn = createMatrixButton("", COLOR_CARD, COLOR_NEON);
        fileStatusLabel = new JLabel();
        fileStatusLabel.setFont(FONT_MONO_NORMAL);
        fileStatusLabel.setForeground(COLOR_TEXT_MUTED);
        fileStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        labelFormat = createSectionLabel("");
        formatCombo = new JComboBox<>(ImageFormat.values());
        formatCombo.setBackground(COLOR_CARD);
        formatCombo.setForeground(COLOR_NEON);
        formatCombo.setFont(FONT_MONO_NORMAL);
        formatCombo.setBorder(new LineBorder(COLOR_NEON_DIM, 1));
        formatCombo.setFocusable(false);

        qualitySlider = new JSlider(0, 100, 90);
        qualitySlider.setBackground(new Color(10, 15, 10));
        qualitySlider.setForeground(COLOR_NEON);
        qualitySlider.setFocusable(false);
        qualityValueLabel = new JLabel("90%");
        qualityValueLabel.setForeground(COLOR_NEON);
        qualityValueLabel.setFont(FONT_MONO_NORMAL);

        labelDestination = createSectionLabel("");
        outputDirBtn = createMatrixButton("", COLOR_CARD, COLOR_NEON);
        outputDirStatusLabel = new JLabel();
        outputDirStatusLabel.setFont(FONT_MONO_NORMAL);
        outputDirStatusLabel.setForeground(COLOR_TEXT_MUTED);
        outputDirStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        convertBtn = createMatrixButton("", COLOR_NEON, COLOR_BG);
        convertBtn.setFont(new Font("Monospaced", Font.BOLD, 14));

        statusLabel = new JLabel();
        statusLabel.setFont(FONT_MONO_NORMAL);
        statusLabel.setForeground(COLOR_TEXT_MUTED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        createAuthorPanel();
    }

    private void createAuthorPanel() {
        authorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        authorPanel.setOpaque(false);

        JLabel avatarLabel = new JLabel();
        avatarLabel.setPreferredSize(new Dimension(50, 50));

        try {
            java.net.URL imgUrl = getClass().getResource("/avatar.jpg");
            BufferedImage srcImg = null;
            if (imgUrl != null) {
                srcImg = ImageIO.read(imgUrl);
            } else {
                File localFile = new File("src/avatar.jpg");
                if (localFile.exists()) {
                    srcImg = ImageIO.read(localFile);
                }
            }

            if (srcImg != null) {
                BufferedImage roundImg = makeRoundedImage(srcImg, 50);
                avatarLabel.setIcon(new ImageIcon(roundImg));
            } else {
                avatarLabel.setIcon(createDefaultAvatarIcon());
            }
        } catch (IOException e) {
            avatarLabel.setIcon(createDefaultAvatarIcon());
        }

        JLabel authorText = new JLabel("<html>OPERATOR: <font color='#00ff66'>ShuveeDev</font><br><font size='2' color='#78a082'>ACCESS_LEVEL: ADMIN</font></html>");
        authorText.setFont(FONT_MONO_NORMAL);
        authorText.setForeground(COLOR_TEXT);

        authorPanel.add(avatarLabel);
        authorPanel.add(authorText);
    }

    private ImageIcon createDefaultAvatarIcon() {
        BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(COLOR_CARD);
        g.fillOval(2, 2, 46, 46);
        g.setColor(COLOR_NEON);
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(2, 2, 46, 46);
        g.drawString("SD", 18, 30);
        g.dispose();
        return new ImageIcon(img);
    }

    private BufferedImage makeRoundedImage(BufferedImage image, int size) {
        BufferedImage resized = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        g.setClip(new Ellipse2D.Float(2, 2, size - 4, size - 4));
        g.drawImage(image, 2, 2, size - 4, size - 4, null);
        g.setClip(null);

        g.setColor(COLOR_NEON);
        g.setStroke(new BasicStroke(2f));
        g.drawOval(2, 2, size - 4, size - 4);
        
        g.dispose();
        return resized;
    }

    private void layoutComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);

        gbc.gridy = 0;
        JPanel langPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        langPanel.setOpaque(false);
        langPanel.add(createLangButton("UA"));
        langPanel.add(createLangButton("EN"));
        langPanel.add(createLangButton("PL"));
        mainPanel.add(langPanel, gbc);

        gbc.gridy = 1;
        mainPanel.add(titleLabel, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(8, 0, 12, 0);
        mainPanel.add(authorPanel, gbc);
        gbc.insets = new Insets(5, 0, 5, 0);

        gbc.gridy = 3;
        JPanel modePanel = new JPanel(new GridLayout(1, 2, 10, 0));
        modePanel.setOpaque(false);
        modePanel.add(singleModeBtn);
        modePanel.add(batchModeBtn);
        mainPanel.add(modePanel, gbc);

        gbc.gridy = 4;
        mainPanel.add(labelSource, gbc);

        gbc.gridy = 5;
        mainPanel.add(fileBtn, gbc);

        gbc.gridy = 6;
        mainPanel.add(fileStatusLabel, gbc);

        gbc.gridy = 7;
        mainPanel.add(labelFormat, gbc);

        gbc.gridy = 8;
        mainPanel.add(formatCombo, gbc);

        gbc.gridy = 9;
        labelQualityTitle = createSectionLabel("");
        qualityPanel = new JPanel(new BorderLayout(10, 0));
        qualityPanel.setOpaque(false);
        qualityPanel.add(labelQualityTitle, BorderLayout.WEST);
        qualityPanel.add(qualityValueLabel, BorderLayout.EAST);
        mainPanel.add(qualityPanel, gbc);

        gbc.gridy = 10;
        mainPanel.add(qualitySlider, gbc);

        gbc.gridy = 11;
        mainPanel.add(labelDestination, gbc);

        gbc.gridy = 12;
        mainPanel.add(outputDirBtn, gbc);

        gbc.gridy = 13;
        mainPanel.add(outputDirStatusLabel, gbc);

        gbc.gridy = 14;
        gbc.insets = new Insets(15, 0, 5, 0);
        mainPanel.add(convertBtn, gbc);

        gbc.gridy = 15;
        gbc.insets = new Insets(5, 0, 5, 0);
        mainPanel.add(statusLabel, gbc);

        add(mainPanel);
    }

    private JButton createLangButton(String lang) {
        JButton btn = new JButton(lang) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (currentLang.equals(lang)) {
                    g2.setColor(COLOR_NEON_DIM);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                }
                g2.setColor(COLOR_NEON);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Monospaced", Font.BOLD, 10));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorder(new EmptyBorder(4, 8, 4, 8));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            playBeep();
            currentLang = lang;
            updateLanguageTexts();
            repaint();
        });
        return btn;
    }

    private void setupListeners() {
        singleModeBtn.addActionListener(e -> {
            playBeep();
            isBatchMode = false;
            selectedInput = null;
            updateLanguageTexts();
        });

        batchModeBtn.addActionListener(e -> {
            playBeep();
            isBatchMode = true;
            selectedInput = null;
            updateLanguageTexts();
        });

        fileBtn.addActionListener(e -> {
            playBeep();
            JFileChooser chooser = new JFileChooser();
            if (isBatchMode) {
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            } else {
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            }
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedInput = chooser.getSelectedFile();
                fileStatusLabel.setText("LOADED: " + selectedInput.getName());
            }
        });

        outputDirBtn.addActionListener(e -> {
            playBeep();
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedOutputDir = chooser.getSelectedFile();
                outputDirStatusLabel.setText("DEST: " + selectedOutputDir.getName() + "/");
            }
        });

        formatCombo.addActionListener(e -> {
            playBeep();
            updateQualityVisibility();
        });

        qualitySlider.addChangeListener(e -> qualityValueLabel.setText(qualitySlider.getValue() + "%"));

        convertBtn.addActionListener(e -> {
            playBeep();
            triggerConversion();
        });
    }

    private void updateQualityVisibility() {
        ImageFormat fmt = (ImageFormat) formatCombo.getSelectedItem();
        boolean isJpeg = fmt == ImageFormat.JPG || fmt == ImageFormat.JPEG;
        qualityPanel.setVisible(isJpeg);
        qualitySlider.setVisible(isJpeg);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void triggerConversion() {
        if (selectedInput == null) {
            showStatus(t("err_source"), new Color(255, 50, 50));
            return;
        }

        ImageFormat targetFormat = (ImageFormat) formatCombo.getSelectedItem();
        float quality = qualitySlider.getValue() / 100.0f;
        converter.setJpegQuality(quality);

        showStatus(t("converting"), COLOR_NEON);

        new Thread(() -> {
            try {
                if (isBatchMode) {
                    File outDir = selectedOutputDir;
                    if (outDir == null) {
                        outDir = selectedInput;
                    }
                    int count = converter.convertDirectory(selectedInput, outDir, targetFormat);
                    showStatus(t("success_batch").replace("{0}", String.valueOf(count)), COLOR_NEON);
                } else {
                    File out;
                    if (selectedOutputDir != null) {
                        String name = stripExtension(selectedInput.getName()) + "." + targetFormat.getExtension();
                        out = new File(selectedOutputDir, name);
                        converter.convert(selectedInput, out, targetFormat);
                    } else {
                        out = converter.convertInPlace(selectedInput, targetFormat);
                    }
                    showStatus(t("success_single") + out.getName().toUpperCase(), COLOR_NEON);
                }
            } catch (Exception ex) {
                showStatus("ERR: " + ex.getMessage().toUpperCase(), new Color(255, 50, 50));
            }
        }).start();
    }

    private void showStatus(String text, Color color) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(">> " + text);
            statusLabel.setForeground(color);
        });
    }

    private String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot > 0) ? filename.substring(0, dot) : filename;
    }

    private JLabel createSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_MONO_NORMAL);
        lbl.setForeground(COLOR_TEXT_MUTED);
        return lbl;
    }

    private JButton createMatrixButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                g2.setColor(COLOR_NEON_DIM);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(FONT_MONO_NORMAL);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(new EmptyBorder(12, 16, 12, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.brighter());
                btn.setForeground(COLOR_NEON);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
                btn.setForeground(fg);
            }
        });
        return btn;
    }

    private JToggleButton createMatrixToggleButton(String text, boolean active) {
        JToggleButton btn = new JToggleButton(text, active) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                g2.setColor(isSelected() ? COLOR_NEON : COLOR_NEON_DIM);
                g2.setStroke(new BasicStroke(isSelected() ? 2f : 1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setBackground(active ? COLOR_CARD : new Color(5, 7, 5, 200));
        btn.setForeground(active ? COLOR_NEON : COLOR_TEXT_MUTED);
        btn.setFont(FONT_MONO_NORMAL);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(new EmptyBorder(10, 16, 10, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            singleModeBtn.setBackground(singleModeBtn.isSelected() ? COLOR_CARD : new Color(5, 7, 5, 200));
            singleModeBtn.setForeground(singleModeBtn.isSelected() ? COLOR_NEON : COLOR_TEXT_MUTED);
            batchModeBtn.setBackground(batchModeBtn.isSelected() ? COLOR_CARD : new Color(5, 7, 5, 200));
            batchModeBtn.setForeground(batchModeBtn.isSelected() ? COLOR_NEON : COLOR_TEXT_MUTED);
        });
        return btn;
    }

    private class MatrixRainPanel extends JPanel {
        private final int[] rainY;
        private final char[] chars;
        private final Random rand = new Random();

        public MatrixRainPanel() {
            setBackground(COLOR_BG);
            rainY = new int[40];
            chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZあぃいぅうぇえぉおかがきぎくぐけげこごさざしじすずせぜそぞただちぢっつづてでとどなにぬねのはばぱひびぴふぶぷへべぺほぼぽまみむめもゃやゅゆょよらりるれろゎわゐゑをん".toCharArray();
            for (int i = 0; i < rainY.length; i++) {
                rainY[i] = rand.nextInt(800);
            }
            Timer timer = new Timer(50, e -> {
                for (int i = 0; i < rainY.length; i++) {
                    rainY[i] += 12;
                    if (rainY[i] > getHeight()) {
                        rainY[i] = 0;
                    }
                }
                repaint();
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
            int colWidth = getWidth() / rainY.length;
            
            for (int i = 0; i < rainY.length; i++) {
                int x = i * colWidth + 2;
                int y = rainY[i];
                
                for (int j = 0; j < 15; j++) {
                    int cy = y - (j * 14);
                    if (cy < 0 || cy > getHeight()) continue;
                    
                    char c = chars[rand.nextInt(chars.length)];
                    
                    if (j == 0) {
                        g2.setColor(new Color(220, 255, 220, 240));
                    } else {
                        int alpha = Math.max(0, 240 - (j * 16));
                        g2.setColor(new Color(0, 255, 102, alpha));
                    }
                    g2.drawString(String.valueOf(c), x, cy);
                }
            }
            
            g2.setColor(new Color(5, 7, 5, 140));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
