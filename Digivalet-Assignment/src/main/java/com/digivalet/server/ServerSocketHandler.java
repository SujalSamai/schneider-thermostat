package com.digivalet.server;

import com.digivalet.config.Thermostat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.*;
import java.net.Socket;

public class ServerSocketHandler extends Thread{
    Thermostat thermostat;
    Socket clientSocket;
    SerialPort serialPort;
    OutputStream outputStream;

    public ServerSocketHandler(Thermostat thermostat, Socket clientSocket) {
        this.thermostat = thermostat;
        this.serialPort = Server.serialPort;
        this.clientSocket = clientSocket;
        this.outputStream = Server.outputStream;
    }

    @Override
    public void run(){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Server.jsonStr = objectMapper.writeValueAsString(thermostat);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


        System.out.println("Client Connected");
        sendPojoToClient(clientSocket, Server.jsonStr);

        while (true){
            try{
                ObjectMapper mapper = new ObjectMapper();
                ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
                String receivedStr = (String) objectInputStream.readObject();
                JsonNode jsonNodePrev = mapper.readTree(Server.jsonStr);
                JsonNode jsonNodeNew = mapper.readTree(receivedStr);

                String[] command = new String[2];
                if (!jsonNodeNew.get("power").equals(jsonNodePrev.get("power"))){
                    command[0]= "Power";
                    command[1] = String.valueOf(jsonNodeNew.get("power"));
                }else if (!jsonNodeNew.get("temperature").equals(jsonNodePrev.get("temperature"))){
                    command[0]= "Temperature";
                    command[1] = String.valueOf(jsonNodeNew.get("temperature"));
                }else if (!jsonNodeNew.get("mode").equals(jsonNodePrev.get("mode"))){
                    command[0]= "Mode";
                    command[1] = String.valueOf(jsonNodeNew.get("mode"));
                }else if (!jsonNodeNew.get("fanSpeed").equals(jsonNodePrev.get("fanSpeed"))){
                    command[0]= "FanSpeed";
                    command[1] = String.valueOf(jsonNodeNew.get("fanSpeed"));
                }
                else if (!jsonNodeNew.get("unit").equals(jsonNodePrev.get("unit"))){
                    command[0] = "Unit";
                    command[1] = String.valueOf(jsonNodeNew.get("unit"));
                }

                Server.jsonStr = receivedStr;


                if (command[1]!=null){
                    if (Server.isConnectionClosed){
                        SerialDeviceWriter.writeCommands(command, thermostat);
                        Server.isConnectionClosed=false;
                    }else{
                        SerialDeviceWriter.writeCommands(command, thermostat);
                    }
                }
            }catch (IOException | ClassNotFoundException e){
                e.printStackTrace();
                try {
                    Server.reconnect();
                } catch (IOException | UnsupportedCommOperationException | NoSuchPortException | PortInUseException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }


    public static void sendPojoToClient(Socket clientSocket, String jsonStr){
        try{
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            objectOutputStream.writeObject(jsonStr);
            objectOutputStream.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
