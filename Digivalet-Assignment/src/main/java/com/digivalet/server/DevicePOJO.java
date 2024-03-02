package com.digivalet.server;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "thermostat")
public class DevicePOJO {

    private Power power;
    private Temperature temperature;
    private Mode mode;
    private FanSpeed fanSpeed;
    private String modbusWrite;
    private String modbusRead;

    public String getModbusWrite() {
        return modbusWrite;
    }

    public void setModbusWrite(String modbusWrite) {
        this.modbusWrite = modbusWrite;
    }

    public String getModbusRead() {
        return modbusRead;
    }

    public void setModbusRead(String modbusRead) {
        this.modbusRead = modbusRead;
    }

    public Power getPower() {
        return power;
    }

    @XmlElement(name = "power")
    public void setPower(Power power) {
        this.power = power;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    @XmlElement(name = "temperature")
    public void setTemperature(Temperature temperature) {
        this.temperature = temperature;
    }

    public Mode getMode() {
        return mode;
    }

    @XmlElement(name = "mode")
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public FanSpeed getFanSpeed() {
        return fanSpeed;
    }

    @XmlElement(name = "fanSpeed")
    public void setFanSpeed(FanSpeed fanSpeed) {
        this.fanSpeed = fanSpeed;
    }
}

class Power {

    private String on;
    private String off;
    private String status;

    public String getOn() {
        return on;
    }

    @XmlElement(name = "on")
    public void setOn(String on) {
        this.on = on;
    }

    public String getOff() {
        return off;
    }

    @XmlElement(name = "off")
    public void setOff(String off) {
        this.off = off;
    }

    public String getStatus() {
        return status;
    }

    @XmlElement(name = "status")
    public void setStatus(String status) {
        this.status = status;
    }
}

class Temperature {

    private String set;
    private String status;

    public String getSet() {
        return set;
    }

    @XmlElement(name = "set")
    public void setSet(String set) {
        this.set = set;
    }

    public String getStatus() {
        return status;
    }

    @XmlElement(name = "status")
    public void setStatus(String status) {
        this.status = status;
    }
}

class Mode {

    private String heat;
    private String cool;
    private String ventilation;
    private String status;

    public String getHeat() {
        return heat;
    }

    @XmlElement(name = "heat")
    public void setHeat(String heat) {
        this.heat = heat;
    }

    public String getCool() {
        return cool;
    }

    @XmlElement(name = "cool")
    public void setCool(String cool) {
        this.cool = cool;
    }

    public String getVentilation() {
        return ventilation;
    }

    @XmlElement(name = "ventilation")
    public void setVentilation(String ventilation) {
        this.ventilation = ventilation;
    }

    public String getStatus() {
        return status;
    }

    @XmlElement(name = "status")
    public void setStatus(String status) {
        this.status = status;
    }
}

class FanSpeed {

    private String low;
    private String medium;
    private String high;
    private String auto;
    private String status;

    public String getLow() {
        return low;
    }

    @XmlElement(name = "low")
    public void setLow(String low) {
        this.low = low;
    }

    public String getMedium() {
        return medium;
    }

    @XmlElement(name = "medium")
    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getHigh() {
        return high;
    }

    @XmlElement(name = "high")
    public void setHigh(String high) {
        this.high = high;
    }

    public String getAuto() {
        return auto;
    }

    @XmlElement(name = "auto")
    public void setAuto(String auto) {
        this.auto = auto;
    }

    public String getStatus() {
        return status;
    }

    @XmlElement(name = "status")
    public void setStatus(String status) {
        this.status = status;
    }
}
