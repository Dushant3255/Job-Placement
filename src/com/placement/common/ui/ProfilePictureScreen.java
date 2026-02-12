package com.placement.common.ui;

import com.placement.company.service.CompanyLogoService;
import com.placement.student.service.StudentProfilePictureService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.List;

public class ProfilePictureScreen extends JFrame {

    private final String email;
    private final String gender;
    private final boolean isCompany;

    private JLabel statusLabel;
    private JProgressBar progressBar;
    private JLabel previewLabel;

    private File selectedImageFile;
    private boolean usingDefaultImage = false;
    private String defaultImageName = "";

    private static final int DROP_W = 350;
    private static final int DROP_H = 320;

    private final Color GRAD_START;
    private final Color GRAD_END;
    private final Color PRIMARY_BTN;
    private final Color SECONDARY_BG;
    private final Color SECONDARY_FG;
    private final Color LINK_HOVER;

    private final CompanyLogoService companyLogoService = new CompanyLogoService();
    private final StudentProfilePictureService studentProfilePictureService = new StudentProfilePictureService();

    public ProfilePictureScreen(String email, String gender, boolean isCompany) {
        this.email = email;
        this.gender = gender;
        this.isCompany = isCompany;

        if (isCompany) {
            GRAD_START = new Color(220, 38, 38);
            GRAD_END   = new Color(244, 63, 94);
            PRIMARY_BTN = new Color(220, 38, 38);
            SECONDARY_BG = new Color(254, 226, 226);
            SECONDARY_FG = new Color(185, 28, 28);
            LINK_HOVER = new Color(220, 38, 38);
        } else {
            GRAD_START = new Color(99, 102, 241);
            GRAD_END   = new Color(124, 58, 237);
            PRIMARY_BTN = new Color(99, 102, 241);
            SECONDARY_BG = new Color(237, 233, 254);
            SECONDARY_FG = new Color(79, 70, 229);
            LINK_HOVER = new Color(79, 70, 229);
        }

        setTitle("Student Placement Portal");
        setSize(420, 740);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        String subtitle = isCompany ? "Upload Company Logo" : "Upload Profile Picture";
        VerifyOtpScreen.Purpose backPurpose = isCompany
                ? VerifyOtpScreen.Purpose.COMPANY_SIGNUP
                : VerifyOtpScreen.Purpose.STUDENT_SIGNUP;

        JPanel header = buildHeader(subtitle, e -> {
            new VerifyOtpScreen(email, gender, isCompany, backPurpose).setVisible(true);
            dispose();
        });
        root.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(10, 30, 30, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        int row = 0;

        JPanel dropArea = createDropArea();
        dropArea.setPreferredSize(new Dimension(DROP_W, DROP_H));
        dropArea.setMinimumSize(new Dimension(DROP_W, DROP_H));

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        progressBar.setVisible(false);

        InteractiveButton browseBtn = new InteractiveButton("Browse Files", SECONDARY_BG);
        browseBtn.setForeground(SECONDARY_FG);
        browseBtn.addActionListener(e -> browseForImage());

        InteractiveButton removeBtn = new InteractiveButton("Remove", SECONDARY_BG);
        removeBtn.setForeground(SECONDARY_FG);
        removeBtn.addActionListener(e -> removePicture());

        InteractiveButton saveBtn = new InteractiveButton("Save & Continue", PRIMARY_BTN);
        saveBtn.addActionListener(e -> onSave());

        JButton maybeLaterBtn = createLinkButton("Maybe later");
        maybeLaterBtn.addActionListener(e -> applyDefaultAndFinish());

        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.BOTH;
        form.add(dropArea, gbc);

        gbc.gridy = row++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(statusLabel, gbc);

        gbc.gridy = row++;
        form.add(progressBar, gbc);

        gbc.gridy = row++;
        JPanel buttonsRow1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonsRow1.setBackground(Color.WHITE);
        buttonsRow1.add(browseBtn);
        buttonsRow1.add(removeBtn);
        form.add(buttonsRow1, gbc);

        gbc.gridy = row++;
        JPanel buttonsRow2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonsRow2.setBackground(Color.WHITE);
        buttonsRow2.add(saveBtn);
        form.add(buttonsRow2, gbc);

        gbc.gridy = row++;
        form.add(maybeLaterBtn, gbc);

        gbc.gridy = row++;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        form.add(filler, gbc);

        root.add(form, BorderLayout.CENTER);
        add(root);
    }

    private JPanel createDropArea() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        String txt = isCompany ? "Drop Logo Here" : "Drop Image Here";
        previewLabel = new JLabel(txt, SwingConstants.CENTER);
        previewLabel.setOpaque(true);
        previewLabel.setBackground(Color.WHITE);
        previewLabel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 230)));

        previewLabel.setPreferredSize(new Dimension(DROP_W, DROP_H));
        previewLabel.setMinimumSize(new Dimension(DROP_W, DROP_H));

        new DropTarget(previewLabel, new DropTargetAdapter() {
            @Override
            @SuppressWarnings("unchecked")
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Object data = dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

                    if (data instanceof List) {
                        List<File> files = (List<File>) data;
                        if (!files.isEmpty()) {
                            File f = files.get(0);
                            if (isImageFile(f)) {
                                loadAndPreviewWithStatus(f);
                                dtde.dropComplete(true);
                                return;
                            }
                        }
                    }
                    setError("Please drop an image file (jpg, png, jpeg, gif, bmp).");
                    dtde.dropComplete(false);
                } catch (Exception ex) {
                    setError("Drop failed. Try again.");
                    dtde.dropComplete(false);
                }
            }
        });

        panel.add(previewLabel, BorderLayout.CENTER);
        return panel;
    }

    private void browseForImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(isCompany ? "Choose a Company Logo" : "Choose a Profile Picture");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Image Files (jpg, jpeg, png, gif, bmp)", "jpg", "jpeg", "png", "gif", "bmp"
        ));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            if (isImageFile(f)) loadAndPreviewWithStatus(f);
            else setError("Please select a valid image file.");
        }
    }

    private void loadAndPreviewWithStatus(File file) {
        setBusy(true, "Uploading...");
        usingDefaultImage = false;
        defaultImageName = "";

        SwingWorker<BufferedImage, Void> worker = new SwingWorker<>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                Thread.sleep(300);
                BufferedImage img = ImageIO.read(file);
                if (img == null) throw new IllegalArgumentException("Not an image");
                return img;
            }

            @Override
            protected void done() {
                try {
                    BufferedImage img = get();
                    selectedImageFile = file;

                    Image scaled = scaleToFit(img, DROP_W, DROP_H);
                    previewLabel.setText("");
                    previewLabel.setIcon(new ImageIcon(scaled));

                    setBusy(false, "Uploaded: " + file.getName());
                } catch (Exception ex) {
                    selectedImageFile = null;
                    previewLabel.setIcon(null);
                    previewLabel.setText(isCompany ? "Drop Logo Here" : "Drop Image Here");
                    setError("Could not load image. Try another file.");
                }
            }
        };
        worker.execute();
    }

    private void removePicture() {
        selectedImageFile = null;
        usingDefaultImage = false;
        defaultImageName = "";
        previewLabel.setIcon(null);
        previewLabel.setText(isCompany ? "Drop Logo Here" : "Drop Image Here");
        setError("Removed.");
    }

    private String genderKey() {
        String g = (gender == null) ? "" : gender.trim().toLowerCase();
        if (g.contains("female")) return "female";
        if (g.contains("male")) return "male";
        return "other";
    }

    private static class DefaultPick {
        final BufferedImage img;
        final String name;
        DefaultPick(BufferedImage img, String name) { this.img = img; this.name = name; }
    }

    private DefaultPick loadDefaultImage() throws Exception {
        String[] candidates;

        if (isCompany) {
            candidates = new String[]{"default_company_logo.png", "default_logo.png", "company_logo.png"};
        } else {
            String key = genderKey();
            if (key.equals("female")) candidates = new String[]{"default_female.png", "female.png"};
            else if (key.equals("male")) candidates = new String[]{"default_male.png", "male.png"};
            else candidates = new String[]{"default_other.png", "other.png"};
        }

        for (String name : candidates) {
            try (InputStream in = getClass().getResourceAsStream("/images/" + name)) {
                if (in != null) {
                    BufferedImage img = ImageIO.read(in);
                    if (img != null) return new DefaultPick(img, name);
                }
            }
        }

        for (String name : candidates) {
            File disk = new File("resources/images/" + name);
            if (disk.exists()) {
                BufferedImage img = ImageIO.read(disk);
                if (img != null) return new DefaultPick(img, name);
            }
        }

        throw new IllegalStateException("Default image not found");
    }

    private void applyDefaultAndFinish() {
        setBusy(true, "Applying default...");

        SwingWorker<DefaultPick, Void> worker = new SwingWorker<>() {
            @Override
            protected DefaultPick doInBackground() throws Exception {
                Thread.sleep(200);
                DefaultPick pick = loadDefaultImage();

                if (isCompany) {
                    // ✅ FIX: must pass default image name
                    companyLogoService.applyDefaultLogo(email, pick.name);
                } else {
                    // ✅ Student default should save to DB too
                    studentProfilePictureService.applyDefaultProfilePicture(email, pick.name);
                }

                return pick;
            }

            @Override
            protected void done() {
                try {
                    DefaultPick pick = get();

                    selectedImageFile = null;
                    usingDefaultImage = true;
                    defaultImageName = pick.name;

                    Image scaled = scaleToFit(pick.img, DROP_W, DROP_H);
                    previewLabel.setText("");
                    previewLabel.setIcon(new ImageIcon(scaled));

                    setBusy(false, "Default applied.");

                    JOptionPane.showMessageDialog(
                            ProfilePictureScreen.this,
                            isCompany ? "Default company logo applied." : "Default profile picture applied.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    new LoginScreen().setVisible(true);
                    dispose();

                } catch (Exception ex) {
                    Throwable root = ex;
                    if (ex instanceof java.util.concurrent.ExecutionException && ex.getCause() != null) {
                        root = ex.getCause();
                    }
                    setError("Error: " + (root.getMessage() == null ? "Unknown error" : root.getMessage()));
                }
            }
        };
        worker.execute();
    }

    private void onSave() {
        if (selectedImageFile == null && !usingDefaultImage) {
            applyDefaultAndFinish();
            return;
        }

        // ✅ Company save
        if (isCompany) {
            if (selectedImageFile == null) {
                applyDefaultAndFinish();
                return;
            }

            setBusy(true, "Saving logo...");

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    companyLogoService.saveUploadedLogo(email, selectedImageFile);
                    Thread.sleep(150);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        setBusy(false, "Saved.");

                        JOptionPane.showMessageDialog(
                                ProfilePictureScreen.this,
                                "Company logo saved!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        new LoginScreen().setVisible(true);
                        dispose();

                    } catch (Exception ex) {
                        Throwable root = ex;
                        if (ex instanceof java.util.concurrent.ExecutionException && ex.getCause() != null) {
                            root = ex.getCause();
                        }
                        setError("Save failed: " + (root.getMessage() == null ? "Unknown error" : root.getMessage()));
                    }
                }
            };
            worker.execute();
            return;
        }

        // ✅ Student save
        if (selectedImageFile == null) {
            applyDefaultAndFinish();
            return;
        }

        setBusy(true, "Saving picture...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                studentProfilePictureService.saveUploadedProfilePicture(email, selectedImageFile);
                Thread.sleep(150);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    setBusy(false, "Saved.");

                    JOptionPane.showMessageDialog(
                            ProfilePictureScreen.this,
                            "Profile picture saved!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    new LoginScreen().setVisible(true);
                    dispose();

                } catch (Exception ex) {
                    Throwable root = ex;
                    if (ex instanceof java.util.concurrent.ExecutionException && ex.getCause() != null) {
                        root = ex.getCause();
                    }
                    setError("Save failed: " + (root.getMessage() == null ? "Unknown error" : root.getMessage()));
                }
            }
        };
        worker.execute();
    }

    /* ---------- Header + UI helpers ---------- */

    private JPanel buildHeader(String subtitleText, java.awt.event.ActionListener backAction) {
        JPanel header = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(
                        0, 0, GRAD_START,
                        getWidth(), getHeight(), GRAD_END
                ));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        header.setPreferredSize(new Dimension(420, 140));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(18, 26, 15, 20));

        JButton backBtn = new HeaderBackButton("← Back");
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.addActionListener(backAction);

        JLabel title = new JLabel("Student Placement Portal");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel(subtitleText);
        subtitle.setForeground(isCompany ? new Color(255, 230, 230) : new Color(230, 230, 255));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(backBtn);
        header.add(Box.createVerticalStrut(10));
        header.add(title);
        header.add(Box.createVerticalStrut(5));
        header.add(subtitle);

        return header;
    }

    private JButton createLinkButton(String text) {
        JButton btn = new JButton("<HTML><U>" + text + "</U></HTML>");
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setForeground(Color.GRAY);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setForeground(LINK_HOVER); }
            public void mouseExited(MouseEvent e) { btn.setForeground(Color.GRAY); }
        });
        return btn;
    }

    private void setBusy(boolean busy, String msg) {
        statusLabel.setForeground(new Color(60, 60, 60));
        statusLabel.setText(msg);
        progressBar.setVisible(busy);
        progressBar.setIndeterminate(busy);
    }

    private void setError(String msg) {
        statusLabel.setForeground(Color.RED);
        statusLabel.setText(msg);
        progressBar.setVisible(false);
        progressBar.setIndeterminate(false);
    }

    private boolean isImageFile(File f) {
        if (f == null || !f.isFile()) return false;
        String n = f.getName().toLowerCase();
        return n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png")
                || n.endsWith(".gif") || n.endsWith(".bmp");
    }

    private Image scaleToFit(BufferedImage img, int maxW, int maxH) {
        int w = img.getWidth(), h = img.getHeight();
        double scale = Math.min((double) maxW / w, (double) maxH / h);

        int newW = Math.max(1, (int) (w * scale));
        int newH = Math.max(1, (int) (h * scale));

        BufferedImage out = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(img, 0, 0, newW, newH, null);
        g2.dispose();
        return out;
    }

    static class InteractiveButton extends JButton {
        private final Color normal;
        private final Color hover;
        private final Color pressed;

        public InteractiveButton(String text, Color normal) {
            super(text);
            this.normal = normal;
            this.hover = normal.darker();
            this.pressed = normal.darker().darker();

            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 28, 10, 28));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { repaint(); }
                @Override public void mouseExited(MouseEvent e) { repaint(); }
                @Override public void mousePressed(MouseEvent e) { repaint(); }
                @Override public void mouseReleased(MouseEvent e) { repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (getModel().isPressed()) g2.setColor(pressed);
            else if (getModel().isRollover()) g2.setColor(hover);
            else g2.setColor(normal);

            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}
