import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Calendar;
import java.util.HashMap;

/*
 * CROP PRICE ADVISOR - pure Java (Swing), one file, no libraries, no database.
 * Pages: 1 Welcome -> 2 Name + State/District/Village -> 3 Choose crop -> 4 Crop details
 *
 * IMAGES: put your photos in a folder called "Images" next to this file:
 *         Images/rice.jpg, wheat.jpg, cotton.jpg, maize.jpg, sugarcane.jpg, groundnut.jpg
 *         (.jpg, .jpeg and .png all work; file name = crop name in lowercase)
 *
 * NOTE: all prices, costs and yields below are SAMPLE numbers. Replace with real
 * figures (Agmarknet / state agriculture department) before presenting.
 */
public class Crop {

    // ---------- colours and constants ----------
    static final Color GREEN = new Color(46, 125, 50);
    static final Color LIGHT = new Color(232, 245, 233);
    static final Color ORANGE = new Color(245, 124, 0);
    static final Color BLUE = new Color(21, 101, 192);
    static final Color RED = new Color(198, 40, 40);
    static final Color DARK = new Color(33, 33, 33);
    static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    static final String[] STATES = {"Andhra Pradesh", "Telangana", "Karnataka", "Maharashtra", "Tamil Nadu", "Kerala"};
    static final String[] CROPS = {"Rice", "Wheat", "Cotton", "Maize", "Sugarcane", "Groundnut"};
    // how the price moves through the year (1.00 = base price)
    static final double[] PATTERN = {0.95, 0.93, 0.96, 1.00, 1.04, 1.08,
                                     1.10, 1.07, 1.03, 0.98, 0.95, 0.97};

    // where to look for pictures: folder names and file extensions to try
    static final String[] IMAGE_FOLDERS = {"Images", "images"};
    static final String[] IMAGE_TYPES = {".jpg", ".jpeg", ".png", ".JPG", ".PNG"};

    // ---------- data (HashMap = key -> value, like a dictionary) ----------
    static HashMap<String, String[]> districtsOf = new HashMap<String, String[]>(); // state -> districts
    static HashMap<String, String[]> villagesOf = new HashMap<String, String[]>();  // district -> villages
    static HashMap<String, String[]> cropsOf = new HashMap<String, String[]>();     // district -> crops sold
    static HashMap<String, String[]> cropInfo = new HashMap<String, String[]>();    // crop -> details
    static HashMap<String, Double> factor = new HashMap<String, Double>();          // district -> price factor
    static HashMap<String, Color> cropColor = new HashMap<String, Color>();         // crop -> border colour
    static HashMap<String, ImageIcon> iconCache = new HashMap<String, ImageIcon>(); // loaded pictures

    // ---------- what the user chose (shared by all pages) ----------
    static JFrame frame;
    static String userName, state, district, village, crop;
    static JTextField nameField;
    static JComboBox<String> stateBox, districtBox, villageBox;

    // =====================================================================
    // PART 1: DATA
    // cropInfo order: 0 price per quintal, 1 cost per acre, 2 yield (quintal per acre),
    //                 3 season, 4 soil, 5 rainfall, 6 temperature, 7 days, 8 grown in districts
    // =====================================================================
    static void loadData() {
        districtsOf.put("Andhra Pradesh", "Guntur,Krishna".split(","));
        districtsOf.put("Telangana", "Warangal,Nizamabad".split(","));
        districtsOf.put("Karnataka", "Mysuru,Belagavi".split(","));
        districtsOf.put("Maharashtra", "Nashik,Pune".split(","));
        districtsOf.put("Tamil Nadu", "Chennai,Coimbatore".split(","));
        districtsOf.put("Kerala", "Thiruvananthapuram,Kochi".split(","));

        villagesOf.put("Guntur", "Tenali,Mangalagiri,Narasaraopet".split(","));
        villagesOf.put("Krishna", "Machilipatnam,Gudivada,Nuzvid".split(","));
        villagesOf.put("Warangal", "Hanamkonda,Narsampet,Parkal".split(","));
        villagesOf.put("Nizamabad", "Armoor,Bodhan,Balkonda".split(","));
        villagesOf.put("Mysuru", "Hunsur,Nanjangud,T Narasipura".split(","));
        villagesOf.put("Belagavi", "Gokak,Athani,Saundatti".split(","));
        villagesOf.put("Nashik", "Sinnar,Niphad,Igatpuri".split(","));
        villagesOf.put("Pune", "Baramati,Junnar,Shirur".split(","));
        villagesOf.put("Chennai", "Tambaram,Velachery,Sholinganallur".split(","));
        villagesOf.put("Coimbatore", "Pollachi,Perur,Annur".split(","));
        villagesOf.put("Thiruvananthapuram", "Neyyattinkara,Attingal,Varkala".split(","));
        villagesOf.put("Kochi", "Aluva,Perumbavoor,Kothamangalam".split(","));

        cropsOf.put("Guntur", "Rice,Cotton,Maize,Groundnut".split(","));
        cropsOf.put("Krishna", "Rice,Sugarcane,Maize".split(","));
        cropsOf.put("Warangal", "Rice,Cotton,Maize".split(","));
        cropsOf.put("Nizamabad", "Rice,Maize,Sugarcane".split(","));
        cropsOf.put("Mysuru", "Rice,Maize,Groundnut,Sugarcane".split(","));
        cropsOf.put("Belagavi", "Sugarcane,Cotton,Groundnut,Wheat".split(","));
        cropsOf.put("Nashik", "Wheat,Maize,Sugarcane,Cotton".split(","));
        cropsOf.put("Pune", "Wheat,Sugarcane,Maize".split(","));
        cropsOf.put("Chennai", "Rice,Wheat,Maize".split(","));
        cropsOf.put("Coimbatore", "Rice,Maize,Groundnut".split(","));
        cropsOf.put("Thiruvananthapuram", "Rice,Maize,Groundnut".split(","));
        cropsOf.put("Kochi", "Rice,Maize,Groundnut".split(","));
        cropInfo.put("Rice", new String[]{"2200", "28000", "22", "Kharif (Jun-Oct)", "Clay loam",
                "1000-1500 mm", "20-35 C", "120", "Guntur,Krishna,Warangal,Nizamabad,Mysuru"});
        cropInfo.put("Wheat", new String[]{"2275", "20000", "16", "Rabi (Nov-Mar)", "Loamy",
                "500-900 mm", "12-25 C", "130", "Belagavi,Nashik,Pune"});
        cropInfo.put("Cotton", new String[]{"6800", "32000", "8", "Kharif (Jun-Dec)", "Black soil",
                "600-1000 mm", "21-35 C", "170", "Guntur,Warangal,Belagavi"});
        cropInfo.put("Maize", new String[]{"2090", "18000", "24", "Kharif and Rabi", "Well-drained loam",
                "600-900 mm", "21-30 C", "100", "Guntur,Krishna,Warangal,Nizamabad,Mysuru,Nashik,Pune"});
        cropInfo.put("Sugarcane", new String[]{"340", "60000", "350", "Year round (12 months)", "Deep loam",
                "1100-1500 mm", "20-35 C", "365", "Krishna,Nizamabad,Mysuru,Belagavi,Nashik,Pune"});
        cropInfo.put("Groundnut", new String[]{"6377", "30000", "9", "Kharif (Jun-Oct)", "Sandy loam",
                "500-750 mm", "25-30 C", "110", "Guntur,Mysuru,Belagavi,Nashik"});

        factor.put("Guntur", 1.00);    factor.put("Krishna", 1.02);
        factor.put("Warangal", 0.98);  factor.put("Nizamabad", 0.97);
        factor.put("Mysuru", 1.03);    factor.put("Belagavi", 1.01);
        factor.put("Nashik", 1.05);    factor.put("Pune", 1.06);
        factor.put("Chennai", 1.04);   factor.put("Coimbatore", 1.02);
        factor.put("Thiruvananthapuram", 1.08);  factor.put("Kochi", 1.09);

        cropColor.put("Rice",      new Color(102, 187, 106));  // fresh green
        cropColor.put("Wheat",     new Color(230, 160,  60));  // golden amber
        cropColor.put("Cotton",    new Color(100, 181, 246));  // soft sky blue
        cropColor.put("Maize",     new Color(250, 215,  90));  // warm yellow
        cropColor.put("Sugarcane", new Color(149, 117, 205));  // muted violet
        cropColor.put("Groundnut", new Color(188, 143, 107));  // warm tan
    }

    // =====================================================================
    // PART 2: CALCULATIONS
    // =====================================================================
    static int indexOfCrop(String name) {
        for (int i = 0; i < CROPS.length; i++) {
            if (CROPS[i].equals(name)) {
                return i;
            }
        }
        return 0;
    }

    // 12 monthly prices of last year for a crop in the chosen district
    static double[] lastYear(String c) {
        double base = Double.parseDouble(cropInfo.get(c)[0]) * factor.get(district);
        int shift = indexOfCrop(c) * 2;      // so each crop has its own price curve
        double[] prices = new double[12];
        for (int m = 0; m < 12; m++) {
            prices[m] = Math.round(base * PATTERN[(m + shift) % 12]);
        }
        return prices;
    }

    // today's price = last year's price for this month + 5%
    static double todayPrice(String c) {
        int month = Calendar.getInstance().get(Calendar.MONTH);
        return Math.round(lastYear(c)[month] * 1.05);
    }

    static String money(double v) {
        return "Rs. " + String.format("%,.0f", v);
    }

    // =====================================================================
    // PART 3: SMALL HELPERS FOR LOOK AND FEEL
    // =====================================================================
    static JLabel label(String text, int size, boolean bold, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, size));
        l.setForeground(color);
        return l;
    }

    static JButton button(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 16));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        return b;
    }

    // coloured box with a small title and a big value
    static JPanel statBox(String title, String value, Color color) {
        JPanel box = new JPanel(new GridLayout(2, 1));
        box.setBackground(color);
        box.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        JLabel t = label(title, 14, false, Color.WHITE);
        JLabel v = label(value, 22, true, Color.WHITE);
        t.setHorizontalAlignment(SwingConstants.CENTER);
        v.setHorizontalAlignment(SwingConstants.CENTER);
        box.add(t);
        box.add(v);
        return box;
    }

    // ---------------------------------------------------------------------
    // CROP PICTURES
    // Looks for Images/<crop>.jpg (also .jpeg / .png, folder Images or images).
    // If no picture is found it draws a coloured circle with the first letter.
    // ---------------------------------------------------------------------

    // find the picture file for a crop, or null if there is none
    static File findImageFile(String name) {
        for (int f = 0; f < IMAGE_FOLDERS.length; f++) {
            for (int t = 0; t < IMAGE_TYPES.length; t++) {
                File file = new File(IMAGE_FOLDERS[f] + "/" + name.toLowerCase() + IMAGE_TYPES[t]);
                if (file.exists()) {
                    return file;
                }
            }
        }
        return null;
    }

    // cut the middle square out of the photo, then shrink it (so photos are not stretched)
    static ImageIcon squareIcon(BufferedImage pic, int size) {
        int side = Math.min(pic.getWidth(), pic.getHeight());
        int x = (pic.getWidth() - side) / 2;
        int y = (pic.getHeight() - side) / 2;
        BufferedImage square = pic.getSubimage(x, y, side, side);
        Image small = square.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        return new ImageIcon(small);
    }

    // backup picture: coloured circle with the first letter
    static ImageIcon circleIcon(String name, int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(cropColor.get(name));
        g.fillOval(0, 0, size, size);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, size / 2));
        FontMetrics fm = g.getFontMetrics();
        String letter = name.substring(0, 1);
        g.drawString(letter, (size - fm.stringWidth(letter)) / 2,
                (size + fm.getAscent() - fm.getDescent()) / 2);
        g.dispose();
        return new ImageIcon(img);
    }

    static ImageIcon cropIcon(String name, int size) {
        String key = name + size;
        if (iconCache.containsKey(key)) {          // already loaded once: reuse it
            return iconCache.get(key);
        }
        ImageIcon icon = null;
        File f = findImageFile(name);
        if (f != null) {
            try {
                BufferedImage pic = ImageIO.read(f);
                if (pic != null) {
                    icon = squareIcon(pic, size);
                }
            } catch (Exception e) {
                System.out.println("Could not read " + f.getPath());
            }
        } else {
            System.out.println("No picture for " + name + " (looked in "
                    + new File("Images").getAbsolutePath() + ")");
        }
        if (icon == null) {
            icon = circleIcon(name, size);
        }
        iconCache.put(key, icon);
        return icon;
    }

    // put a new page in the window
    static void setRoot(JComponent p) {
        frame.setContentPane(p);
        frame.revalidate();
        frame.repaint();
    }

    // standard page = green title bar + body + button bar
    static void showPage(String title, JComponent body, JButton... buttons) {
        JPanel root = new JPanel(new BorderLayout());
        JPanel head = new JPanel();
        head.setBackground(GREEN);
        head.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        head.add(label(title, 26, true, Color.WHITE));
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 12));
        foot.setBackground(LIGHT);
        for (JButton b : buttons) {
            foot.add(b);
        }
        root.add(head, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(foot, BorderLayout.SOUTH);
        setRoot(root);
    }

    // =====================================================================
    // PAGE 1: WELCOME
    // =====================================================================
    static void welcomePage() {
        JPanel p = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setPaint(new GradientPaint(0, 0, GREEN, 0, h, new Color(255, 193, 7)));
                g2.fillRect(0, 0, w, h);
                g2.setColor(new Color(255, 245, 157));
                g2.fillOval(w - 220, 50, 120, 120);                 // sun
                g2.setColor(new Color(27, 94, 32, 200));
                g2.fillOval(-w / 4, h - 120, w, 400);               // hills
                g2.fillOval(w / 3, h - 90, w, 400);
            }
        };
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets(10, 10, 10, 10);
        p.add(label("Welcome to", 24, false, Color.WHITE), c);
        p.add(label("Crop Price Advisor", 46, true, Color.WHITE), c);
        p.add(label("Know your market price, your profit, and if a crop can grow in your area",
                16, false, Color.WHITE), c);
        JButton start = button("Get Started", ORANGE);
        start.addActionListener(e -> detailsPage());
        c.insets = new Insets(30, 10, 10, 10);
        p.add(start, c);
        setRoot(p);
    }

    // =====================================================================
    // PAGE 2: NAME + STATE / DISTRICT / VILLAGE DROPDOWNS
    // =====================================================================
    static void addRow(JPanel form, String text, JComponent field, int row) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(12, 10, 12, 10);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        form.add(label(text, 18, true, DARK), c);
        c.gridx = 1;
        form.add(field, c);
    }

    // fill district box from the chosen state
    static void fillDistricts() {
        districtBox.removeAllItems();
        String[] list = districtsOf.get((String) stateBox.getSelectedItem());
        for (int i = 0; i < list.length; i++) {
            districtBox.addItem(list[i]);
        }
    }

    // fill village box from the chosen district
    static void fillVillages() {
        String d = (String) districtBox.getSelectedItem();
        if (d == null) {
            return;   // district box is being refilled
        }
        villageBox.removeAllItems();
        String[] list = villagesOf.get(d);
        for (int i = 0; i < list.length; i++) {
            villageBox.addItem(list[i]);
        }
    }

    // check every field before moving on; returns a warning text, or null if all is fine
    static String checkDetails() {
        if (nameField.getText().trim().equals("")) {
            return "Please enter your name.";
        }
        if (stateBox.getSelectedItem() == null) {
            return "Please select your State.";
        }
        if (districtBox.getSelectedItem() == null) {
            return "Please select your District.";
        }
        if (villageBox.getSelectedItem() == null) {
            return "Please select your Village / Town.";
        }
        String d = (String) districtBox.getSelectedItem();
        if (cropsOf.get(d) == null || factor.get(d) == null) {
            return "Sorry, price data for " + d + " is not available yet.\nPlease choose another district.";
        }
        return null;   // everything is fine
    }

    static void detailsPage() {
        // 1. create the fields
        nameField = new JTextField(userName == null ? "" : userName, 20);
        stateBox = new JComboBox<String>(STATES);
        districtBox = new JComboBox<String>();
        villageBox = new JComboBox<String>();
        JComponent[] fields = {nameField, stateBox, districtBox, villageBox};
        for (int i = 0; i < fields.length; i++) {
            fields[i].setFont(new Font("SansSerif", Font.PLAIN, 16));
            fields[i].setPreferredSize(new Dimension(260, 34));
        }

        // 2. put them in the form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(LIGHT);
        addRow(form, "Your name", nameField, 0);
        addRow(form, "State", stateBox, 1);
        addRow(form, "District", districtBox, 2);
        addRow(form, "Village / Town", villageBox, 3);

        // 3. connect the dropdowns FIRST, then fill them
        //    (before: filling happened first, so the village box stayed empty)
        stateBox.addActionListener(e -> fillDistricts());   // state changed -> new districts
        districtBox.addActionListener(e -> fillVillages()); // district changed -> new villages
        fillDistricts();                                    // this also fills the villages

        // 4. buttons
        JButton back = button("Back", BLUE);
        back.addActionListener(e -> welcomePage());
        JButton next = button("Next", ORANGE);
        next.addActionListener(e -> {
            String warning = checkDetails();
            if (warning != null) {
                JOptionPane.showMessageDialog(frame, warning, "Missing details",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            userName = nameField.getText().trim();
            state = (String) stateBox.getSelectedItem();
            district = (String) districtBox.getSelectedItem();
            village = (String) villageBox.getSelectedItem();
            cropPage();
        });
        showPage("Tell us about yourself", form, back, next);
    }

    // =====================================================================
    // PAGE 3: CHOOSE A CROP (one card per crop sold in the district market)
    // =====================================================================
    static void cropPage() {
        String[] list = cropsOf.get(district);
        JPanel grid = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        grid.setBackground(LIGHT);
        for (int i = 0; i < list.length; i++) {
            final String name = list[i];
            JButton card = new JButton("<html><center><b>" + name + "</b><br>"
                    + money(todayPrice(name)) + " / quintal</center></html>");
            card.setIcon(cropIcon(name, 140));
            card.setVerticalTextPosition(SwingConstants.BOTTOM);
            card.setHorizontalTextPosition(SwingConstants.CENTER);
            card.setFont(new Font("SansSerif", Font.PLAIN, 16));
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createLineBorder(cropColor.get(name), 4));
            card.setPreferredSize(new Dimension(210, 240));
            card.addActionListener(e -> {
                crop = name;
                resultPage();
            });
            grid.add(card);
        }
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(LIGHT);
        JLabel hello = label("Hello " + userName + "! Crops sold in " + village + ", "
                + district + " market - pick one", 18, true, DARK);
        hello.setHorizontalAlignment(SwingConstants.CENTER);
        hello.setBorder(BorderFactory.createEmptyBorder(20, 10, 0, 10));
        body.add(hello, BorderLayout.NORTH);
        body.add(grid, BorderLayout.CENTER);
        JButton back = button("Back", BLUE);
        back.addActionListener(e -> detailsPage());
        showPage("Which crop do you want?", body, back);
    }

    // =====================================================================
    // PAGE 4: CROP DETAILS (picture on top + 3 tabs)
    // =====================================================================
    static void resultPage() {
        String[] info = cropInfo.get(crop);
        double[] prices = lastYear(crop);
        double today = todayPrice(crop);

        JPanel top = new JPanel(new BorderLayout(20, 0));
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        top.add(new JLabel(cropIcon(crop, 100)), BorderLayout.WEST);
        JPanel text = new JPanel(new GridLayout(3, 1));
        text.setOpaque(false);
        text.add(label(crop + " in " + district, 26, true, GREEN));
        text.add(label("Market near " + village + ", " + state, 15, false, DARK));
        text.add(label("Today's price: " + money(today) + " per quintal", 20, true, ORANGE));
        top.add(text, BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 16));
        tabs.addTab("Last year prices", pricesTab(prices, today));
        tabs.addTab("Investment", investmentTab(info, today));
        tabs.addTab("Can it grow here?", growTab(info));

        JPanel body = new JPanel(new BorderLayout());
        body.add(top, BorderLayout.NORTH);
        body.add(tabs, BorderLayout.CENTER);

        JButton back = button("Back", BLUE);
        back.addActionListener(e -> cropPage());
        JButton home = button("Start Over", ORANGE);
        home.addActionListener(e -> welcomePage());
        showPage("Crop Details", body, back, home);
    }

    // TAB 1: four stat boxes + bar chart
    static JPanel pricesTab(double[] p, double today) {
        double sum = 0, max = p[0], min = p[0];
        int maxM = 0, minM = 0;
        for (int i = 0; i < 12; i++) {
            sum = sum + p[i];
            if (p[i] > max) { max = p[i]; maxM = i; }
            if (p[i] < min) { min = p[i]; minM = i; }
        }
        double avg = sum / 12;
        double diff = (today - avg) / avg * 100;

        JPanel stats = new JPanel(new GridLayout(1, 4, 10, 10));
        stats.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        stats.add(statBox("Average", money(avg), BLUE));
        stats.add(statBox("Highest (" + MONTHS[maxM] + ")", money(max), GREEN));
        stats.add(statBox("Lowest (" + MONTHS[minM] + ")", money(min), RED));
        stats.add(statBox("Today vs average", String.format("%+.1f%%", diff), ORANGE));

        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.add(stats, BorderLayout.NORTH);
        tab.add(new Chart(p), BorderLayout.CENTER);
        return tab;
    }

    // TAB 2: type acres -> see investment, income, profit
    static JPanel investmentTab(String[] info, double today) {
        final double costPerAcre = Double.parseDouble(info[1]);
        final double yieldPerAcre = Double.parseDouble(info[2]);
        final double price = today;

        final JTextField acresField = new JTextField("1", 8);
        acresField.setFont(new Font("SansSerif", Font.PLAIN, 18));
        JButton calc = button("Calculate", GREEN);
        JPanel input = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 15));
        input.add(label("Land (acres):", 18, true, DARK));
        input.add(acresField);
        input.add(calc);

        JLabel note = label("Cost per acre: " + money(costPerAcre) + "   |   Expected yield: "
                + (int) yieldPerAcre + " quintal per acre   |   Price used: " + money(price), 14, false, DARK);
        note.setHorizontalAlignment(SwingConstants.CENTER);

        final JPanel result = new JPanel(new GridLayout(1, 3, 15, 15));
        result.setBorder(BorderFactory.createEmptyBorder(20, 20, 40, 20));

        calc.addActionListener(e -> {
            double acres;
            try {
                acres = Double.parseDouble(acresField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Please type a number, for example 2.5");
                return;
            }
            if (acres <= 0) {
                JOptionPane.showMessageDialog(frame, "Acres must be more than 0.");
                return;
            }
            double cost = costPerAcre * acres;            // investment
            double income = yieldPerAcre * acres * price; // expected income
            double profit = income - cost;
            result.removeAll();
            result.add(statBox("Investment", money(cost), BLUE));
            result.add(statBox("Expected income", money(income), ORANGE));
            if (profit >= 0) {
                result.add(statBox("Profit", money(profit), GREEN));
            } else {
                result.add(statBox("Loss", money(-profit), RED));
            }
            result.revalidate();
            result.repaint();
        });
        calc.doClick();   // show the answer for 1 acre straight away

        JPanel north = new JPanel(new BorderLayout());
        north.add(input, BorderLayout.CENTER);
        north.add(note, BorderLayout.SOUTH);
        JPanel tab = new JPanel(new BorderLayout());
        tab.add(north, BorderLayout.NORTH);
        tab.add(result, BorderLayout.CENTER);
        return tab;
    }

    // TAB 3: YES / NOT RECOMMENDED + growing details
    static JPanel growTab(String[] info) {
        boolean ok = false;
        String[] growers = info[8].split(",");
        for (int i = 0; i < growers.length; i++) {
            if (growers[i].equals(district)) {
                ok = true;
            }
        }
        JLabel answer = label(ok ? "YES - " + crop + " can grow in " + district
                : "NOT RECOMMENDED in " + district, 26, true, Color.WHITE);
        answer.setOpaque(true);
        answer.setBackground(ok ? GREEN : RED);
        answer.setHorizontalAlignment(SwingConstants.CENTER);
        answer.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        String[] names = {"Season", "Soil", "Rainfall needed", "Temperature", "Days to harvest", "Usually grown in"};
        String[] values = {info[3], info[4], info[5], info[6], info[7] + " days", info[8].replace(",", ", ")};
        JPanel grid = new JPanel(new GridLayout(names.length, 2, 8, 8));
        grid.setBorder(BorderFactory.createEmptyBorder(15, 40, 15, 40));
        for (int i = 0; i < names.length; i++) {
            grid.add(label(names[i], 17, true, BLUE));
            grid.add(label(values[i], 17, false, DARK));
        }
        JPanel tab = new JPanel(new BorderLayout(10, 10));
        tab.add(answer, BorderLayout.NORTH);
        tab.add(grid, BorderLayout.CENTER);
        return tab;
    }

    // =====================================================================
    // BAR CHART: draws 12 bars with fillRoundRect
    // =====================================================================
    static class Chart extends JPanel {
        double[] v;

        Chart(double[] v) {
            this.v = v;
            setBackground(Color.WHITE);
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double max = v[0], min = v[0];
            for (int i = 0; i < 12; i++) {
                if (v[i] > max) max = v[i];
                if (v[i] < min) min = v[i];
            }
            double base = min * 0.8;            // bars start a bit below the lowest price
            int w = getWidth(), h = getHeight();
            int slot = (w - 40) / 12;           // width given to each month
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.setColor(DARK);
            g2.drawString("Price per quintal, month by month last year (bars start at "
                    + money(base) + ")", 20, 22);
            for (int i = 0; i < 12; i++) {
                int barH = (int) ((v[i] - base) / (max - base) * (h - 90));
                int x = 20 + i * slot + 6;
                int y = h - 30 - barH;
                g2.setColor(v[i] == max ? ORANGE : GREEN);   // highest month in orange
                g2.fillRoundRect(x, y, slot - 12, barH, 8, 8);
                g2.setColor(DARK);
                g2.drawString(String.valueOf((int) v[i]), x, y - 5);
                g2.drawString(MONTHS[i], x + 6, h - 12);
            }
        }
    }

    // =====================================================================
    // START OF THE PROGRAM
    // =====================================================================
    public static void main(String[] args) {
        loadData();
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Crop Price Advisor");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null);
            welcomePage();
            frame.setVisible(true);
        });
    }
}