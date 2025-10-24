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
import java.awt.RenderingHints;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
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
        simulationPanel.generateRandomParticles(5000);
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
            simulationPanel.generateRandomParticles(10000);
            matrixPanel.updateFields();
        });
        topPanel.add(randomButton);
        
        // Boundary mode toggle
        JButton boundaryButton = new JButton("Mode: Wrap");
        boundaryButton.addActionListener(e -> {
            String mode = simulationPanel.cycleBoundaryMode();
            boundaryButton.setText("Mode: " + mode);
        });
        topPanel.add(boundaryButton);
        
        
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
        
        JLabel infoLabel = new JLabel("<html><b>Drag to place | Right-drag to pan | Scroll to zoom</b></html>");
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
                simulationPanel.setParticleCount(Math.max(1, Math.min(5000, count)));
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
    static final int MAX_PARTICLES = 50000;
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
    
    // Camera/viewport controls
    private double cameraX = 0.5;
    private double cameraY = 0.5;
    private double zoom = 1.0;
    private Point lastPanPoint = null;
    
    // Boundary mode
    private BoundaryMode boundaryMode = BoundaryMode.WRAP;
    
    // Performance optimization: buffered image
    private BufferedImage buffer;
    private Graphics2D bufferGraphics;
    
    // FPS tracking
    private long lastFrameTime = System.nanoTime();
    private int frameCount = 0;
    private double fps = 60.0;
    
    enum BoundaryMode {
        WRAP,
        CLOSED,
        INFINITE
    }

    void randomizeEverything() {
        matrix = makeRandomMatrix();

        int newM = 3 + rand.nextInt(M - 2);
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < M; i++) indices.add(i);
        Collections.shuffle(indices, rand);

        int[] speciesMapping = new int[newM];
        for (int i = 0; i < newM; i++) speciesMapping[i] = indices.get(i);

        particleCount = 0;

        int count = 5000 + rand.nextInt(15000);
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
                if (e.getButton() == MouseEvent.BUTTON3) {
                    lastPanPoint = e.getPoint();
                } else {
                    dragStart = e.getPoint();
                    dragEnd = e.getPoint();
                }
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastPanPoint != null) {
                    int dx = e.getX() - lastPanPoint.x;
                    int dy = e.getY() - lastPanPoint.y;
                    
                    cameraX -= (double) dx / (getWidth() * zoom);
                    cameraY -= (double) dy / (getHeight() * zoom);
                    
                    lastPanPoint = e.getPoint();
                } else if (dragStart != null) {
                    dragEnd = e.getPoint();
                }
                repaint();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (lastPanPoint != null) {
                    lastPanPoint = null;
                } else if (dragStart != null && dragEnd != null) {
                    addParticlesInShape(dragStart, dragEnd);
                    dragStart = null;
                    dragEnd = null;
                }
                repaint();
            }
            
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                Point2D.Double mouseWorld = screenToWorld(e.getX(), e.getY());
                
                double zoomFactor = e.getPreciseWheelRotation() > 0 ? 0.9 : 1.1;
                zoom *= zoomFactor;
                zoom = Math.max(0.1, Math.min(10.0, zoom));
                
                Point2D.Double mouseWorldAfter = screenToWorld(e.getX(), e.getY());
                
                cameraX += mouseWorld.x - mouseWorldAfter.x;
                cameraY += mouseWorld.y - mouseWorldAfter.y;
                
                repaint();
            }
        };
        
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        addMouseWheelListener(mouseHandler);

        Timer timer = new Timer(16, e -> {
            if (!paused) {
                updateParticles();
            }
            repaint();
        });
        timer.start();
    }
    
    private Point2D.Double screenToWorld(int screenX, int screenY) {
        double worldX = cameraX + (screenX - getWidth() / 2.0) / (getWidth() * zoom);
        double worldY = cameraY + (screenY - getHeight() / 2.0) / (getHeight() * zoom);
        return new Point2D.Double(worldX, worldY);
    }
    
    private Point worldToScreen(double worldX, double worldY) {
        int screenX = (int) ((worldX - cameraX) * getWidth() * zoom + getWidth() / 2.0);
        int screenY = (int) ((worldY - cameraY) * getHeight() * zoom + getHeight() / 2.0);
        return new Point(screenX, screenY);
    }

    private double[][] makeRandomMatrix() {
        double[][] mat = new double[M][M];
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < M; j++) {
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
    
    String cycleBoundaryMode() {
        switch (boundaryMode) {
            case WRAP:
                boundaryMode = BoundaryMode.CLOSED;
                return "Closed";
            case CLOSED:
                boundaryMode = BoundaryMode.INFINITE;
                return "Infinite";
            case INFINITE:
                boundaryMode = BoundaryMode.WRAP;
                return "Wrap";
            default:
                return "Wrap";
        }
    }

    void clearParticles() {
        particleCount = 0;
    }
    
    void generateRandomParticles(int count) {
        clearParticles();
        count = Math.min(count, MAX_PARTICLES);
        for (int i = 0; i < count; i++) {
            colors[i] = rand.nextInt(M);
            positionsX[i] = rand.nextFloat();
            positionsY[i] = rand.nextFloat();
            velocitiesX[i] = 0;
            velocitiesY[i] = 0;
        }
        particleCount = count;
    }

    private void addParticlesInShape(Point start, Point end) {
        Point2D.Double worldStart = screenToWorld(start.x, start.y);
        Point2D.Double worldEnd = screenToWorld(end.x, end.y);
        
        double x1 = worldStart.x;
        double y1 = worldStart.y;
        double x2 = worldEnd.x;
        double y2 = worldEnd.y;
        
        double centerX = (x1 + x2) / 2;
        double centerY = (y1 + y2) / 2;
        double width = Math.abs(x2 - x1);
        double height = Math.abs(y2 - y1);
        double radius = Math.hypot(width, height) / 2;
        
        int toAdd = Math.min(placeCount, MAX_PARTICLES - particleCount);
        
        for (int i = 0; i < toAdd; i++) {
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

        // Spatial grid for optimized neighbor search
        Map<Long, List<Integer>> grid = new HashMap<>(particleCount / 4);
        
        if (boundaryMode == BoundaryMode.WRAP) {
            int cols = (int) Math.ceil(1.0 / gridSize);
            int rows = cols;
            
            for (int i = 0; i < particleCount; i++) {
                int gx = (int) Math.floor(positionsX[i] / gridSize);
                int gy = (int) Math.floor(positionsY[i] / gridSize);
                gx = ((gx % cols) + cols) % cols;
                gy = ((gy % rows) + rows) % rows;
                long key = ((long) gx << 32) | (gy & 0xFFFFFFFFL);
                grid.computeIfAbsent(key, k -> new ArrayList<>(8)).add(i);
            }
            
            // Calculate forces
            for (int i = 0; i < particleCount; i++) {
                double fx = 0, fy = 0;
                int gx = (int) Math.floor(positionsX[i] / gridSize);
                int gy = (int) Math.floor(positionsY[i] / gridSize);
                gx = ((gx % cols) + cols) % cols;
                gy = ((gy % rows) + rows) % rows;

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int nx = ((gx + dx) % cols + cols) % cols;
                        int ny = ((gy + dy) % rows + rows) % rows;
                        long key = ((long) nx << 32) | (ny & 0xFFFFFFFFL);
                        List<Integer> cell = grid.get(key);
                        if (cell == null) continue;

                        for (int j : cell) {
                            if (i == j) continue;

                            double rx = positionsX[j] - positionsX[i];
                            double ry = positionsY[j] - positionsY[i];

                            if (rx > 0.5)  rx -= 1.0;
                            if (rx < -0.5) rx += 1.0;
                            if (ry > 0.5)  ry -= 1.0;
                            if (ry < -0.5) ry += 1.0;

                            double dxAspect = rx * aspect;
                            double dyAspect = ry;
                            double r = Math.hypot(dxAspect, dyAspect);

                            if (r > 0 && r < R_MAX) {
                                double f = force(r / R_MAX, matrix[colors[i]][colors[j]]);
                                double invR = 1.0 / r;
                                fx += dxAspect * invR * f;
                                fy += dyAspect * invR * f;
                            }
                        }
                    }
                }

                velocitiesX[i] = (float) (velocitiesX[i] * frictionFactor + fx * DT / aspect);
                velocitiesY[i] = (float) (velocitiesY[i] * frictionFactor + fy * DT);
            }
        } else {
            // Closed or Infinite mode
            for (int i = 0; i < particleCount; i++) {
                int gx = (int) Math.floor(positionsX[i] / gridSize);
                int gy = (int) Math.floor(positionsY[i] / gridSize);
                long key = ((long) gx << 32) | (gy & 0xFFFFFFFFL);
                grid.computeIfAbsent(key, k -> new ArrayList<>(8)).add(i);
            }

            for (int i = 0; i < particleCount; i++) {
                double fx = 0, fy = 0;
                int gx = (int) Math.floor(positionsX[i] / gridSize);
                int gy = (int) Math.floor(positionsY[i] / gridSize);

                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int nx = gx + dx;
                        int ny = gy + dy;
                        long key = ((long) nx << 32) | (ny & 0xFFFFFFFFL);
                        List<Integer> cell = grid.get(key);
                        if (cell == null) continue;

                        for (int j : cell) {
                            if (i == j) continue;

                            double rx = positionsX[j] - positionsX[i];
                            double ry = positionsY[j] - positionsY[i];

                            double dxAspect = rx * aspect;
                            double dyAspect = ry;
                            double r = Math.hypot(dxAspect, dyAspect);

                            if (r > 0 && r < R_MAX) {
                                double f = force(r / R_MAX, matrix[colors[i]][colors[j]]);
                                double invR = 1.0 / r;
                                fx += dxAspect * invR * f;
                                fy += dyAspect * invR * f;
                            }
                        }
                    }
                }

                velocitiesX[i] = (float) (velocitiesX[i] * frictionFactor + fx * DT / aspect);
                velocitiesY[i] = (float) (velocitiesY[i] * frictionFactor + fy * DT);
            }
        }

        // Update positions
        for (int i = 0; i < particleCount; i++) {
            positionsX[i] += velocitiesX[i] * DT;
            positionsY[i] += velocitiesY[i] * DT;
            
            if (boundaryMode == BoundaryMode.WRAP) {
                positionsX[i] = (float) ((positionsX[i] % 1 + 1) % 1);
                positionsY[i] = (float) ((positionsY[i] % 1 + 1) % 1);
            } else if (boundaryMode == BoundaryMode.CLOSED) {
                if (positionsX[i] < 0) {
                    positionsX[i] = 0;
                    velocitiesX[i] *= -0.5f;
                } else if (positionsX[i] > 1) {
                    positionsX[i] = 1;
                    velocitiesX[i] *= -0.5f;
                }
                
                if (positionsY[i] < 0) {
                    positionsY[i] = 0;
                    velocitiesY[i] *= -0.5f;
                } else if (positionsY[i] > 1) {
                    positionsY[i] = 1;
                    velocitiesY[i] *= -0.5f;
                }
            }
        }
        
        // FPS calculation
        frameCount++;
        long currentTime = System.nanoTime();
        if (currentTime - lastFrameTime >= 1_000_000_000L) {
            fps = frameCount * 1_000_000_000.0 / (currentTime - lastFrameTime);
            frameCount = 0;
            lastFrameTime = currentTime;
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Disable anti-aliasing for better performance
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        int width = getWidth();
        int height = getHeight();

        // Draw boundary in closed mode
        if (boundaryMode == BoundaryMode.CLOSED) {
            Point topLeft = worldToScreen(0, 0);
            Point bottomRight = worldToScreen(1, 1);
            g2d.setColor(new Color(60, 60, 60));
            g2d.drawRect(topLeft.x, topLeft.y, 
                        bottomRight.x - topLeft.x, 
                        bottomRight.y - topLeft.y);
        }

        // Draw particles
        int size = Math.max(1, (int) (2 * zoom));
        for (int i = 0; i < particleCount; i++) {
            Point screenPos = worldToScreen(positionsX[i], positionsY[i]);
            
            if (screenPos.x >= -10 && screenPos.x <= width + 10 &&
                screenPos.y >= -10 && screenPos.y <= height + 10) {
                g2d.setColor(particleColors[colors[i]]);
                g2d.fillRect(screenPos.x, screenPos.y, size, size);
            }
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
        
        // Draw info text
        g2d.setColor(Color.WHITE);
        g2d.drawString("Particles: " + particleCount, 10, 20);
        String modeStr = boundaryMode == BoundaryMode.WRAP ? "Wrap" : 
                        (boundaryMode == BoundaryMode.CLOSED ? "Closed" : "Infinite");
        g2d.drawString(String.format("Zoom: %.2fx | Mode: %s | FPS: %.1f", zoom, modeStr, fps), 10, 40);
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
            header.setBackground(simPanel.particleColors[j]);
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
            rowHeader.setBackground(simPanel.particleColors[i]);
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
