package com.digivalet.server;

import com.digivalet.config.Thermostat;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class SerialDeviceReader extends Thread{
    String currentPortName;
    private String xmlFilePath;
    Thermostat thermostat;

    public SerialDeviceReader(String xmlFilePath, Thermostat thermostat){
        this.xmlFilePath = xmlFilePath;
        this.thermostat = thermostat;
    }

    @Override
    public void run(){
        DevicePOJO deviceCommands = readXML(xmlFilePath);
        assert deviceCommands != null;
        String powerStatus = deviceCommands.getPower().getStatus();
        String tempStatus = deviceCommands.getTemperature().getStatus();
        String modeStatus = deviceCommands.getMode().getStatus();
        String fanSpeedStatus = deviceCommands.getFanSpeed().getStatus();
        String readCommand = deviceCommands.getModbusRead();
        String[] commands = {powerStatus, tempStatus, modeStatus, fanSpeedStatus};


        while (true){
            int bytesRead=0;
            try {
                for (String command: commands){
                    String hexCommand = readCommand+" "+command;
                    String crc = calculateCRC16(hexCommand);
                    String actualCommand = hexCommand+" "+crc.substring(2)+ " "+crc.substring(0,2);

                    byte[] bytesArr = convertHexStringToByteArray(actualCommand);
                    Server.outputStream.write(bytesArr);
                    Server.outputStream.flush();

                    Thread.sleep(400);
                    byte[] response = new byte[8];
                    bytesRead = Server.inputStream.read(response);
                    if (bytesRead==8){
                        continue;
                    }

                    switch (command) {
                        case "00 02 00 01":
                            if (response[4] == 1) {
                                thermostat.setPower(Thermostat.Power.ON);
                            } else if (response[4]==0){
                                thermostat.setPower(Thermostat.Power.OFF);
                            }
                            System.out.println("\nPower: " + thermostat.getPower());
                            break;
                        case "00 04 00 01":
                            byte[] arr = {(byte) response[3], (byte) response[4]};
                            StringBuilder hexBuilder = new StringBuilder();
                            for (byte b : arr) {
                                int intValue = b & 0xFF;
                                hexBuilder.append(String.format("%02X", intValue));
                            }
                            String hexString = hexBuilder.toString();
                            int decimalValue = Integer.parseInt(hexString, 16);
                            double temp= decimalValue/10.0;
                            thermostat.setTemperature(temp);
                            if (thermostat.getUnit()==null){
                                thermostat.setUnit(Thermostat.Unit.CELSIUS);
                            }

                            if (thermostat.getUnit()== Thermostat.Unit.FAHRENHEIT){
                                System.out.println("Temperature: " + ((thermostat.getTemperature()*(9/5))+32));
                            }else{
                                System.out.println("Temperature: "+ thermostat.getTemperature());
                            }
                            System.out.println("Temperature Unit: " + thermostat.getUnit());
                            break;
                        case "00 03 00 01":
                            if (response[4] == 1) {
                                thermostat.setMode(Thermostat.Mode.COOL);
                            } else if (response[4] == 2) {
                                thermostat.setMode(Thermostat.Mode.HEAT);
                            } else if (response[4]==3){
                                thermostat.setMode(Thermostat.Mode.VENTILATION);
                            }
                            System.out.println("Mode: " + thermostat.getMode());
                            break;
                        case "00 05 00 01":
                            if (response[4] == 0) {
                                thermostat.setFanSpeed(Thermostat.FanSpeed.HIGH);
                            } else if (response[4] == 1) {
                                thermostat.setFanSpeed(Thermostat.FanSpeed.MEDIUM);
                            } else if (response[4] == 2) {
                                thermostat.setFanSpeed(Thermostat.FanSpeed.LOW);
                            } else if (response[4]==3){
                                thermostat.setFanSpeed(Thermostat.FanSpeed.AUTO);
                            }
                            System.out.println("FanSpeed: " + thermostat.getFanSpeed());
                            break;
                    }
                }
            }catch (Exception e){
                while (true) {
                    try {
                        Server.reconnect();
                        break;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.out.println("Failed to connect to " + currentPortName + ". Retrying...");
                    }
                    break;
                }
            }
            try {
                if (bytesRead!=8){
                    Thread.sleep(8000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] convertHexStringToByteArray(String hexString) {
        String[] hexValues = hexString.split(" ");
        byte[] byteArray = new byte[hexValues.length];
        for (int i = 0; i < hexValues.length; i++) {
            byteArray[i] = (byte) Integer.parseInt(hexValues[i], 16);
        }
        return byteArray;
    }
    public DevicePOJO readXML(String xmlFilePath){
        DevicePOJO thermostat = new DevicePOJO();
        try{
            File file = new File(xmlFilePath);
            JAXBContext jaxbContext = JAXBContext.newInstance(DevicePOJO.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            thermostat = (DevicePOJO) unmarshaller.unmarshal(file);
        }catch (JAXBException e){
            e.printStackTrace();
        }
        return thermostat;
    }

    private static String calculateCRC16(String hexCommand) {
        int crc = 0xFFFF;

        String[] hexBytes = hexCommand.split(" ");
        for (String hexByte : hexBytes) {
            int data = Integer.parseInt(hexByte, 16);
            crc ^= data & 0xFF;

            for (int i = 0; i < 8; i++) {
                if ((crc & 0x0001) != 0) {
                    crc >>= 1;
                    crc ^= 0xA001;
                } else {
                    crc >>= 1;
                }
            }
        }
        return String.format("%04X", crc);
    }


}
