package com.example.canreaderapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private static final String SERVER_IP = "10.4.163.157"; // Change to your server's IP
    private static final int SERVER_PORT = 5000;
    private TextView jsonTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        jsonTextView = findViewById(R.id.jsonTextView);

        // Start receiving data in the background
        new ReceiveJsonTask().execute();
    }

    private class ReceiveJsonTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            StringBuilder receivedJson = new StringBuilder();

            try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    receivedJson.append(line);
                }

                Log.d("JSON_RECEIVED", "Received: " + receivedJson.toString());

            } catch (Exception e) {
                Log.e("ERROR", "Error receiving JSON", e);
            }
            return receivedJson.toString();
        }

        @Override
        protected void onPostExecute(String json) {
            if (!json.isEmpty()) {
                try {
                    // Parse the received JSON string
                    JSONObject jsonObject = new JSONObject(json);
                    // Display the JSON object
                    jsonTextView.setText(jsonObject.toString(4));  // Pretty print JSON with indentation
                } catch (Exception e) {
                    Log.e("ERROR", "Error parsing JSON", e);
                    jsonTextView.setText("Failed to parse JSON.");
                }
            } else {
                jsonTextView.setText("Failed to receive JSON.");
            }
        }
    }
}





