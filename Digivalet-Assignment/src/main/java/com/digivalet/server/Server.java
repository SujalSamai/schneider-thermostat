package com.digivalet.server;

import com.digivalet.config.Thermostat;
import gnu.io.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;

public class Server implements Runnable{
    private static final String XML_FILE_PATH = "/home/sujalsamai/schneider-thermostat/config.xml";
    Thermostat thermostat;
    static String jsonStr;
    static SerialPort serialPort;

    static OutputStream outputStream;
    static InputStream inputStream;
    private static final String PORTNAME1 ="/dev/ttyUSB0";
    private static final String PORTNAME2 = "/dev/ttyUSB1";
    public static boolean isConnectionClosed=false;

    public void run(){
        thermostat = new Thermostat();
        while (serialPort==null){
            connect();
        }
        SerialDeviceReader deviceReader = new SerialDeviceReader(XML_FILE_PATH, thermostat);
        deviceReader.start();

        try(ServerSocket serverSocket = new ServerSocket(5000)){
            Thread.sleep(3000);

            while (true){
                ServerSocketHandler serverSocketHandler = new ServerSocketHandler(thermostat, serverSocket.accept());
                serverSocketHandler.start();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void connect() {
        final String PORTNAME1 = "/dev/ttyUSB0";
        final String PORTNAME2 = "/dev/ttyUSB1";
        try {
            File port1 = new File(PORTNAME1);
            File port2 = new File(PORTNAME2);
            if (port1.exists()){
                CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(PORTNAME1);
                if (portIdentifier!=null){
                    serialPort = (SerialPort) portIdentifier.open("com.digivalet.server.Server", 2000);
                    serialPort.setSerialPortParams(4800, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_ODD);
                    outputStream = serialPort.getOutputStream();
                    inputStream = serialPort.getInputStream();
                }
            }else if (port2.exists()){
                CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(PORTNAME2);
                if (portIdentifier!=null){
                    serialPort = (SerialPort) portIdentifier.open("com.digivalet.server.Server", 2000);
                    serialPort.setSerialPortParams(4800, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_ODD);
                    outputStream = serialPort.getOutputStream();
                    inputStream = serialPort.getInputStream();
                }
            }
        } catch (NoSuchPortException | UnsupportedCommOperationException | PortInUseException e) {
            try {
                System.out.println("No Serial Connection detected. Retrying after 3 seconds..");
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reconnect() throws UnsupportedCommOperationException, NoSuchPortException, PortInUseException, IOException {
        destroyConnection();
        String lockFilePath0 = "/var/lock/LCK..ttyUSB0";
        String lockFilePath1 = "/var/lock/LCK..ttyUSB1";
        File lockFile0 = new File(lockFilePath0);
        File lockFile1 = new File(lockFilePath1);

        if (lockFile0.exists()){
            lockFile0.delete();
        }
        if (lockFile1.exists()){
            lockFile1.delete();
        }

        File port1 = new File(PORTNAME1);
        File port2 = new File(PORTNAME2);
        if (port1.exists()){
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(PORTNAME1);
            if (portIdentifier!=null){
                serialPort = (SerialPort) portIdentifier.open("com.digivalet.server.Server", 2000);
                serialPort.setSerialPortParams(4800, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_ODD);
                outputStream = serialPort.getOutputStream();
                inputStream = serialPort.getInputStream();
                Server.isConnectionClosed=true;
            }
        }else if (port2.exists()){
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(PORTNAME2);
            if (portIdentifier!=null){
                serialPort = (SerialPort) portIdentifier.open("com.digivalet.server.Server", 2000);
                serialPort.setSerialPortParams(4800, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_ODD);
                outputStream = serialPort.getOutputStream();
                inputStream = serialPort.getInputStream();
                Server.isConnectionClosed=true;
            }
        }

    }

    private static void destroyConnection(){
        try {
            if(outputStream!=null) {
                outputStream.close();
            }
            if(inputStream!=null) {
                inputStream.close();
            }
            if(serialPort!=null) {
                serialPort.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}


