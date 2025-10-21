import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Collections;


public class ParticleLife extends JFrame {
    private SimulationPanel simulationPanel;
    private JPanel controlPanel;
    private MatrixPanel matrixPanel;
    private boolean uiVisible = true;

    
    
    public ParticleLife() {
        setTitle("Particle Life Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        simulationPanel = new SimulationPanel();
        add(simulationPanel, BorderLayout.CENTER);
        
        createControlPanel();
        add(controlPanel, BorderLayout.EAST);
        
        // Toggle UI with H key
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_H) {
                    toggleUI();
                }
            }
        });
        
        setFocusable(true);
        pack();
        setLocationRelativeTo(null);
        
        // Generate initial particles
        simulationPanel.generateRandomParticles(2000);
    }
    
    private void createControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        controlPanel.setPreferredSize(new Dimension(380, 600));
        controlPanel.setBackground(new Color(40, 40, 40));
        
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(0, 2, 5, 5));
        topPanel.setBackground(new Color(40, 40, 40));
        
        JButton hideButton = new JButton("Hide UI (H)");
        hideButton.addActionListener(e -> toggleUI());
        topPanel.add(hideButton);
        
        JButton clearButton = new JButton("Clear All");
        clearButton.addActionListener(e -> simulationPanel.clearParticles());
        topPanel.add(clearButton);
        
        JButton pauseButton = new JButton("Pause");
        pauseButton.addActionListener(e -> {
            boolean paused = simulationPanel.togglePause();
            pauseButton.setText(paused ? "Resume" : "Pause");
        });
        topPanel.add(pauseButton);

        JButton randomAllButton = new JButton("Randomize Everything");
        randomAllButton.addActionListener(e -> {
            simulationPanel.randomizeEverything();
            matrixPanel.updateFields();
        });
        topPanel.add(randomAllButton);
        
        JButton randomButton = new JButton("Random Matrix");
        randomButton.addActionListener(e -> {
            simulationPanel.randomizeMatrix();
            simulationPanel.generateRandomParticles(5000);
            matrixPanel.updateFields();
        });
        topPanel.add(randomButton);
        
        
        controlPanel.add(topPanel, BorderLayout.NORTH);
        
        // Create scrollable content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBackground(new Color(40, 40, 40));
        
        // Matrix editor
        matrixPanel = new MatrixPanel(simulationPanel);
        contentPanel.add(matrixPanel, BorderLayout.NORTH);
        
        // Particle placement controls
        JPanel placementPanel = createPlacementPanel();
        contentPanel.add(placementPanel, BorderLayout.CENTER);
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        controlPanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    private JPanel createPlacementPanel() {
        JPanel placementPanel = new JPanel();
        placementPanel.setLayout(new GridBagLayout());
        placementPanel.setBackground(new Color(40, 40, 40));
        placementPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), 
            "Add Particles",
            0, 0, null, Color.WHITE));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel infoLabel = new JLabel("<html><b>Drag on canvas to place particles</b></html>");
        infoLabel.setForeground(Color.WHITE);
        placementPanel.add(infoLabel, gbc);
        
        // Shape selection
        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel shapeLabel = new JLabel("Shape:");
        shapeLabel.setForeground(Color.WHITE);
        placementPanel.add(shapeLabel, gbc);
        
        gbc.gridx = 1;
        String[] shapes = {"Circle", "Square", "Line", "Ring"};
        JComboBox<String> shapeCombo = new JComboBox<>(shapes);
        shapeCombo.addActionListener(e -> 
            simulationPanel.setPlacementShape((String) shapeCombo.getSelectedItem()));
        placementPanel.add(shapeCombo, gbc);
        
        // Particle count
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel countLabel = new JLabel("Count:");
        countLabel.setForeground(Color.WHITE);
        placementPanel.add(countLabel, gbc);
        
        gbc.gridx = 1;
        JTextField countField = new JTextField("100", 10);
        countField.addActionListener(e -> {
            try {
                int count = Integer.parseInt(countField.getText());
                simulationPanel.setParticleCount(Math.max(1, Math.min(1000, count)));
            } catch (NumberFormatException ex) {
                countField.setText("100");
            }
        });
        placementPanel.add(countField, gbc);
        
        // Species selection
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JLabel speciesLabel = new JLabel("Species:");
        speciesLabel.setForeground(Color.WHITE);
        placementPanel.add(speciesLabel, gbc);
        
        gbc.gridy++;
        JPanel colorPanel = new JPanel();
        colorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        colorPanel.setBackground(new Color(40, 40, 40));
        
        for (int i = 0; i < SimulationPanel.M; i++) {
            final int species = i;
            JButton colorBtn = new JButton(String.valueOf(i));
            colorBtn.setPreferredSize(new Dimension(50, 40));
            colorBtn.setBackground(simulationPanel.particleColors[i]);
            colorBtn.setForeground(Color.BLACK);
            colorBtn.addActionListener(e -> {
                simulationPanel.setSelectedSpecies(species);
                for (int j = 0; j < colorPanel.getComponentCount(); j++) {
                    ((JButton) colorPanel.getComponent(j)).setBorder(BorderFactory.createLineBorder(Color.GRAY));
                }
                colorBtn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
            });
            if (i == 0) {
                colorBtn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
            }
            colorPanel.add(colorBtn);
        }
        placementPanel.add(colorPanel, gbc);
        
        return placementPanel;
    }
    
    private void toggleUI() {
        uiVisible = !uiVisible;
        controlPanel.setVisible(uiVisible);
        if (!uiVisible) {
            setSize(getWidth() - controlPanel.getWidth(), getHeight());
        } else {
            pack();
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ParticleLife frame = new ParticleLife();
            frame.setVisible(true);
        });
    }
}

class SimulationPanel extends JPanel {
    static final int MAX_PARTICLES = 10000;
    static final double DT = 0.02;
    static final double FRICTION_HALF_LIFE = 0.04;
    static final double R_MAX = 0.1;
    static final int M = 6;
    static final double BETA = 0.3;

    private final double frictionFactor;
    private double[][] matrix;

    private int particleCount = 0;
    private final int[] colors = new int[MAX_PARTICLES];
    private final float[] positionsX = new float[MAX_PARTICLES];
    private final float[] positionsY = new float[MAX_PARTICLES];
    private final float[] velocitiesX = new float[MAX_PARTICLES];
    private final float[] velocitiesY = new float[MAX_PARTICLES];

    final Color[] particleColors;
    private int selectedSpecies = 0;
    private int placeCount = 100;
    private String placementShape = "Circle";
    private final Random rand = new Random();
    
    private Point dragStart = null;
    private Point dragEnd = null;
    private boolean paused = false;

    void randomizeEverything() {
        matrix = makeRandomMatrix();

        int newM = 3 + rand.nextInt(M - 2); // pick 3..6 species
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < M; i++) indices.add(i);
        Collections.shuffle(indices, rand);

        // assign random colors from default colors
        int[] speciesMapping = new int[newM];
        for (int i = 0; i < newM; i++) speciesMapping[i] = indices.get(i);

        // Clear current particles
        particleCount = 0;

        int count = 1000 + rand.nextInt(4000);
        for (int i = 0; i < count && particleCount < MAX_PARTICLES; i++) {
            int species = speciesMapping[rand.nextInt(newM)];
            colors[particleCount] = species;

            positionsX[particleCount] = rand.nextFloat();
            positionsY[particleCount] = rand.nextFloat();
            velocitiesX[particleCount] = (rand.nextFloat() - 0.5f) * 0.01f;
            velocitiesY[particleCount] = (rand.nextFloat() - 0.5f) * 0.01f;
            particleCount++;
        }
    }

    private static final Color[] defaultSpeciesColors = new Color[] {
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.GREEN,
            Color.CYAN,
            Color.MAGENTA
    };

    public SimulationPanel() {
        setPreferredSize(new Dimension(900, 700));
        setBackground(Color.BLACK);

        frictionFactor = Math.pow(0.5, DT / FRICTION_HALF_LIFE);
        matrix = makeRandomMatrix();

        particleColors = new Color[M];
        for (int i = 0; i < M; i++) {
            particleColors[i] = defaultSpeciesColors[i];
        }

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStart = e.getPoint();
                dragEnd = e.getPoint();
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                dragEnd = e.getPoint();
                repaint();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragStart != null && dragEnd != null) {
                    addParticlesInShape(dragStart, dragEnd);
                }
                dragStart = null;
                dragEnd = null;
                repaint();
            }
        };
        
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);

        Timer timer = new Timer(16, e -> {
            if (!paused) {
                updateParticles();
            }
            repaint();
        });
        timer.start();
    }

    private double[][] makeRandomMatrix() {
        double[][] mat = new double[M][M];
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < M; j++) {
                // smaller random forces (-0.5 to +0.5 instead of -1 to +1)
                mat[i][j] = (rand.nextDouble() * 1.0) - 0.5;
            }
        }
        return mat;
    }

    void randomizeMatrix() {
        matrix = makeRandomMatrix();
    }

    double[][] getMatrix() {
        return matrix;
    }

    void setMatrixValue(int i, int j, double value) {
        matrix[i][j] = Math.max(-1, Math.min(1, value));
    }

    void setSelectedSpecies(int species) {
        selectedSpecies = species;
    }

    void setParticleCount(int count) {
        placeCount = count;
    }

    void setPlacementShape(String shape) {
        placementShape = shape;
    }
    
    boolean togglePause() {
        paused = !paused;
        return paused;
    }

    void clearParticles() {
        particleCount = 0;
    }
    
    void generateRandomParticles(int count) {
        clearParticles();
        for (int i = 0; i < count && particleCount < MAX_PARTICLES; i++) {
            colors[particleCount] = rand.nextInt(M);
            positionsX[particleCount] = rand.nextFloat();
            positionsY[particleCount] = rand.nextFloat();
            velocitiesX[particleCount] = 0;
            velocitiesY[particleCount] = 0;
            particleCount++;
        }
    }

    private void addParticlesInShape(Point start, Point end) {
        double x1 = (double) start.x / getWidth();
        double y1 = (double) start.y / getHeight();
        double x2 = (double) end.x / getWidth();
        double y2 = (double) end.y / getHeight();
        
        double centerX = (x1 + x2) / 2;
        double centerY = (y1 + y2) / 2;
        double width = Math.abs(x2 - x1);
        double height = Math.abs(y2 - y1);
        double radius = Math.hypot(width, height) / 2;
        
        for (int i = 0; i < placeCount && particleCount < MAX_PARTICLES; i++) {
            double px, py;
            
            switch (placementShape) {
                case "Circle":
                    double angle = rand.nextDouble() * 2 * Math.PI;
                    double r = Math.sqrt(rand.nextDouble()) * radius;
                    px = centerX + r * Math.cos(angle);
                    py = centerY + r * Math.sin(angle);
                    break;
                    
                case "Square":
                    px = x1 + rand.nextDouble() * width;
                    py = y1 + rand.nextDouble() * height;
                    break;
                    
                case "Line":
                    double t = rand.nextDouble();
                    px = x1 + t * (x2 - x1);
                    py = y1 + t * (y2 - y1);
                    break;
                    
                case "Ring":
                    double ringAngle = rand.nextDouble() * 2 * Math.PI;
                    double ringR = radius * (0.7 + rand.nextDouble() * 0.3);
                    px = centerX + ringR * Math.cos(ringAngle);
                    py = centerY + ringR * Math.sin(ringAngle);
                    break;
                    
                default:
                    px = centerX;
                    py = centerY;
            }
            
            colors[particleCount] = selectedSpecies;
            positionsX[particleCount] = (float) px;
            positionsY[particleCount] = (float) py;
            velocitiesX[particleCount] = 0;
            velocitiesY[particleCount] = 0;
            particleCount++;
        }
    }

    private double force(double r, double a) {
        if (r < BETA) {
            return r / BETA - 1;
        } else if (r < 1) {
            return a * (1 - Math.abs(2 * r - 1 - BETA) / (1 - BETA));
        }
        return 0;
    }

    private void updateParticles() {
        if (particleCount == 0) return;
        
        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        double aspect = (double) width / height;
        double gridSize = R_MAX;

        // number of columns/rows in normalized [0..1) space
        int cols = (int) Math.ceil(1.0 / gridSize);
        int rows = cols; // square grid in normalized coords

        Map<String, List<Integer>> grid = new HashMap<>();
        for (int i = 0; i < particleCount; i++) {
            int gx = (int) Math.floor(positionsX[i] / gridSize);
            int gy = (int) Math.floor(positionsY[i] / gridSize);
            // wrap indices into [0..cols-1] and [0..rows-1]
            gx = ((gx % cols) + cols) % cols;
            gy = ((gy % rows) + rows) % rows;
            String key = gx + "," + gy;
            grid.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
        }

        for (int i = 0; i < particleCount; i++) {
            double fx = 0, fy = 0;
            int gx = (int) Math.floor(positionsX[i] / gridSize);
            int gy = (int) Math.floor(positionsY[i] / gridSize);
            gx = ((gx % cols) + cols) % cols;
            gy = ((gy % rows) + rows) % rows;

            // check the 3x3 neighbor cells but with wrap-around indices
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int nx = gx + dx;
                    int ny = gy + dy;
                    // wrap neighbor indices
                    nx = ((nx % cols) + cols) % cols;
                    ny = ((ny % rows) + rows) % rows;
                    String key = nx + "," + ny;
                    List<Integer> cell = grid.get(key);
                    if (cell == null) continue;

                    for (int j : cell) {
                        if (i == j) continue;

                        double rx = positionsX[j] - positionsX[i];
                        double ry = positionsY[j] - positionsY[i];

                        // --- FIXED wrap-around (toroidal) distance ---
                        if (rx > 0.5)  rx -= 1.0;
                        if (rx < -0.5) rx += 1.0;
                        if (ry > 0.5)  ry -= 1.0;
                        if (ry < -0.5) ry += 1.0;

                        // Correct aspect ratio scaling *after* wrapping
                        double dxAspect = rx * aspect;
                        double dyAspect = ry;
                        double r = Math.hypot(dxAspect, dyAspect);

                        if (r > 0 && r < R_MAX) {
                            double f = force(r / R_MAX, matrix[colors[i]][colors[j]]);
                            fx += (dxAspect / r) * f;
                            fy += (dyAspect / r) * f;
                        }
                    }
                }
            }

            velocitiesX[i] = (float) (velocitiesX[i] * frictionFactor + fx * DT / aspect);
            velocitiesY[i] = (float) (velocitiesY[i] * frictionFactor + fy * DT);
        }

        for (int i = 0; i < particleCount; i++) {
            positionsX[i] = (float) ((positionsX[i] + velocitiesX[i] * DT + 1) % 1);
            positionsY[i] = (float) ((positionsY[i] + velocitiesY[i] * DT + 1) % 1);
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int width = getWidth();
        int height = getHeight();

        for (int i = 0; i < particleCount; i++) {
            int x = (int) (positionsX[i] * width);
            int y = (int) (positionsY[i] * height);
            g2d.setColor(particleColors[colors[i]]);
            g2d.fillRect(x, y, 2, 2);
        }
        
        // Draw drag preview
        if (dragStart != null && dragEnd != null) {
            g2d.setColor(particleColors[selectedSpecies]);
            int x1 = dragStart.x;
            int y1 = dragStart.y;
            int x2 = dragEnd.x;
            int y2 = dragEnd.y;
            
            switch (placementShape) {
                case "Circle":
                    int cx = (x1 + x2) / 2;
                    int cy = (y1 + y2) / 2;
                    int r = (int) Math.hypot(x2 - x1, y2 - y1) / 2;
                    g2d.drawOval(cx - r, cy - r, r * 2, r * 2);
                    break;
                case "Square":
                    g2d.drawRect(Math.min(x1, x2), Math.min(y1, y2), 
                               Math.abs(x2 - x1), Math.abs(y2 - y1));
                    break;
                case "Line":
                    g2d.drawLine(x1, y1, x2, y2);
                    break;
                case "Ring":
                    int rcx = (x1 + x2) / 2;
                    int rcy = (y1 + y2) / 2;
                    int rr = (int) Math.hypot(x2 - x1, y2 - y1) / 2;
                    g2d.drawOval(rcx - rr, rcy - rr, rr * 2, rr * 2);
                    g2d.drawOval(rcx - (int)(rr*0.7), rcy - (int)(rr*0.7), 
                               (int)(rr*1.4), (int)(rr*1.4));
                    break;
            }
        }
        
        // Draw particle count
        g2d.setColor(Color.WHITE);
        g2d.drawString("Particles: " + particleCount, 10, 20);
    }
}

class MatrixPanel extends JPanel {
    private final SimulationPanel simPanel;
    private final JTextField[][] textFields;

    public MatrixPanel(SimulationPanel simPanel) {
        this.simPanel = simPanel;
        this.textFields = new JTextField[SimulationPanel.M][SimulationPanel.M];
        
        setBackground(new Color(40, 40, 40));
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), 
            "Interaction Matrix",
            0, 0, null, Color.WHITE));
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        
        // Column headers
        for (int j = 0; j < SimulationPanel.M; j++) {
            gbc.gridx = j + 1;
            gbc.gridy = 0;
            JLabel header = new JLabel(String.valueOf(j));
            header.setPreferredSize(new Dimension(60, 25));
            header.setOpaque(true);
            header.setHorizontalAlignment(JLabel.CENTER);
            header.setBackground(Color.getHSBColor((float) j / SimulationPanel.M, 1.0f, 1.0f));
            header.setForeground(Color.BLACK);
            add(header, gbc);
        }
        
        // Row headers and text fields
        for (int i = 0; i < SimulationPanel.M; i++) {
            gbc.gridx = 0;
            gbc.gridy = i + 1;
            JLabel rowHeader = new JLabel(String.valueOf(i));
            rowHeader.setPreferredSize(new Dimension(25, 25));
            rowHeader.setOpaque(true);
            rowHeader.setHorizontalAlignment(JLabel.CENTER);
            rowHeader.setBackground(Color.getHSBColor((float) i / SimulationPanel.M, 1.0f, 1.0f));
            rowHeader.setForeground(Color.BLACK);
            add(rowHeader, gbc);
            
            for (int j = 0; j < SimulationPanel.M; j++) {
                final int row = i;
                final int col = j;
                
                gbc.gridx = j + 1;
                JTextField field = new JTextField(String.format("%.2f", simPanel.getMatrix()[i][j]));
                field.setPreferredSize(new Dimension(60, 25));
                field.setHorizontalAlignment(JTextField.CENTER);
                
                field.addActionListener(e -> {
                    try {
                        double value = Double.parseDouble(field.getText());
                        simPanel.setMatrixValue(row, col, value);
                        field.setText(String.format("%.2f", simPanel.getMatrix()[row][col]));
                    } catch (NumberFormatException ex) {
                        field.setText(String.format("%.2f", simPanel.getMatrix()[row][col]));
                    }
                });
                
                textFields[i][j] = field;
                add(field, gbc);
            }
        }
    }
    
    void updateFields() {
        double[][] matrix = simPanel.getMatrix();
        for (int i = 0; i < SimulationPanel.M; i++) {
            for (int j = 0; j < SimulationPanel.M; j++) {
                textFields[i][j].setText(String.format("%.2f", matrix[i][j]));
            }
        }
    }
}