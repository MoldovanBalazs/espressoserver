package com.espresso.moldovanbalazs.androidserver;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    private ServerSocket serverSocket;
    Handler uiHandler;
    Thread serverThread = null;
    TextView textView;
    private static final int SERVERPORT = 8000;
    private static final String SERVERIP = "0.0.0.0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.text2);
        textView.setText("Initial text");
        uiHandler = new Handler();

        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        try{
            serverSocket.close();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    class ServerThread implements Runnable {

        private BufferedReader input;

        @Override
        public void run() {
            Socket socket = null;
            String read = null;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    socket = serverSocket.accept();

                    /*OutputStreamWriter osw;
                    String str = "serverSocket";
                    osw = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
                    osw.write(str, 0, str.length());
                    socket.close();*/

                    //new Thread(new CommunicationThread(socket)).start();

                    this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    read = input.readLine();
                    uiHandler.post(new UpdateUIThread(read));
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    class CommunicationThread implements Runnable {

        private Socket clientSocket;
        private BufferedReader input;
        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;

            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()) {
                String read = null;
                try {
                    read = input.readLine();
                    uiHandler.post(new UpdateUIThread(read));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    class UpdateUIThread implements Runnable {

        private String message;

        public UpdateUIThread(String msg) {
            this.message = msg;
        }

        @Override
        public void run() {
            textView.setText(textView.getText().toString() + "Client says:" + message + "\n");
            //textView.setText("this is a test");
        }
    }

}
