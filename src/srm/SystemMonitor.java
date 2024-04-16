package srm;

import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.GradientPaint;

import javax.swing.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.*;
import org.jfree.data.xy.*;
import org.jfree.ui.RectangleEdge;
import org.hyperic.sigar.*;
import javax.swing.border.*;
import java.io.FileWriter;
import java.io.IOException;




public class SystemMonitor extends JFrame {
    private TimeSeries cpuSeries;
    private TimeSeries memorySeries;
    private TimeSeries diskReadSeries;
    private TimeSeries diskWriteSeries;
    private TimeSeries networkInSeries;
    private TimeSeries networkOutSeries;
    private Sigar sigar;
    private JFreeChart cpuChart;
    private JFreeChart memoryChart;
    private JFreeChart diskInChart;
    private JFreeChart diskOutChart;
    private JFreeChart networkInChart;
    private JFreeChart networkOutChart;
    private static final String CSV_FILE_NAME = "system_monitor_data.csv";
    private static final String CSV_HEADERS = "Timestamp,CPU Utilization,Memory Utilization,Disk Read Speed,Disk Write Speed,Network Inbound Throughput,Network Outbound Throughput\n";
    
    private FileWriter csvWriter;
    public SystemMonitor() {
        setTitle("System Monitor");
        setSize(1200, 900); // Adjusted size to accommodate two side-by-side charts
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Create Sigar instance
        sigar = new Sigar();

        // Create time series datasets for each metric
        cpuSeries = new TimeSeries("CPU Utilization");
        memorySeries = new TimeSeries("Memory Utilization");
        diskReadSeries = new TimeSeries("Disk Read Speed");
        diskWriteSeries = new TimeSeries("Disk Write Speed");
        networkInSeries = new TimeSeries("Network Inbound Throughput");
        networkOutSeries = new TimeSeries("Network Outbound Throughput");

        // Create time series collections
        TimeSeriesCollection cpuDataset = new TimeSeriesCollection(cpuSeries);
        TimeSeriesCollection memoryDataset = new TimeSeriesCollection(memorySeries);
        TimeSeriesCollection diskInDataset = new TimeSeriesCollection(diskReadSeries);
        TimeSeriesCollection diskOutDataset = new TimeSeriesCollection(diskWriteSeries);
        TimeSeriesCollection networkInDataset = new TimeSeriesCollection(networkInSeries);
        TimeSeriesCollection networkOutDataset = new TimeSeriesCollection(networkOutSeries);

        // Create charts
        cpuChart = createChart(cpuDataset, "CPU Utilization", "% Utilization", 0, 100, Color.decode("#8BE9FD"));
        memoryChart = createChart(memoryDataset, "Memory Utilization", "% Utilization",  0, 100, Color.decode("#50FA7B"));
        diskInChart = createChart(diskInDataset, "Disk Read", "% Time Spent Reading", 0, 100, Color.decode("#FFB86C")); // Assuming disk speed in bytes/sec
        diskOutChart = createChart(diskOutDataset, "Disk Write", "% Time Spent Writing", 0, 100, Color.decode("#FF79C6")); // Assuming disk speed in bytes/sec
        networkInChart = createChart(networkInDataset, "Network Inbound Throughput", "MBps", 0, 50, Color.decode("#BD93F9"));
        networkOutChart = createChart(networkOutDataset, "Network Outbound Throughput",  "MBps",0, 50, Color.decode("#FF5555"));

        // Create panels for charts
        JPanel cpuMemoryPanel = new JPanel();
        cpuMemoryPanel.setLayout(new BoxLayout(cpuMemoryPanel, BoxLayout.X_AXIS)); 
        cpuMemoryPanel.add(new ChartPanel(cpuChart));
        cpuMemoryPanel.add(new ChartPanel(memoryChart));

        JPanel diskPanel = new JPanel();
        diskPanel.setLayout(new BoxLayout(diskPanel, BoxLayout.X_AXIS));
        diskPanel.add(new ChartPanel(diskInChart));
        diskPanel.add(new ChartPanel(diskOutChart));

        JPanel networkPanel = new JPanel();
        networkPanel.setLayout(new BoxLayout(networkPanel, BoxLayout.X_AXIS));
        networkPanel.add(new ChartPanel(networkInChart));
        networkPanel.add(new ChartPanel(networkOutChart));

        // Add panels to main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(cpuMemoryPanel);
        mainPanel.add(diskPanel);
        mainPanel.add(networkPanel);

        // Inside the SystemMonitor constructor
        Color paddingColor = Color.decode("#282A36"); // Change this to the desired color
        int paddingSize = 30; // Adjust padding size as needed

        // Create the padding border
        Border paddingBorder = BorderFactory.createEmptyBorder(paddingSize, paddingSize, paddingSize, paddingSize);

        // Create the line border with the desired color
        Border lineBorder = BorderFactory.createLineBorder(paddingColor);
        
        Border pad = BorderFactory.createMatteBorder(paddingSize, paddingSize, paddingSize, paddingSize, paddingColor);

        // Combine the padding border and the line border
        Border compoundBorder = BorderFactory.createCompoundBorder(lineBorder, paddingBorder);

        // Set the compound border to the main panel
        mainPanel.setBorder(pad);
        
        // Add main panel to frame
        add(mainPanel);

        // Start updating charts
        startUpdatingCharts();
        try {
            // Create FileWriter object for writing to CSV file
            csvWriter = new FileWriter(CSV_FILE_NAME);

            // Write headers to CSV file
            csvWriter.append(CSV_HEADERS);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Start updating charts and logging data
        startUpdatingCharts();
        startLoggingData();
    }

    private JFreeChart createChart(XYDataset dataset, String title, String y, double minY, double maxY, Color lineColor) {
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                title, // Title
                "", // X-axis Label
                y, // Y-axis Label
                dataset, // Dataset
                false, // Show Legend
                true, // Use tooltips
                false // Generate URLs
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setRenderer(new XYAreaRenderer());
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setRange(minY, maxY); // Set y-axis range

        DateAxis domainAxis = (DateAxis) plot.getDomainAxis();
        domainAxis.setTickLabelsVisible(false); // Show tick labels
        domainAxis.setTickMarksVisible(false); // Show tick marks
        


        XYAreaRenderer renderer = new XYAreaRenderer();
        plot.setRenderer(renderer);

        // Set solid line color
        renderer.setSeriesPaint(0, new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 120)); 

        // Set translucent fill color (adjusted alpha value for transparency)
        renderer.setSeriesFillPaint(0, new Color(lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue(), 200)); 

        // Set stroke width (optional adjustment)
        BasicStroke stroke0 = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        renderer.setSeriesStroke(0, stroke0);

        // Optional: Set outline paint to match line color (for a more cohesive look)
        renderer.setSeriesOutlinePaint(0, lineColor); 
        
//      // Adjust domain to show only the last 60 seconds if data is available
        long now = System.currentTimeMillis();
        long end = now + 6000;
       
 

        plot.setDomainPannable(true);
        plot.setRangePannable(true);


        // Set Dracula color scheme for the chart background, grid lines, axis labels, and legend
        chart.setBackgroundPaint(Color.decode("#282A36")); // page bg
        plot.setBackgroundPaint(Color.decode("#44475A")); // chart bg
        
        
        plot.setRangeGridlinePaint(new Color(108, 113, 196));
        plot.setDomainGridlinePaint(Color.decode("#44475A")); // vertlines
        rangeAxis.setLabelPaint( Color.decode("#F8F8F2"));
        domainAxis.setLabelPaint(Color.decode("#F8F8F2"));
        chart.getTitle().setPaint(lineColor);
        
        rangeAxis.setTickLabelPaint(Color.WHITE);
        // Remove chart border
        chart.setBorderVisible(false);

        return chart;
    }




    private void startUpdatingCharts() {
        Timer timer = new Timer(125, e -> { // Decreased interval to 250ms
            long now = System.currentTimeMillis();
            long oneMinuteAgo = now - 60000;
            
            
           
            try {
                // Update CPU chart
                updateCPUChart(now);

                // Update memory chart
                updateMemoryChart(now);

                // Update disk chart
                updateDiskChart(now);

                // Update network charts
                updateNetworkChart(now);
                
            } catch (SigarException ex) {
                ex.printStackTrace();
            }
        });
        timer.start();
    }


    private void updateCPUChart(long now) throws SigarException {
        long cutoff = now - 60000;
        for (int i = 0; i < cpuSeries.getItemCount(); i++) {
            long timestamp = cpuSeries.getTimePeriod(i).getMiddleMillisecond();
            if (timestamp < cutoff) {
                cpuSeries.delete(i, i);
                i--; // Adjust index after deletion
            }
        }

        double cpuUsage = sigar.getCpuPerc().getCombined() * 100;
        cpuSeries.addOrUpdate(new Millisecond(), cpuUsage);
        
        try (FileWriter writer = new FileWriter(CSV_FILE_NAME, true)) {
        	writer.append(String.valueOf(now)).append("\n");
            writer.append("CPU,%,").append(String.valueOf(cpuUsage)).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateMemoryChart(long now) throws SigarException {
    	long cutoff = now - 60000;
        for (int i = 0; i < memorySeries.getItemCount(); i++) {
            long timestamp = memorySeries.getTimePeriod(i).getMiddleMillisecond();
            if (timestamp < cutoff) {
                memorySeries.delete(i, i);
                i--; // Adjust index after deletion
            }
        }
        double usedMemory = sigar.getMem().getUsedPercent();
        memorySeries.addOrUpdate(new Millisecond(), usedMemory);
        try (FileWriter writer = new FileWriter(CSV_FILE_NAME, true)) {
            writer.append("Memory,%,").append(String.valueOf(usedMemory)).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long lastUpdateTime = 0;
    private long lastReadBytes = 0;
    private long lastWriteBytes = 0;

    private void updateDiskChart(long now) throws SigarException {
        long cutoff = now - 60000;
        for (int i = 0; i < diskReadSeries.getItemCount(); i++) {
            long timestamp = diskReadSeries.getTimePeriod(i).getMiddleMillisecond();
            if (timestamp < cutoff) {
                diskReadSeries.delete(i, i);
                diskWriteSeries.delete(i, i);
                i--; // Adjust index after deletion
            }
        }
        FileSystemUsage fsUsage = sigar.getFileSystemUsage("C:");

        // Calculate the elapsed time since the last update
        long elapsedTime = now - lastUpdateTime;

        // Calculate the number of bytes read and written since the last update
        long bytesRead = fsUsage.getDiskReadBytes() - lastReadBytes;
        long bytesWritten = fsUsage.getDiskWriteBytes() - lastWriteBytes;

        // Estimate the time spent on disk reads and writes based on the read and write speeds
        double readTime = (double) bytesRead / fsUsage.getDiskReadBytes() * elapsedTime;
        double writeTime = (double) bytesWritten / fsUsage.getDiskWriteBytes() * elapsedTime;

        // Calculate the percentage of time spent on disk reads and writes
        double readTimePercent = readTime / elapsedTime * 10000;
        double writeTimePercent = writeTime / elapsedTime * 10000;

        
        // Update the upper bounds of the y-axis
        double maxDiskUsage = Math.max(readTimePercent, 0);
        
        // Retrieve the plot from the inchart
        XYPlot plot = (XYPlot) diskInChart.getPlot();
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setUpperBound(100);
        rangeAxis.setLowerBound(0);// Set dynamic upper bound

        // Add the data points to the respective series
        diskReadSeries.add(new Millisecond(), readTimePercent);
        diskWriteSeries.add(new Millisecond(), writeTimePercent);

        // Update last update and last read/write bytes for the next iteration
        lastUpdateTime = now;
        lastReadBytes = fsUsage.getDiskReadBytes();
        lastWriteBytes = fsUsage.getDiskWriteBytes();
        try (FileWriter writer = new FileWriter(CSV_FILE_NAME, true)) {
            writer.append("Disk Read,%,").append(String.valueOf(readTimePercent)).append("\n");
            writer.append("Disk Write,%,").append(String.valueOf(writeTimePercent)).append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateNetworkChart(long now) throws SigarException {
        long cutoff = now - 60000;
        for (int i = 0; i < networkInSeries.getItemCount(); i++) {
            long timestamp = networkInSeries.getTimePeriod(i).getMiddleMillisecond();
            if (timestamp < cutoff) {
                networkInSeries.delete(i, i);
                networkOutSeries.delete(i, i);
                i--; // Adjust index after deletion
            }
        }
        NetInterfaceStat ifStat = sigar.getNetInterfaceStat("eth27");
        if (ifStat != null) {
            long rxBytes = ifStat.getRxBytes();
            long txBytes = ifStat.getTxBytes();
            if (rxBytes > 0 || txBytes > 0) {
                double rxKbps = ((rxBytes) / 1048576) - 1130;
                double txKbps = ((txBytes) / 1048576) - 59;

                // Update the upper bounds of the y-axis
                double maxNetworkinUsage = Math.max(rxKbps, 0);
                double maxNetworkoutUsage = Math.max(txKbps, 0);
                
                // Retrieve the plot from the chart
                XYPlot plotin = (XYPlot) networkInChart.getPlot();
                NumberAxis rangeAxisi = (NumberAxis) plotin.getRangeAxis();
                rangeAxisi.setUpperBound(maxNetworkinUsage+(maxNetworkinUsage*.25)); // Set dynamic upper bound
                rangeAxisi.setLowerBound(0);
                
                XYPlot plotout = (XYPlot) networkOutChart.getPlot();
                NumberAxis rangeAxiso = (NumberAxis) plotout.getRangeAxis();
                rangeAxiso.setUpperBound(maxNetworkoutUsage+(maxNetworkoutUsage*.25));
                rangeAxiso.setLowerBound(0);

                // Add the data points to the respective series
                networkInSeries.add(new Millisecond(), rxKbps);
                networkOutSeries.add(new Millisecond(), txKbps);
                try (FileWriter writer = new FileWriter(CSV_FILE_NAME, true)) {
                    writer.append("Network In,Kbps,").append(String.valueOf(rxKbps)).append("\n");
                    writer.append("Network Out,Kbps,").append(String.valueOf(txKbps)).append("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SystemMonitor().setVisible(true);
        });
    }
    
    private void startLoggingData() {
        Timer timer = new Timer(500, e -> {
            try {
                // Log data to CSV file
                logData();
            } catch (IOException | SigarException ex) {
                ex.printStackTrace();
            }
        });
        timer.start();
    }

    private void logData() throws IOException, SigarException {
        long timestamp = System.currentTimeMillis();
        double cpuUsage = sigar.getCpuPerc().getCombined() * 100;
        double memoryUsage = sigar.getMem().getUsedPercent();
        // Add more metrics as needed

        // Append data to CSV file
        csvWriter.append(String.format("%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f\n",
                timestamp, cpuUsage, memoryUsage,
                diskReadSeries.getValue(new Millisecond()).doubleValue(),
                diskWriteSeries.getValue(new Millisecond()).doubleValue(),
                networkInSeries.getValue(new Millisecond()).doubleValue(),
                networkOutSeries.getValue(new Millisecond()).doubleValue()));

        // Flush the buffer to ensure data is written immediately
        csvWriter.flush();
    }

    @Override
    public void dispose() {
        super.dispose();
        try {
            // Close the FileWriter when the JFrame is closed
            if (csvWriter != null) {
                csvWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
