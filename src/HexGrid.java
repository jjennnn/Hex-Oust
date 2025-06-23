import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.Stack;

/**
 * Modified Point class with proper equals and hashCode overrides.
 * Represents a 2D point with double precision.
 */
class Point {
    /**
     * Constructs a Point with specified x and y coordinates.
     *
     * @param x X-coordinate
     * @param y Y-coordinate
     */
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public final double x;
    public final double y;


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // same reference
        if (obj == null || getClass() != obj.getClass()) return false; // not same type
        Point point = (Point) obj;
        return Double.compare(point.x, x) == 0 &&
                Double.compare(point.y, y) == 0;
    }


    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
/**
 * Represents a hexagon's position in cube coordinates.
 */
class HexCube {
    /**
     * Constructs a HexCube at given q, r, s coordinates.
     * Throws an exception if q + r + s != 0.
     *
     * @param q coordinate q
     * @param r coordinate r
     * @param s coordinate s
     */

    public HexCube(int q, int r, int s) {
        this.q = q;
        this.r = r;
        this.s = s;
        if (q + r + s != 0)
            throw new IllegalArgumentException("q + r + s must be 0");
    }
    public final int q;
    public final int r;
    public final int s;

    /**
     * Adds two HexCubes.
     *
     * @param b HexCube to add
     * @return new HexCube result
     */

    public HexCube add(HexCube b) {
        return new HexCube(q + b.q, r + b.r, s + b.s);
    }

    /**
     * Subtracts one HexCube from another.
     *
     * @param b HexCube to subtract
     * @return new HexCube result
     */

    public HexCube subtract(HexCube b) {
        return new HexCube(q - b.q, r - b.r, s - b.s);
    }
    /**
     * Static list of hexagonal directions.
     */
    static public ArrayList<HexCube> directions = new ArrayList<HexCube>() {{
        add(new HexCube(1, 0, -1));
        add(new HexCube(1, -1, 0));
        add(new HexCube(0, -1, 1));
        add(new HexCube(-1, 0, 1));
        add(new HexCube(-1, 1, 0));
        add(new HexCube(0, 1, -1));
    }};

    static public HexCube direction(int direction) {
        return HexCube.directions.get(direction);
    }
    /**
     * Returns a neighboring HexCube in a given direction.
     *
     * @param direction index 0–5
     * @return neighboring HexCube
     */
    public HexCube neighbor(int direction) {
        return add(HexCube.direction(direction));
    }
    /**
     * Calculates the length (distance to origin) of this HexCube.
     *
     * @return distance to origin
     */
    public int length() {
        return (int)((Math.abs(q) + Math.abs(r) + Math.abs(s)) / 2);
    }
    /**
     * Computes the distance between two HexCubes.
     *
     * @param b target HexCube
     * @return distance as integer
     */
    public int distance(HexCube b) {
        return subtract(b).length();
    }
}
/**
 * Represents a hexagonal position with fractional cube coordinates.
 * Useful for converting pixel positions into hex positions.
 */
class FractionalHexCube {
    /**
     * Constructs a FractionalHexCube at the specified q, r, s coordinates.
     * Throws an exception if q + r + s is not approximately zero.
     *
     * @param q fractional q-coordinate
     * @param r fractional r-coordinate
     * @param s fractional s-coordinate
     */
    public FractionalHexCube(double q, double r, double s) {
        this.q = q;
        this.r = r;
        this.s = s;
        if (Math.round(q + r + s) != 0)
            throw new IllegalArgumentException("q + r + s must be 0");
    }
    public final double q;
    public final double r;
    public final double s;
    /**
     * Rounds fractional cube coordinates to the nearest HexCube.
     *
     * @return HexCube rounded to nearest integer coordinates
     */
    public HexCube hexRound() {
        int qi = (int)(Math.round(q));
        int ri = (int)(Math.round(r));
        int si = (int)(Math.round(s));
        double q_diff = Math.abs(qi - q);
        double r_diff = Math.abs(ri - r);
        double s_diff = Math.abs(si - s);
        if (q_diff > r_diff && q_diff > s_diff) {
            qi = -ri - si;
        } else if (r_diff > s_diff) {
            ri = -qi - si;
        } else {
            si = -qi - ri;
        }
        return new HexCube(qi, ri, si);
    }
}
/**
 * Describes the orientation matrix for converting between hex coordinates and pixel positions.
 * Contains forward and backward matrix coefficients and a start angle.
 */
class Orientation {
    /**
     * Constructs an Orientation with specified matrix values.
     *
     * @param f0 forward matrix value
     * @param f1 forward matrix value
     * @param f2 forward matrix value
     * @param f3 forward matrix value
     * @param b0 backward matrix value
     * @param b1 backward matrix value
     * @param b2 backward matrix value
     * @param b3 backward matrix value
     * @param start_angle starting angle for hex corner calculation
     */
    public Orientation(double f0, double f1, double f2, double f3,
                       double b0, double b1, double b2, double b3,
                       double start_angle) {
        this.f0 = f0;
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.b0 = b0;
        this.b1 = b1;
        this.b2 = b2;
        this.b3 = b3;
        this.start_angle = start_angle;
    }
    public final double f0;
    public final double f1;
    public final double f2;
    public final double f3;
    public final double b0;
    public final double b1;
    public final double b2;
    public final double b3;
    public final double start_angle;
}
/**
 * Manages the layout configuration for mapping hex grid coordinates to 2D pixel positions.
 */
class Layout {

    /**
     * Constructs a Layout with given orientation, hex size, and origin position.
     *
     * @param orientation orientation matrix
     * @param size        size of each hexagon
     * @param origin      origin point of the grid
     */

    public Layout(Orientation orientation, Point size, Point origin) {
        this.orientation = orientation;
        this.size = size;
        this.origin = origin;
    }
    public final Orientation orientation;
    public final Point size;
    public final Point origin;
    /**
     * Static flat-top orientation preset.
     */
    static public Orientation flat = new Orientation(3.0 / 2.0, 0.0, Math.sqrt(3.0) / 2.0, Math.sqrt(3.0),
            2.0 / 3.0, 0.0, -1.0 / 3.0, Math.sqrt(3.0) / 3.0, 0.0);
    /**
     * Converts a hex coordinate to its corresponding pixel position.
     *
     * @param h HexCube position
     * @return 2D pixel Point
     */
    public Point hexToPixel(HexCube h) {
        Orientation M = orientation;
        double x = (M.f0 * h.q + M.f1 * h.r) * size.x;
        double y = (M.f2 * h.q + M.f3 * h.r) * size.y;
        return new Point(x + origin.x, y + origin.y);
    }
    /**
     * Converts a pixel position to fractional hex coordinates.
     *
     * @param p pixel position
     * @return corresponding FractionalHexCube
     */
    public FractionalHexCube pixelToHex(Point p) {
        Orientation M = orientation;
        Point pt = new Point((p.x - origin.x) / size.x, (p.y - origin.y) / size.y);
        double q = M.b0 * pt.x + M.b1 * pt.y;
        double r = M.b2 * pt.x + M.b3 * pt.y;
        return new FractionalHexCube(q, r, -q - r);
    }
    /**
     * Computes the offset of a given hex corner relative to its center.
     *
     * @param corner index of corner (0-5)
     * @return corner Point offset
     */
    public Point hexCornerOffset(int corner) {
        double angle = 2.0 * Math.PI * (orientation.start_angle - corner) / 6.0;
        return new Point(size.x * Math.cos(angle), size.y * Math.sin(angle));
    }
    /**
     * Computes the screen positions of the corners of a hexagon.
     *
     * @param h HexCube coordinate
     * @return list of corner Points
     */
    public ArrayList<Point> polygonCorners(HexCube h) {
        ArrayList<Point> corners = new ArrayList<>();
        Point center = hexToPixel(h);
        for (int i = 0; i < 6; i++) {
            Point offset = hexCornerOffset(i);
            corners.add(new Point(center.x + offset.x, center.y + offset.y));
        }
        return corners;
    }
}
/**
 * JPanel representing an interactive Hexagonal game board.
 * Manages rendering, move validation, captures, undo functionality, and UI controls.
 */
public class HexGrid extends JPanel {
    ArrayList<ArrayList<Point>> grid;
    ArrayList<Point> redStones = new ArrayList<>();
    ArrayList<Point> blueStones = new ArrayList<>();
    boolean isRedTurn = true;
    private Point hoveredCell = null;
    private JButton exitButton; // Exit button
    private JButton newGameButton; // New Game button
    boolean gameExited = false;

    Stack<ArrayList<Point>> redStonesHistory = new Stack<>();
    Stack<ArrayList<Point>> blueStonesHistory = new Stack<>();
    Stack<Boolean> turnHistory = new Stack<>();
    private JButton undoButton;
    /**
     * Constructs a HexGrid panel using a provided grid of hexagons.
     *
     * @param hexagons list of hexagon vertex lists
     */
    public HexGrid(ArrayList<ArrayList<Point>> hexagons) {
        this.grid = hexagons;

        // Create the Exit button
        exitButton = new JButton("Exit");
        exitButton.setBounds(20, 20, 100, 40); // Position it at the top right
        exitButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        exitButton.setBackground(Color.RED);
        exitButton.setForeground(Color.WHITE);

        // Create the New Game button
        newGameButton = new JButton("New Game");
        newGameButton.setBounds(140, 20, 120, 40); // Position it next to the Exit button
        newGameButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        newGameButton.setBackground(Color.GREEN);
        newGameButton.setForeground(Color.BLACK);

        this.exitButton = exitButton;

        undoButton = new JButton("Undo");
        undoButton.setBounds(280, 20, 100, 40); // Position it next to the New Game button
        undoButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        undoButton.setBackground(Color.ORANGE);
        undoButton.setForeground(Color.BLACK);
        undoButton.setEnabled(false);


        newGameButton.addActionListener(e -> {

            int response = JOptionPane.showConfirmDialog(
                    null,
                    "Do you want to start a new game?",
                    "New Game",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );


            if (response == JOptionPane.YES_OPTION) {
                // Reset game state completely
                redStones.clear();
                blueStones.clear();
                isRedTurn = true;
                hoveredCell = null;
                gameExited = false;


                redStonesHistory.clear();
                blueStonesHistory.clear();
                turnHistory.clear();
                undoButton.setEnabled(false);


                invalidate();
                validate();
                repaint();


                JOptionPane.showMessageDialog(null,
                        "New game started. Red player's turn.",
                        "New Game",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        undoButton.addActionListener(e -> undoLastMove());

        exitButton.addActionListener(e -> exitGame());


        setLayout(null);
        add(exitButton);
        add(undoButton);
        add(newGameButton);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point click = new Point(e.getX(), e.getY());

                for (ArrayList<Point> hexagon : grid) {
                    Point center = getHexCenter(hexagon);
                    double distance = Math.sqrt(Math.pow(center.x - click.x, 2) + Math.pow(center.y - click.y, 2));
                    if (distance <= 20) {
                        // Check if cell is empty
                        if (redStones.contains(center) || blueStones.contains(center)) {
                            JOptionPane.showMessageDialog(null, "Cell is already occupied", "Error", JOptionPane.ERROR_MESSAGE);
                            break;
                        }

                        // First stone placement is always allowed
                        if (redStones.isEmpty() && blueStones.isEmpty()) {
                            saveGameState();
                            if (isRedTurn) {
                                redStones.add(center);
                            } else {
                                blueStones.add(center);
                            }
                            isRedTurn = !isRedTurn;
                            repaint();
                            break;
                        }


                        ArrayList<Point> neighbors = getNeighborCenters(center);
                        boolean hasAnyNeighbor = false;
                        boolean hasEnemyNeighbor = false;
                        boolean hasFriendlyNeighbor = false;

                        for (Point neighbor : neighbors) {
                            if (redStones.contains(neighbor) || blueStones.contains(neighbor)) {
                                hasAnyNeighbor = true;
                                if ((isRedTurn && blueStones.contains(neighbor)) ||
                                        (!isRedTurn && redStones.contains(neighbor))) {
                                    hasEnemyNeighbor = true;
                                } else {
                                    hasFriendlyNeighbor = true;
                                }
                            }
                        }

                        // Placement rules:
                        // 1. Always allowed if no neighbors (isolated placement)
                        // 2. Allowed if adjacent to enemy stones
                        // 3. Allowed if adjacent only to friendly stones that are themselves connected to enemies
                        boolean validPlacement = !hasAnyNeighbor || hasEnemyNeighbor;

                        // If only friendly neighbors, check if they're connected to enemies
                        if (!validPlacement && hasFriendlyNeighbor) {
                            for (Point neighbor : neighbors) {
                                if ((isRedTurn && redStones.contains(neighbor)) ||
                                        (!isRedTurn && blueStones.contains(neighbor))) {
                                    ArrayList<Point> friendNeighbors = getNeighborCenters(neighbor);
                                    for (Point friendNeighbor : friendNeighbors) {
                                        if ((isRedTurn && blueStones.contains(friendNeighbor)) ||
                                                (!isRedTurn && redStones.contains(friendNeighbor))) {
                                            validPlacement = true;
                                            break;
                                        }
                                    }
                                    if (validPlacement) break;
                                }
                            }
                        }

                        if (validPlacement) {
                            saveGameState();
                            if (isRedTurn) {
                                redStones.add(center);
                            } else {
                                blueStones.add(center);
                            }


                            boolean capturedAny ;
                            do {
                                capturedAny = false;
                                Set<Point> playerGroup = findGroup(center, isRedTurn ? redStones : blueStones);


                                Set<Point> adjacentOpponentGroups = new HashSet<>();
                                for (Point stone : playerGroup) {
                                    ArrayList<Point> stoneNeighbors = getNeighborCenters(stone);
                                    for (Point neighbor : stoneNeighbors) {
                                        if ((isRedTurn && blueStones.contains(neighbor)) ||
                                                (!isRedTurn && redStones.contains(neighbor))) {
                                            Set<Point> opponentGroup = findGroup(neighbor, isRedTurn ? blueStones : redStones);
                                            adjacentOpponentGroups.addAll(opponentGroup);
                                        }
                                    }
                                }

                                for (Point opponentStone : adjacentOpponentGroups) {
                                    Set<Point> opponentGroup = findGroup(opponentStone, isRedTurn ? blueStones : redStones);
                                    if (opponentGroup.size() < playerGroup.size()) {

                                        if (isRedTurn) {
                                            blueStones.removeAll(opponentGroup);
                                        } else {
                                            redStones.removeAll(opponentGroup);
                                        }
                                        capturedAny = true;
                                    }
                                }


                                checkWinCondition();
                                if (gameExited) return;


                                if (capturedAny) {
                                    repaint();
                                    JOptionPane.showMessageDialog(null,
                                            "Captured opponent's stones! Place another stone.",
                                            "Capture Move", JOptionPane.INFORMATION_MESSAGE);
                                    return; // Exit the current click handler - player will click again
                                }
                            } while (capturedAny);


                            isRedTurn = !isRedTurn;
                            repaint();
                        } else {
                            JOptionPane.showMessageDialog(null,
                                    "Cannot place stone - must be either:\n" +
                                            "1. Isolated (no neighbors)\n" +
                                            "2. Adjacent to enemy stone\n" +
                                            "3. Adjacent to friendly stone connected to enemy",
                                    "Invalid Move", JOptionPane.ERROR_MESSAGE);

                        }
                        break;
                    }
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point mousePos = new Point(e.getX(), e.getY());
                hoveredCell = null; // Reset hovered cell

                // Check if the mouse is hovering over any hexagon
                for (ArrayList<Point> hexagon : grid) {
                    Point center = getHexCenter(hexagon);
                    double distance = Math.sqrt(Math.pow(center.x - mousePos.x, 2) + Math.pow(center.y - mousePos.y, 2));
                    if (distance <= 20) {
                        hoveredCell = center; // Set hovered cell
                        repaint();
                        break;
                    }
                }
            }
        });
    }
    /**
     * Exits the game immediately and closes the application.
     */
    void exitGame() {
        gameExited = true;
        System.exit(0);
    }
    /**
     * Checks if the game has been exited by the player.
     *
     * @return true if the game was exited, false otherwise
     */
    public boolean isGameExited() {
        return gameExited;
    }

    /**
     * Computes the center point of a hexagon by averaging its vertices.
     *
     * @param hexagon list of Points representing hexagon vertices
     * @return center Point of the hexagon
     */
    Point getHexCenter(ArrayList<Point> hexagon) {
        double sumX = 0;
        double sumY = 0;
        for (Point p : hexagon) {
            sumX += p.x;
            sumY += p.y;
        }
        return new Point(sumX / hexagon.size(), sumY / hexagon.size());
    }

    /**
     * Retrieves centers of adjacent hexagons around a given hex center.
     *
     * @param center target hexagon's center Point
     * @return list of neighbor centers
     */
    ArrayList<Point> getNeighborCenters(Point center) {
        ArrayList<Point> neighbors = new ArrayList<>();
        for (ArrayList<Point> hexagon : grid) {
            Point neighbor = getHexCenter(hexagon);
            double distance = Math.sqrt(Math.pow(center.x - neighbor.x, 2) + Math.pow(center.y - neighbor.y, 2));
            // Using 45 as the approximate center-to-center distance threshold
            if (distance > 0 && distance <= 45) {
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }
    /**
     * Checks whether a cell has a neighboring enemy stone.
     *
     * @param center Point to check neighbors around
     * @param isRed  true if checking for red player's move
     * @return true if an enemy is adjacent, false otherwise
     */

    private boolean hasEnemyConnection(Point center, boolean isRed) {
        ArrayList<Point> neighbors = getNeighborCenters(center);
        for (Point neighbor : neighbors) {
            if (isRed && blueStones.contains(neighbor)) {
                return true;
            } else if (!isRed && redStones.contains(neighbor)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Determines if a group of connected stones is adjacent to any enemy stones.
     *
     * @param center starting Point of the group
     * @param isRed  true if red's group, false if blue's
     * @return true if group touches enemy, false otherwise
     */
    private boolean isGroupConnectedToEnemy(Point center, boolean isRed) {
        Set<Point> group = findGroup(center, isRed ? redStones : blueStones);
        for (Point stone : group) {
            if (hasEnemyConnection(stone, isRed)) {
                return true;
            }
        }
        return false;
    }
    /**
     * Finds all stones connected to a given stone via neighboring positions.
     *
     * @param stone  starting Point
     * @param stones list of same-color stones
     * @return set of connected stones including the starting stone
     */
    Set<Point> findGroup(Point stone, ArrayList<Point> stones) {
        Set<Point> group = new HashSet<>();
        if (!stones.contains(stone)) return group;

        Queue<Point> queue = new LinkedList<>();
        queue.add(stone);
        group.add(stone);

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            for (Point neighbor : getNeighborCenters(current)) {
                if (stones.contains(neighbor) && !group.contains(neighbor)) {
                    group.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return group;
    }
    /**
     * Saves the current board state (both players' stones and turn history) to enable undo.
     */
    void saveGameState() {

        ArrayList<Point> redStonesCopy = new ArrayList<>();
        for (Point p : redStones) {
            redStonesCopy.add(new Point(p.x, p.y));
        }

        ArrayList<Point> blueStonesCopy = new ArrayList<>();
        for (Point p : blueStones) {
            blueStonesCopy.add(new Point(p.x, p.y));
        }


        redStonesHistory.push(redStonesCopy);
        blueStonesHistory.push(blueStonesCopy);
        turnHistory.push(isRedTurn);


        undoButton.setEnabled(true);
    }
    /**
     * Reverts the game to the previous saved state from the undo stack.
     */
    void undoLastMove() {
        if (redStonesHistory.isEmpty() || blueStonesHistory.isEmpty() || turnHistory.isEmpty()) {

            undoButton.setEnabled(false);
            return;
        }


        redStones = redStonesHistory.pop();
        blueStones = blueStonesHistory.pop();
        isRedTurn = turnHistory.pop();


        if (redStonesHistory.isEmpty()) {
            undoButton.setEnabled(false);
        }

        repaint();
    }


    /**
     * Paints the game board, stones, current player turn indicator, and move validation hints.
     *
     * @param g Graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3));

        int circleX = 30;
        int circleY = getHeight() - 50;
        int circleDiameter = 30;

        // Draw vertices of each hexagon for reference.
        for (ArrayList<Point> hexagon : grid) {
            for (Point p : hexagon) {
                int x = (int) Math.round(p.x);
                int y = (int) Math.round(p.y);
                g.fillOval(x - 3, y - 3, 6, 6);
            }
        }


        if (isRedTurn) {
            g.setColor(Color.RED);
            g.fillOval(circleX, circleY, circleDiameter, circleDiameter);
            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, 20));
            g.drawString("Red Player's Turn", circleX + circleDiameter + 10, circleY + 23);
        } else {
            g.setColor(Color.BLUE);
            g.fillOval(circleX, circleY, circleDiameter, circleDiameter);
            g.setColor(Color.BLACK);
            g.setFont(new Font("SansSerif", Font.BOLD, 20));
            g.drawString("Blue Player's Turn", circleX + circleDiameter + 10, circleY + 23);
        }


        for (Point p : redStones) {
            g2.setColor(Color.RED);
            int radius = 15;
            g2.fillOval((int)(p.x - radius), (int)(p.y - radius), 2 * radius, 2 * radius);
        }
        for (Point p : blueStones) {
            g2.setColor(Color.BLUE);
            int radius = 15;
            g2.fillOval((int)(p.x - radius), (int)(p.y - radius), 2 * radius, 2 * radius);
        }

        g2.setColor(Color.BLACK);

        for (ArrayList<Point> hexagon : grid) {
            g.setColor(Color.BLACK);
            int p1xi, p1yi, p2xi, p2yi;
            Point p1 = hexagon.get(0), p2;
            for (int i = 1; i < hexagon.size(); i++) {
                p2 = hexagon.get(i);
                p1xi = (int) Math.round(p1.x);
                p1yi = (int) Math.round(p1.y);
                p2xi = (int) Math.round(p2.x);
                p2yi = (int) Math.round(p2.y);
                g.drawLine(p1xi, p1yi, p2xi, p2yi);
                p1 = p2;
            }
            p2 = hexagon.get(0);
            p1xi = (int) Math.round(p1.x);
            p1yi = (int) Math.round(p1.y);
            p2xi = (int) Math.round(p2.x);
            p2yi = (int) Math.round(p2.y);
            g.drawLine(p1xi, p1yi, p2xi, p2yi);


            Point center = getHexCenter(hexagon);
            if (redStones.contains(center)) {
                g.setColor(Color.RED);
                fillHexagon(g, hexagon);
            } else if (blueStones.contains(center)) {
                g.setColor(Color.BLUE);
                fillHexagon(g, hexagon);
            }
        }
        if (hoveredCell != null) {
            boolean isValidMove = true;


            if (redStones.contains(hoveredCell) || blueStones.contains(hoveredCell)) {
                isValidMove = false;
            } else {

                if (redStones.isEmpty() && blueStones.isEmpty()) {
                    isValidMove = true;
                } else {
                    ArrayList<Point> neighbors = getNeighborCenters(hoveredCell);
                    boolean hasAnyNeighbor = false;
                    boolean hasEnemyNeighbor = false;
                    boolean hasFriendlyNeighbor = false;


                    for (Point neighbor : neighbors) {
                        if (redStones.contains(neighbor) || blueStones.contains(neighbor)) {
                            hasAnyNeighbor = true;
                            if ((isRedTurn && blueStones.contains(neighbor)) ||
                                    (!isRedTurn && redStones.contains(neighbor))) {
                                hasEnemyNeighbor = true;
                            } else {
                                hasFriendlyNeighbor = true;
                            }
                        }
                    }


                    if (!hasAnyNeighbor) {
                        isValidMove = true;
                    }

                    else if (hasEnemyNeighbor) {
                        isValidMove = true;
                    }
                    // Adjacent only to friendly - check if they connect to enemy
                    else if (hasFriendlyNeighbor) {
                        isValidMove = false;
                        for (Point neighbor : neighbors) {
                            if ((isRedTurn && redStones.contains(neighbor)) ||
                                    (!isRedTurn && blueStones.contains(neighbor))) {
                                ArrayList<Point> friendNeighbors = getNeighborCenters(neighbor);
                                for (Point friendNeighbor : friendNeighbors) {
                                    if ((isRedTurn && blueStones.contains(friendNeighbor)) ||
                                            (!isRedTurn && redStones.contains(friendNeighbor))) {
                                        isValidMove = true;
                                        break;
                                    }
                                }
                                if (isValidMove) break;
                            }
                        }
                    }
                }
            }

            // Draw indicator
            g.setFont(new Font("SansSerif", Font.BOLD, 30));
            if (isValidMove) {
                g.setColor(Color.GREEN);
                g.drawString("✔", (int) hoveredCell.x - 10, (int) hoveredCell.y + 10);
            } else {
                g.setColor(Color.RED);
                g.drawString("✘", (int) hoveredCell.x - 10, (int) hoveredCell.y + 10);
            }
        }
    }
    /**
     * Checks for a win condition after each move. Ends the game if one player has no stones left.
     */
    void checkWinCondition() {

        if (redStones.size() + blueStones.size() <= 2) {
            return;
        }


        if (redStones.isEmpty() && !blueStones.isEmpty()) {
            gameExited = true;
            undoButton.setEnabled(false);  // Disable undo button
            JOptionPane.showMessageDialog(this,
                    "Blue player wins! All red stones have been captured.",
                    "Game Over", JOptionPane.INFORMATION_MESSAGE);
        }
        else if (blueStones.isEmpty() && !redStones.isEmpty()) {
            gameExited = true;
            undoButton.setEnabled(false);  // Disable undo button
            JOptionPane.showMessageDialog(this,
                    "Red player wins! All blue stones have been captured.",
                    "Game Over", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    /**
     * Fills a hexagon on the board with the current player's color.
     *
     * @param g       Graphics context
     * @param hexagon list of vertices defining the hexagon
     */
    private void fillHexagon(Graphics g, ArrayList<Point> hexagon) {
        int[] xPoints = new int[hexagon.size()];
        int[] yPoints = new int[hexagon.size()];
        for (int i = 0; i < hexagon.size(); i++) {
            xPoints[i] = (int) Math.round(hexagon.get(i).x);
            yPoints[i] = (int) Math.round(hexagon.get(i).y);
        }
        g.fillPolygon(xPoints, yPoints, hexagon.size());
    }
    /**
     * Main application entry point. Creates a hexagonal grid layout, initializes the JFrame,
     * and starts a text-based quit option listener.
     *
     * @param args expects three arguments: size, originx, originy
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("java HexGrid <size> <originx> <originy>");
            System.exit(1);
        }

        double size = 0.0, originx = 0.0, originy = 0.0;
        try {
            size = Double.parseDouble(args[0]);
            originx = Double.parseDouble(args[1]);
            originy = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            System.err.println("Problems parsing double arguments.");
            System.exit(1);
        }

        Layout flat = new Layout(Layout.flat,
                new Point(size, size),
                new Point(originx, originy));

        int baseN = 6;
        ArrayList<ArrayList<Point>> grid = new ArrayList<>();
        for (int q = -baseN; q <= baseN; q++) {
            for (int r = -baseN; r <= baseN; r++) {
                for (int s = -baseN; s <= baseN; s++) {
                    if ((q + r + s) == 0) {
                        HexCube h = new HexCube(q, r, s);
                        ArrayList<Point> corners = flat.polygonCorners(h);
                        grid.add(corners);
                    }
                }
            }
        }

        JFrame frame = new JFrame("HexGrid");
        HexGrid panel = new HexGrid(grid);
        frame.add(panel);
        frame.setSize(800, 1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        frame.setLocationRelativeTo(null);


        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                // Center the window after it's shown
                frame.setLocationRelativeTo(null);
            }
        });

        frame.setVisible(true);

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Type quit to exit: ");
            String input = scanner.next().toLowerCase();
            if (input.equals("quit")) {
                System.out.println("Exiting Game...");
                System.out.println("GoodBye :)");
                frame.dispose();
                System.exit(0);
            }
        }
    }
}

// using the windows listener, we move the board the according to the new window size
// by the amount of the width increased/2 and height increased by 2