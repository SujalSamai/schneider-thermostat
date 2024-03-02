package com.digivalet.client;

import com.digivalet.config.Thermostat;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Client implements Runnable{
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;
    Thermostat thermostat;

    public void run(){
        try{
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            OutputStream outputStream = socket.getOutputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            String jsonStr = (String) objectInputStream.readObject();
            ObjectMapper mapper = new ObjectMapper();
            thermostat = mapper.readValue(jsonStr, Thermostat.class);

            Scanner sc = new Scanner(System.in);

            while (true){
                String jsonCommand = getUserInput(sc, thermostat, socket);
                outputStream.write(jsonCommand.getBytes());
                outputStream.flush();
            }
        }catch (IOException | ClassNotFoundException e) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            run();
        }
    }

    private static String getUserInput(Scanner sc, Thermostat thermostat, Socket socket){
        while (true){
            System.out.println("\n1. Power");
            System.out.println("2. Temperature");
            System.out.println("3. Mode");
            System.out.println("4. FanSpeed");
            System.out.println("5. Switch Temperature Unit");

            System.out.print("Enter your choice: ");
            int n = 0;
            try{
                n = sc.nextInt();
            }catch (InputMismatchException e){
                System.out.println("Invalid Input. Please enter an integer..");
                sc.next();
                continue;
            }

            switch (n){
                case(1):
                    String curr = thermostat.getPower().toString();
                    if (curr.equals("OFF")) {
                        thermostat.setPower(Thermostat.Power.ON);
                    } else if (curr.equals("ON")){
                        thermostat.setPower(Thermostat.Power.OFF);
                    }else{
                        System.out.println("Not a valid option..");
                    }
                    break;
                case(2):
                    System.out.print("Enter temperature: ");
                    double temp=0;
                    try{
                        temp = sc.nextDouble();
                        if (thermostat.getUnit()== Thermostat.Unit.CELSIUS){
                            if (temp<18 || temp>28){
                                throw new IllegalArgumentException("Invalid Range: Should be between 18°-28°C");
                            }
                        }else if (thermostat.getUnit()==Thermostat.Unit.FAHRENHEIT) {
                            if ((temp<65 || temp>83)){
                                throw new IllegalArgumentException("Invalid Range.. Should be between 65°-83°");
                            }else{
                                temp = (temp - 32.0) * (5.0/9.0);
                            }
                        }
                        thermostat.setTemperature(temp);
                    }catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                    break;
                case(3):
                    System.out.println("1. HEAT");
                    System.out.println("2. COOL");
                    System.out.println("3. VENTILATION");
                    System.out.print("Enter the mode number: ");
                    int mode = sc.nextInt();

                    if (mode==1){
                        thermostat.setMode(Thermostat.Mode.HEAT);
                    }else if (mode==3){
                        thermostat.setMode(Thermostat.Mode.VENTILATION);
                    }else if (mode==2){
                        thermostat.setMode(Thermostat.Mode.COOL);
                    }else{
                        System.out.println("Not a valid option..");
                    }
                    break;
                case(4):
                    System.out.println("1. LOW");
                    System.out.println("2. MEDIUM");
                    System.out.println("3. HIGH");
                    System.out.println("4. AUTO");
                    System.out.print("Enter the FanSpeed number: ");
                    int fanspeed = sc.nextInt();

                    if (fanspeed==1){
                        thermostat.setFanSpeed(Thermostat.FanSpeed.LOW);
                    }else if (fanspeed==2){
                        thermostat.setFanSpeed(Thermostat.FanSpeed.MEDIUM);
                    }else if (fanspeed==3){
                        thermostat.setFanSpeed(Thermostat.FanSpeed.HIGH);
                    }else if (fanspeed==4){
                        thermostat.setFanSpeed(Thermostat.FanSpeed.AUTO);
                    }else{
                        System.out.println("Not a valid option..");
                    }
                    break;
                case(5):
                    System.out.println("1. CELSIUS");
                    System.out.println("2. FAHRENHEIT");
                    int tempUnit = sc.nextInt();

                    if (tempUnit==1){
                        thermostat.setUnit(Thermostat.Unit.CELSIUS);
                    }else if (tempUnit==2){
                        thermostat.setUnit(Thermostat.Unit.FAHRENHEIT);
                    }else{
                        System.out.println("Not a valid option..");
                    }
                    break;
                default:
                    System.out.println("Enter a valid option. Retry..");
            }
            try{
                ObjectMapper mapper = new ObjectMapper();
                String jsonStr = mapper.writeValueAsString(thermostat);

                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(jsonStr);
                objectOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
