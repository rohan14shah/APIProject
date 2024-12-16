import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class AlphaStock extends JFrame implements ActionListener {

    private JTextArea resultArea;
    private JButton fetchButton;
    private JPanel inputPanel;
    private JScrollPane scrollPane;
    private JFrame mainFrame;
    private DefaultTableModel tableModel;

    public AlphaStock() {
        //this is all basic stuff creating the gui
        mainFrame = new JFrame();

        inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(1, 2));

        fetchButton = new JButton("Fetch Data");
        fetchButton.addActionListener(this); // adding the action listener to the fetch button, so when that button is clicked it can print the data

        inputPanel.add(new JLabel());
        inputPanel.add(fetchButton);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        scrollPane = new JScrollPane(resultArea);

        String[] columnNames = {"Symbol", "Price ($)"};
        tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(table);

        mainFrame.setTitle("Stock Investment Advisor");
        mainFrame.setSize(600, 400);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());

        mainFrame.add(inputPanel, BorderLayout.NORTH);
        mainFrame.add(tableScrollPane, BorderLayout.CENTER);
        mainFrame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String apiKey = "F8QI5TEKRS1MOLKT";
        resultArea.setText("");

        String apiUrl = "https://www.alphavantage.co/query?function=TOP_GAINERS_LOSERS&apikey=" + apiKey;
        StringBuilder result = new StringBuilder();
        // this part i don't completely understand, but it is fetching the data from the api -- shown by the code from Hales
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            if (conn.getResponseCode() != 200) {
                throw new IOException("Failed to fetch data, response code: " + conn.getResponseCode());
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
            // converted the JSON data into a 2D array of objects as its easier to disply in a table like the symbols/prices
            Object[][] parsedData = parseData(result.toString());
            //use this link to learn the setrowcount function: https://stackoverflow.com/questions/19628266/how-to-set-rowcount-jtable-setrowcount-gui-by-jtextfield-and-jbutton
            tableModel.setRowCount(0);
            //loop through the data and add it to the table
            for (Object[] row : parsedData) {
                tableModel.addRow(row);
            }

        } catch (IOException ex) {
            resultArea.setText("Error: " + ex.getMessage());
        }
    }
    //essentially this can be called using the class AlphaStock wihtout me needing to create an instance -- reason using static instead of void is because im returning data :)))
    public static Object[][] parseData(String jsonData) {
        //creating the parser + the data array
        JSONParser parser = new JSONParser();
        Object[][] data = new Object[0][1];

        try {
            //parsing the json data
            JSONObject jsonObject = (JSONObject) parser.parse(jsonData);
            //only trying to get the top gainers and losers
            JSONArray gainers = (JSONArray) jsonObject.get("top_gainers");
            JSONArray losers = (JSONArray) jsonObject.get("top_losers");
            //find total rows needed for the data array
            int totalRows = gainers.size() + losers.size();
            data = new Object[totalRows][2];

            int index = 0;
            //loop through the gainers/separting ticker and price and adding to data array
            for (Object obj : gainers) {
                JSONObject stock = (JSONObject) obj;
                data[index][0] = stock.get("ticker");
                data[index][1] = stock.get("price");
                index++;
            }
            //same thing for losers 
            for (Object obj : losers) {
                JSONObject stock = (JSONObject) obj;
                data[index][0] = stock.get("ticker");
                data[index][1] = stock.get("price");
                index++;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return data;
    }

    public static void main(String[] args) {
        AlphaStock frame = new AlphaStock();
    }
}
