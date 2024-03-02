package com.digivalet.server;

import com.digivalet.config.Thermostat;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class SerialDeviceWriter {
    private static final String XML_FILE_PATH = "/home/sujalsamai/schneider-thermostat/config.xml";
    public static void writeCommands(String[] commandArr, Thermostat thermostat) throws IOException {
        System.out.println("\n\nWrite Commands: "+ Arrays.toString(commandArr));
        DevicePOJO deviceCommands = readXML(XML_FILE_PATH);
        String actualCommand=null;
        String modbusWrite = deviceCommands.getModbusWrite();
        OutputStream outputStream = Server.outputStream;

        switch (commandArr[0]) {
            case "Power":
                if (commandArr[1].equals("\"ON\"")) {
                    String command = modbusWrite + " " + deviceCommands.getPower().getOn();  //from config file 01 06
                    String crc = calculateCRC16(command);
                    actualCommand = command + " " + crc.substring(2) + " " + crc.substring(0, 2);
                } else if (commandArr[1].equals("\"OFF\"")) {
                    String command = modbusWrite + " " + deviceCommands.getPower().getOff();
                    String crc = calculateCRC16(command);
                    actualCommand = command + " " + crc.substring(2) + " " + crc.substring(0, 2);
                }
                break;
            case "Temperature": {
                int temp = (int) (Double.parseDouble(commandArr[1]) * 10);
                StringBuilder hex = new StringBuilder(decimalToHex(temp));
                while (hex.length() < 4) {
                    hex.insert(0, "0");
                }
                String command = modbusWrite + " " + deviceCommands.getTemperature().getSet() + " " + hex.substring(0, 2) + " " + hex.substring(2);
                String crc = calculateCRC16(command);
                actualCommand = command + " " + crc.substring(2) + " " + crc.substring(0, 2);
                break;
            }
            case "Mode": {
                String command;
                String crc;
                switch (commandArr[1]) {
                    case "\"COOL\"":
                        command = modbusWrite + " " + deviceCommands.getMode().getCool();
                        crc = calculateCRC16(command);
                        actualCommand = command + " " + crc.substring(2) + " " + crc.substring(0, 2);
                        break;
                    case "\"HEAT\"":
                        command = modbusWrite + " " + deviceCommands.getMode().getHeat();
                        crc = calculateCRC16(command);
                        actualCommand = command + " " + crc.substring(2) + " " + crc.substring(0, 2);
                        break;
                    case "\"VENTILATION\"":
                        command = modbusWrite + " " + deviceCommands.getMode().getVentilation();
                        crc = calculateCRC16(command);
                        actualCommand = command + " " + crc.substring(2) + " " + crc.substring(0, 2);
                        break;
                }
                break;
            }
            case "FanSpeed": {
                String command;
                String crc;
                switch (commandArr[1]) {
                    case "\"LOW\"":
                        command = modbusWrite + " " + deviceCommands.getFanSpeed().getLow();
                        crc = calculateCRC16(command);
                        actualCommand = command + " " + crc.substring(2) + " " + crc.substring(0, 2);
                        break;
                    case "\"MEDIUM\"":
                        command = modbusWrite + " " + deviceCommands.getFanSpeed().getMedium();
                        crc = calculateCRC16(command);
                        actualCommand = command + " " + crc.substring(2) + " " + crc.substring(0, 2);
                        break;
                    case "\"HIGH\"":
                        command = modbusWrite + " " + deviceCommands.getFanSpeed().getHigh();
                        crc = calculateCRC16(command);
                        actualCommand = command + " " + crc.substring(2) + " " + crc.substring(0, 2);
                        break;
                    case "\"AUTO\"":
                        command = modbusWrite + " " + deviceCommands.getFanSpeed().getAuto();
                        crc = calculateCRC16(command);
                        actualCommand = command + " " + crc.substring(2) + " " + crc.substring(0, 2);
                        break;
                }
                break;
            }
            case "Unit":{
                switch (commandArr[1]){
                    case "\"CELSIUS\"":
                        thermostat.setUnit(Thermostat.Unit.CELSIUS);
                        break;
                    case "\"FAHRENHEIT\"":
                        thermostat.setUnit(Thermostat.Unit.FAHRENHEIT);
                        break;
                }
                break;
            }
        }

        if (actualCommand!=null){
            if (outputStream!=null){
                byte[] byteArr = convertHexStringToByteArray(actualCommand);
                outputStream.write(byteArr);
                outputStream.flush();
            }
        }
    }

    private static byte[] convertHexStringToByteArray(String hexString) {
        String[] hexValues = hexString.split(" ");
        byte[] byteArray = new byte[hexValues.length];
        for (int i = 0; i < hexValues.length; i++) {
            byteArray[i] = (byte) Integer.parseInt(hexValues[i], 16);
        }
        return byteArray;
    }

    private static DevicePOJO readXML(String xmlFilePath){
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

        // Format the CRC result as a 4-character hexadecimal string
        return String.format("%04X", crc);
    }

    private static String decimalToHex(int decimalNumber){
        String binaryString = String.format("%8s", Integer.toBinaryString(Math.abs(decimalNumber))).replace(' ', '0');
        if (decimalNumber < 0) {
            binaryString = twosComplement(binaryString);
        }

        return String.format("%X", Integer.parseInt(binaryString, 2));
    }

    private static String twosComplement(String binaryString) {
        String complement = binaryString.chars().mapToObj(bit -> (char) (bit == '0' ? '1' : '0'))
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();

        // Add 1 to the 1's complement
        return complement.chars().mapToObj(bit -> bit - '0' + 1).collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
    }
}
