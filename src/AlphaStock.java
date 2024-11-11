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

public class AlphaStock {

    private static String fetchData() {
        String apiUrl = "https://www.alphavantage.co/query?function=TOP_GAINERS_LOSERS&apikey=3U364IUBVJP7UTGW"; // add your API key to the string
        StringBuilder result = new StringBuilder();

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

        } catch (IOException e) {
            System.out.println("Error fetching data: " + e.getMessage());
            e.printStackTrace();
        }

        return result.toString();
    }

    private static Object[][] parseData(String jsonData) {
        JSONParser parser = new JSONParser();
        Object[][] data = new Object[0][3];

        try {
            JSONObject jsonObject = (JSONObject) parser.parse(jsonData);
            JSONArray gainers = (JSONArray) jsonObject.get("top_gainers");
            JSONArray losers = (JSONArray) jsonObject.get("top_losers");

            int totalRows = gainers.size() + losers.size();
            data = new Object[totalRows][3];

            int index = 0;
            for (Object obj : gainers) {
                JSONObject stock = (JSONObject) obj;
                data[index][0] = stock.get("symbol");
                data[index][1] = stock.get("name");
                data[index][2] = stock.get("price");
                index++;
            }

            for (Object obj : losers) {
                JSONObject stock = (JSONObject) obj;
                data[index][0] = stock.get("symbol");
                data[index][1] = stock.get("name");
                data[index][2] = stock.get("price");
                index++;
            }

        } catch (ParseException e) {
            System.out.println("Error parsing data: " + e.getMessage());
            e.printStackTrace();
        }

        return data;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Stock Investment Advisor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton fetchButton = new JButton("Fetch Stocks");
        panel.add(fetchButton);

        String[] columnNames = {"Symbol", "Name", "Price"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        fetchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String data = fetchData();
                if (!data.isEmpty()) {
                    Object[][] parsedData = parseData(data);
                    tableModel.setRowCount(0);
                    for (Object[] row : parsedData) {
                        tableModel.addRow(row);
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "No data found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        frame.add(BorderLayout.NORTH, panel);
        frame.add(BorderLayout.CENTER, scrollPane);

        frame.setVisible(true);
    }
}