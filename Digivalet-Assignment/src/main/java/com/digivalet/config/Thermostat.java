package com.digivalet.config;

import java.io.Serializable;

public class Thermostat implements Serializable {
    public enum Power{
        ON, OFF
    }

    public enum FanSpeed{
        LOW, MEDIUM, HIGH, AUTO
    }

    public enum Mode{
        HEAT, COOL, VENTILATION
    }

    public enum Unit{
        CELSIUS, FAHRENHEIT
    }

    double temperature;
    Power power;
    FanSpeed fanSpeed;
    Mode mode;
    Unit unit;

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public Power getPower() {
        return power;
    }

    public void setPower(Power power) {
        this.power = power;
    }

    public FanSpeed getFanSpeed() {
        return fanSpeed;
    }

    public void setFanSpeed(FanSpeed fanSpeed) {
        this.fanSpeed = fanSpeed;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "Thermostat{" +
                "temperature=" + temperature + " "+ unit+
                ", power=" + power +
                ", fanSpeed=" + fanSpeed +
                ", mode=" + mode +
                '}';
    }
}
