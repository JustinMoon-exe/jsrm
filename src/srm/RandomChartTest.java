package srm;
import java.awt.BasicStroke;
import java.awt.Color;
import java.util.Random;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.data.xy.XYDataset; // Import added
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class RandomChartTest {

  public static void main(String[] args) {
    // Generate random data (adjust number of data points as needed)
    int numPoints = 20;
    XYSeries series = new XYSeries("Series 1");
    Random random = new Random();
    for (int i = 0; i < numPoints; i++) {
      double x = i;
      double y = random.nextDouble() * 10; // Values between 0 and 10
      series.add(x, y);
    }
    XYDataset dataset = new XYSeriesCollection(series);

    // Chart configuration
    String title = "Random Data Chart";
    String yAxisLabel = "Y-Axis";
    double minY = 0;
    double maxY = 10;
    Color lineColor = Color.BLUE;
    int alpha = 100; // Adjust for desired transparency (0-255)

    // Create chart
    JFreeChart chart = createChart(dataset, title, yAxisLabel, minY, maxY, lineColor, alpha);

    // Display the chart (assuming you have a library like Swing to display it)
    // You can replace this with your preferred way of displaying the chart
    // (e.g., saving to an image file)
    chart.fireChartChanged();
  }

  private static JFreeChart createChart(XYDataset dataset, String title, String y, double minY, double maxY, Color lineColor, int alpha) {
    JFreeChart chart = ChartFactory.createTimeSeriesChart(
            title,
            "",
            y,
            dataset,
            false,
            true,
            false
    );

    XYPlot plot = (XYPlot) chart.getPlot();
    plot.setRenderer(new XYAreaRenderer());

    XYAreaRenderer renderer = (XYAreaRenderer) plot.getRenderer();

    // Set solid line color
    renderer.setSeriesPaint(0, lineColor);

    // Set translucent fill color (adjusted alpha value for transparency)
    renderer.setSeriesFillPaint(0, new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), alpha));

    // Set stroke width (optional adjustment)
    BasicStroke stroke0 = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    renderer.setSeriesStroke(0, stroke0);

    // Optional: Set outline paint to match line color (for a more cohesive look)
    renderer.setSeriesOutlinePaint(0, lineColor);

    plot.setDomainPannable(true);
    plot.setRangePannable(true);

    // Remove chart border
    chart.setBorderVisible(false);

    return chart;
  }
}
