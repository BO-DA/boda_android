package com.example.boda;

import static com.example.boda.Config.SOCKET_SERVER_IP;
import static com.example.boda.Config.SOCKET_SERVER_PORT;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketService extends Service {

    private static final String TAG = "SocketService";
    private ClientThread thread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start the network operation in a separate thread
        thread = new ClientThread();
        thread.start();

        return START_STICKY;
    }

    class ClientThread extends Thread {
        @Override
        public void run() {
            Socket socket = null;
            while (true) {
                try {
                    socket = new Socket(SOCKET_SERVER_IP, SOCKET_SERVER_PORT);

                    InputStream input = socket.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(input, StandardCharsets.UTF_8);
                    BufferedReader reader = new BufferedReader(inputStreamReader);

                    String line;
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    String receivedMessage = stringBuilder.toString();
                    System.out.println("Received: " + receivedMessage);

                    sendBroadcast(receivedMessage);

                    Thread.sleep(1000);
                } catch (Exception e) {
                    Log.e(TAG, "Error in socket communication", e);
                } finally {
                    try {
                        if (socket != null) {
                            socket.close();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error closing socket", e);
                    }
                }
            }
        }
    }

    private void sendBroadcast(String message) {
        Intent intent = new Intent("SocketServiceBroadcast");
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Not needed for this example
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up resources when the service is destroyed
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
    }
}
