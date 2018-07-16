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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MultiGraphicsPlot extends JPanel {
    private int width = 800;
    private int heigth = 400;
    private int padding = 25;
    private int labelPadding = 25;
    private Color gridColor = new Color(200, 200, 200, 200);
    private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
    private int pointWidth = 12;
    private int numberYDivisions = 20;
    private ArrayList<Integer> train_size;
    private ArrayList<Integer> k;
    private ArrayList<Double> correctness;
    private ArrayList<Integer> train_size_other;
    private ArrayList<Integer> k_other;
    private ArrayList<Double> correctness_other;
    
    
    public MultiGraphicsPlot(Map<Integer,HashMap<Integer,Double>> scores,Map<Integer,HashMap<Integer,Double>> scores_other) {
    	k = new ArrayList<Integer>();
    	correctness = new ArrayList<Double>();
    	for(Integer key : scores.keySet()){
    		for(Integer inner : scores.get(key).keySet()) {
    			k.add(inner);
        		correctness.add(scores.get(key).get(inner).doubleValue());
    		}
    	}
    	k_other = new ArrayList<Integer>();
    	correctness_other = new ArrayList<Double>();
    	for(Integer key : scores_other.keySet()){
    		for(Integer inner : scores_other.get(key).keySet()) {
    			k_other.add(inner);
        		correctness_other.add(scores_other.get(key).get(inner).doubleValue());
    		}
    	}
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double xScale = ((double) getWidth() - (2 * padding) - labelPadding) / Math.max(getMaxXScore(this.k),getMaxXScore(this.k_other));
        double yScale = ((double) getHeight() - (2 * padding) - labelPadding) / 100;
        
        List<Point> graphPoints = new ArrayList<Point>();
        List<Point> graphPoints_other = new ArrayList<Point>();
        for (int i = 0; i < k.size(); i++) {
            int x1 = (int) (k.get(i) * xScale + padding + labelPadding);
            int y1 = (int) ((getMaxYScore(this.correctness) - correctness.get(i)) * yScale + padding);
            graphPoints.add(new Point(x1, y1));
        }

        for (int i = 0; i < k_other.size(); i++) {
            int x1 = (int) (k_other.get(i) * xScale + padding + labelPadding);
            int y1 = (int) ((getMaxYScore(this.correctness_other) - correctness_other.get(i)) * yScale + padding);
            graphPoints_other.add(new Point(x1, y1));
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
            if (k.size() > 0 && k_other.size() > 0) {
                g2.setColor(gridColor);
                g2.drawLine(padding + labelPadding + 1 + pointWidth, y0, getWidth() - padding, y1);
                g2.setColor(Color.BLACK);
                String yLabel0 = ((int) ((y_line0 * ((i * 1.0) / numberYDivisions)) * 100)) / 100.0 + "";
                //System.out.println(yLabel0);
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
                    //g2.setColor(gridColor);
                    g2.setColor(Color.BLACK);
                    String xLabel = i + "";
                    FontMetrics metrics = g2.getFontMetrics();
                    int labelWidth = metrics.stringWidth(xLabel);
                    g2.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
                }
                g2.drawLine(x0, y0, x1, y1);
            }
        }
        
        for (int i = 0; i < k_other.size(); i++) {
            if (k_other.size() > 1) {
                int x0 = (int) (i * (getWidth() - padding * 2 - labelPadding) / (getMaxXScore(this.k_other) - getMinXScore(this.k_other)) + padding + labelPadding);
                int x1 = x0;
                int y0 = getHeight() - padding - labelPadding;
                int y1 = y0 - pointWidth;
                if ((i % ((int) ((k_other.size() / 20.0)) + 1)) == 0) {
                    //g2.setColor(gridColor);
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

        for (int i = 0; i < graphPoints_other.size() - 1; i++) {
            int x1 = graphPoints_other.get(i).x;
            int y1 = graphPoints_other.get(i).y;
            int x2 = graphPoints_other.get(i + 1).x;
            int y2 = graphPoints_other.get(i + 1).y;
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
        g2.setColor(Color.RED);        
        for (int i = 0; i < graphPoints_other.size(); i++) {
            int x = graphPoints_other.get(i).x - pointWidth / 2;
            int y = graphPoints_other.get(i).y - pointWidth / 2;
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
    public static void createAndShowGuiKNN(Map<Integer,HashMap<Integer,Double>> scores,Map<Integer,HashMap<Integer,Double>> scores_other,int set_size) {
		MultiGraphicsPlot mainPanel = new MultiGraphicsPlot(scores,scores_other);
		mainPanel.setPreferredSize(new Dimension(800, 600));
		JFrame frame = new JFrame("KNN-Learning-Curve-Using-" + set_size + "%-Training-Set");
        frame.getContentPane().add(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}