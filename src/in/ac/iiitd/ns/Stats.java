package in.ac.iiitd.ns;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Taken from slowhttptest C application and ported it to Java Application
 * Used the same Google Chart API
 * @author Suresh Rangaswamy
 *
 */
public class Stats {

    // Global Parameters for statistics;
    private int iter;
    private int pending;
    private int connected;
    private int closed;
    private int isAvailable;

    private static final String HTML_HEADER =
            "<!-- SlowHTTPTest Analysis chart (c) Sergey Shekyan, Victor Agababov 2011  -->\n "
                    +"<html>\n"
                    +"  <head>\n"
                    +" <style>\n"
                    +"    body { font: 12px/18px \"Lucida Grande\", \"Lucida Sans Unicode\", Helvetica, Arial, Verdana, sans-serif; background-color: transparent; color: #333; -webkit-font-smoothing: antialiased; } \n"
                    +"    .slow_results {font-size: 12px; } \n"
                    +"    </style>\n"
                    +"    <script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>\n"
                    +"    <script type=\"text/javascript\">\n"
                    +"      google.load(\"visualization\", \"1\", {packages:[\"corechart\"]});\n"
                    +"      google.setOnLoadCallback(drawChart);\n"
                    +"      function drawChart() {\n"
                    +"        var data = new google.visualization.DataTable();\n"
                    +"        data.addColumn('string', 'Seconds');\n"
                    +"        data.addColumn('number', 'Closed');\n"
                    +"        data.addColumn('number', 'Pending');\n"
                    +"        data.addColumn('number', 'Connected');\n"
                    +"        data.addColumn('number', 'Service available');\n"
                    +"        data.addRows([\n";

    private static final String HTML_FOOTER = 
            "        ]);\n"
                    +"var chart = new google.visualization.AreaChart(document.getElementById('chart_div'));\n"
                    +"            chart.draw(data, {'width': 600, 'height': 360, 'title': 'Test results against %s',"
                    +"          hAxis: {'title': 'Seconds', 'titleTextStyle': {color: '#FF0000'}},\n"
                    +"        vAxis: {'title': 'Connections', 'titleTextStyle': {color: '#FF0000'}, 'viewWindowMode':'maximized'}\n"
                    +"  });\n"
                    +"  }\n"
                    +"  </script>\n"
                    +"<title>SlowHTTPTest(tm) Connection Results</title>\n"
                    +"</head>\n"
                    +"<body>\n"
                    +"      <p>%s</p>\n"
                    +"<div id=\"chart_div\"></div>\n"
                    +"</body>\n"
                    +"</html>\n";

    public Stats(int i, int closed, int pending, int connected,
            int isAvailable) {
        // TODO Auto-generated constructor stub
        this.iter = i;
        this.closed = closed;
        this.pending = pending;
        this.connected = connected;
        this.isAvailable = isAvailable;
    }

    public static void genHTML(){
        try {
            BufferedWriter writer = 
                    new BufferedWriter(new FileWriter(new File("SlowLoris_" 
                            + new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss").format(new Date()) 
                            + ".html")));
            writer.write(HTML_HEADER);
            String s = SlowLoris.stats.toString();
            writer.write(s.substring(1, s.length() - 1));
            StringBuffer table = new StringBuffer();

            table.append("<table class=\"slow_results\" border=\"0\">"
                    + "<tbody>"
                    + "<tr><th>Test parameters</th></tr>"
                    + "<tr><td>Hostname</td><td>" + SlowLoris.CLA.get("-h") + "</td></tr>"
                    + "<tr><td>Port</td><td>" + SlowLoris.CLA.get("-p") + "</td></tr>"
                    + "<tr><td>No. of Connections</td><td>" + SlowLoris.CLA.get("-c") + "</td></tr>"
                    + "<tr><td>Interval Between Follow Up Data</td><td>" + SlowLoris.CLA.get("-i") + "</td></tr>"
                    + "<tr><td>Target Test Duration</td><td>" + SlowLoris.CLA.get("-t") + "</td></tr>"
                    + "<tr><td>Connections per second</td><td>" + SlowLoris.CLA.get("-r") + "</td></tr>"
                    + "<tr><td>Timeout for probe</td><td>" + SlowLoris.CLA.get("-o") + "</td></tr>"
                    + "</tbody></table>");

            writer.write(String.format(HTML_FOOTER, SlowLoris.CLA.get("-h"), table.toString()));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            if(SlowLoris.DEBUG)
                e.printStackTrace();
        }
    }

    @Override
    public String toString(){
        return "['" + iter + "'," + closed + "," + pending + "," + connected + "," + isAvailable + "]";
    }
}
