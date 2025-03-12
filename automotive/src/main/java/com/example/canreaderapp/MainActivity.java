package com.example.canreaderapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private static final String SERVER_IP = "10.4.163.157"; // Replace with your server's IP address
    private static final int SERVER_PORT = 5000;

    private TextView jsonTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        jsonTextView = findViewById(R.id.jsonTextView);

        // Start the background task to connect to the server and fetch the JSON
        new JsonReceiverTask().execute();
    }

    private class JsonReceiverTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // Connect to the server via TCP
                Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                StringBuilder jsonResponse = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    jsonResponse.append(line);
                }

                // Close the socket and the reader
                socket.close();
                reader.close();

                return jsonResponse.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String jsonResponse) {
            if (jsonResponse != null) {
                // Parse the JSON response and display it
                Gson gson = new Gson();
                try {
                    // Assume the JSON is an object, modify this as needed
                    MyJsonData data = gson.fromJson(jsonResponse, MyJsonData.class);

                    // Update the UI with the parsed data
                    jsonTextView.setText(data.toString());
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Failed to connect or receive data", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Define a simple class to match the structure of your JSON data
    public class MyJsonData {
        private String can_messages;

        // Getters and setters
        public String getCan_messages() {
            return can_messages;
        }

        public void setCan_messages(String can_messages) {
            this.can_messages = can_messages;
        }

        @Override
        public String toString() {
            return "Received JSON: " + can_messages;
        }
    }
}
