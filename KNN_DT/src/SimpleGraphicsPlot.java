package Assignment1D;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;

class SimpleGraphicsPlot extends JPanel {
    private int width = 800;
    private int heigth = 400;
    private int padding = 25;
    private int labelPadding = 25;
    private Color gridColor = new Color(200, 200, 200, 200);
    private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
    private int pointWidth = 6;
    private int numberYDivisions = 20;
    private ArrayList<Integer> k;
    private ArrayList<Double> correctness;
    
    public SimpleGraphicsPlot(Map<Integer, Double> scores) {
    	k = new ArrayList<Integer>();
    	correctness = new ArrayList<Double>();
    	for(Integer key : scores.keySet()){
    		k.add(key);
    		correctness.add(scores.get(key));
    	}
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double xScale = ((double) getWidth() - (2 * padding) - labelPadding) / getMaxXScore(this.k);
        double yScale = ((double) getHeight() - (2 * padding) - labelPadding) / 100;
        
        List<Point> graphPoints = new ArrayList<Point>();
        for (int i = 0; i < k.size(); i++) {
            int x1 = (int) (k.get(i) * xScale + padding + labelPadding);
            int y1 = (int) ((getMaxYScore(this.correctness) - correctness.get(i)) * yScale + padding);
            graphPoints.add(new Point(x1, y1));
        }
        
        // draw white background
        g2.setColor(Color.WHITE);
        g2.fillRect(padding + labelPadding, padding, getWidth() - (2 * padding) - labelPadding, getHeight() - 2 * padding - labelPadding);
        g2.setColor(Color.BLACK);

        // create hatch marks and grid lines for y axis.
        Double y_line0 = getMaxYScore(this.correctness);
        for (int i = 0; i < (numberYDivisions + 1); i++) {
            int x0 = padding + labelPadding;
            int x1 = pointWidth + padding + labelPadding;
            int y0 = getHeight() - ((i * (getHeight() - padding * 2 - labelPadding)) / numberYDivisions + padding + labelPadding);
            int y1 = y0;
            if (k.size() > 0) {
                g2.setColor(gridColor);
                g2.drawLine(padding + labelPadding + 1 + pointWidth, y0, getWidth() - padding, y1);
                g2.setColor(Color.BLACK);
                String yLabel0 = ((int) ((y_line0 * ((i * 1.0) / numberYDivisions)) * 100)) / 100.0 + "";
                FontMetrics metrics = g2.getFontMetrics();
                int labelWidth = metrics.stringWidth(yLabel0);
                g2.drawString(yLabel0, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);
            }
            g2.drawLine(x0, y0, x1, y1);
        }

        // and for x axis
        for (int i = 0; i < k.size(); i++) {
            if (k.size() > 1) {
                int x0 = (int) (i * (getWidth() - padding * 2 - labelPadding) / (getMaxXScore(this.k) - getMinXScore(this.k)) + padding + labelPadding);
                int x1 = x0;
                int y0 = getHeight() - padding - labelPadding;
                int y1 = y0 - pointWidth;
                if ((i % ((int) ((k.size() / 20.0)) + 1)) == 0) {
                    g2.setColor(gridColor);
                    g2.setColor(Color.BLACK);
                    String xLabel = i + "";
                    FontMetrics metrics = g2.getFontMetrics();
                    int labelWidth = metrics.stringWidth(xLabel);
                    g2.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
                }
                g2.drawLine(x0, y0, x1, y1);
            }
        }

        g2.setColor(Color.BLACK);
        // create x and y axes 
        g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, padding + labelPadding, padding);
        g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, getWidth() - padding, getHeight() - padding - labelPadding);

        g2.setColor(Color.BLUE);
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(GRAPH_STROKE);
        for (int i = 0; i < graphPoints.size() - 1; i++) {
            int x1 = graphPoints.get(i).x;
            int y1 = graphPoints.get(i).y;
            int x2 = graphPoints.get(i + 1).x;
            int y2 = graphPoints.get(i + 1).y;
            g.drawLine(x1, y1, x2, y2);
        }

        g2.setStroke(oldStroke);
        g2.setColor(Color.GREEN);
        for (int i = 0; i < graphPoints.size(); i++) {
            int x = graphPoints.get(i).x - pointWidth / 2;
            int y = graphPoints.get(i).y - pointWidth / 2;
            int ovalW = pointWidth;
            int ovalH = pointWidth;
            g2.fillOval(x, y, ovalW, ovalH);
        }
    }

	private Double getMaxYScore(ArrayList<Double> correctness) {
    	Double maxScore = Double.MIN_VALUE;
        for (Double score : correctness) {
            maxScore = Math.max(maxScore, score);
        }
        return maxScore;
	}

	private double getMinXScore(ArrayList<Integer> k) {
        Integer minScore = Integer.MAX_VALUE;
        for (Integer score : k) {
            minScore = Math.min(minScore, score);
        }
        return minScore;
    }

    private double getMaxXScore(ArrayList<Integer> k) {
    	Integer maxScore = Integer.MIN_VALUE;
        for (Integer score : k) {
            maxScore = Math.max(maxScore, score);
        }
        return maxScore;
    }
    
    // execute the graph with some valid input:
    public static void createAndShowGuiDT(Map<Integer,Double> scores,int set_size) {
		SimpleGraphicsPlot mainPanel = new SimpleGraphicsPlot(scores);
		mainPanel.setPreferredSize(new Dimension(800, 600));
		JFrame frame = new JFrame("DT-ID3-Learning-Curve-Using-" + set_size + "%-Training-Set");
        frame.getContentPane().add(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
