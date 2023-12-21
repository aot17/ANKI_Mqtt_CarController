package ch.processcontrol.CarProject;

import org.eclipse.paho.client.mqttv3.*;

public class RLService {
    private final MqttClient mqttClient;
    public static final String BASE_ID = "ATClient_RLService";

    public RLService() throws MqttException {
        this.mqttClient = new MqttClient(Data.BROKER, BASE_ID, null);
    }

    // Connection handling
    public void connectToBroker() throws MqttException {
        mqttClient.connect();
    }

    public void disconnectFromBroker() throws MqttException {
        mqttClient.disconnect();
    }

    public void carDiscovery () throws MqttException {
        String topic = "Anki/Hosts/U/hyperdrive/I";
        String payload = "{\"type\":\"discover\", \"payload\":{ \"value\":true} }";
        mqttClient.publish(topic, payload.getBytes(), 0, false);
    }

    // Vehicles blinking light method
    public void blinkVehicleForever() throws MqttException, InterruptedException {
        while(true) {
            // Turn ON the back light
            String payloadOn = "{ \"type\":\"lights\", \"payload\":{\"front\":\"on\", \"back\":\"on\"}}";
            mqttClient.publish("ATC/I/Blinking", payloadOn.getBytes(), 0, false);

            // Wait for 1 second
            Thread.sleep(1000);

            // Turn OFF the back light
            String payloadOff = "{ \"type\":\"lights\", \"payload\":{\"front\":\"off\", \"back\":\"off\"}}";
            mqttClient.publish("ATC/I/Blinking", payloadOff.getBytes(), 0, false);

            // Wait for 1 second
            Thread.sleep(1000);
        }
    }

    // Drive at steady speed
    public void steering() throws MqttException, InterruptedException {
        int velocities = 500;
        int constantAcceleration = 1000; // constant acceleration

        while (true) {
            String payload = String.format("{\"type\":\"speed\",\"payload\":{\"velocity\":%d,\"acceleration\":%d}}", velocities, constantAcceleration);
            mqttClient.publish("ATC/I/Speed", payload.getBytes(), 0, false);

            // Wait for 3 seconds
            Thread.sleep(1000);
        }
    }
}
