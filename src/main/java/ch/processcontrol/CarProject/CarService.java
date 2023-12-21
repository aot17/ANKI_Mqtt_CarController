package ch.processcontrol.CarProject;

import org.eclipse.paho.client.mqttv3.*;

public class CarService {
    private final MqttClient mqttClient;
    public static final String BASE_ID = "ATClient_CarService";

    // Connections and subscription basics
    public CarService() throws MqttException {
        this.mqttClient = new MqttClient(Data.BROKER, BASE_ID, null);
    }

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
            // Turn ON the lights
            String payloadOn = "{ \"type\":\"lights\", \"payload\":{\"front\":\"on\", \"back\":\"on\"}}";
            mqttClient.publish("ATC/I/Blinking", payloadOn.getBytes(), 0, false);

            // Wait for 1 second
            Thread.sleep(1000);

            // Turn OFF the lights
            String payloadOff = "{ \"type\":\"lights\", \"payload\":{\"front\":\"off\", \"back\":\"off\"}}";
            mqttClient.publish("ATC/I/Blinking", payloadOff.getBytes(), 0, false);

            // Wait for 1 second
            Thread.sleep(1000);
        }
    }

    // Vehicle changes speed will driving
    public void driveVehicleWithChangingSpeeds() throws MqttException, InterruptedException {
        int[] velocities = {200, 300, 500};
        int constantAcceleration = 500; // constant acceleration

        int currentIndex = 0;

        while (true) {
            // Format the payload based on the provided structure
            String payload = String.format("{\"type\":\"speed\",\"payload\":{\"velocity\":%d,\"acceleration\":%d}}", velocities[currentIndex], constantAcceleration);
            mqttClient.publish("ATC/I/Steering", payload.getBytes(), 0, false);

            // Wait for 3 seconds
            Thread.sleep(3000);

            // Update the current index to switch to the next speed
            currentIndex = (currentIndex + 1) % velocities.length;
        }
    }

    // Vehicle changes lane will driving
    public void changeLanesPeriodically() throws MqttException, InterruptedException {
        int[] offsets = {-10, 0, 10}; // Possible lane offsets

        int currentIndex = 0;

        String payload1 = "{\"type\":\"speed\",\"payload\":{\"velocity\":500,\"acceleration\":1000}}";

        mqttClient.publish("ATC/I/Steering", payload1.getBytes(), 0, false);

        while (true) {
            // Constructing the JSON payload manually
            String payload = String.format("{\"type\":\"lane\",\"payload\":{\"offset\":%d,\"velocity\":500,\"acceleration\":500}}", offsets[currentIndex]);

            mqttClient.publish("ATC/I/Steering", payload.getBytes(), 0, false);

            // Wait for 3 seconds
            Thread.sleep(3000);

            // Update the current index to switch to the next lane
            currentIndex = (currentIndex + 1) % offsets.length;
        }
    }
}
