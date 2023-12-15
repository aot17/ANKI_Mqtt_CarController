package ch.processcontrol.CarProject;

import org.eclipse.paho.client.mqttv3.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class UltimateRelay {

    private final MqttAsyncClient mqttClient;
    public static final String BROKER = "tcp://192.168.4.1:1883";
    private List<String> activeVehicles = Arrays.asList("cec233dec1cb", "f4c22c6c0382", "cb443e1e4025", "d98ebab7c206");
    //private List<String> activeVehicles = Arrays.asList("f4c22c6c0382");

    public static final String BASE_ID = "ATClient_UltimateRelay";
    private volatile boolean isEmergency = false;


    public UltimateRelay() throws MqttException {
        this.mqttClient = new MqttAsyncClient(BROKER, BASE_ID, null);
    }

    // Connection and subscription handling
    public void connectToBroker() throws MqttException {
        mqttClient.connect().waitForCompletion();
        System.out.println("Emergency Service Connected to broker: " + BROKER);
    }
    public void disconnectFromBroker() throws MqttException {
        mqttClient.disconnect();
    }

    public void carDiscovery () throws MqttException {
        String topic = "Anki/Hosts/U/hyperdrive/I";
        String payload = "{\"type\":\"discover\", \"payload\":{ \"value\":true} }";
        mqttClient.publish(topic, payload.getBytes(), 0, false);
    }

    public void connectToVehicle() throws MqttException {
        for (String vehicleId : activeVehicles) {
            String topic = "Anki/Vehicles/U/" + vehicleId +"/I" ;
            String payload = "{\"type\":\"connect\", \"payload\":{ \"value\":true } }";
            mqttClient.publish(topic, payload.getBytes(), 0, false);
            System.out.println("Connection request sent to vehicle: " + vehicleId);
        }}
    public void subscribeToTopics() throws MqttException {
        mqttClient.setCallback(new MQTTMessageHandler());
        String topic1 = "ATC/I/Relay";
        String topic2 = "ATC/I/Blinking";
        String topic3 = "ATC/I/Emergency";
        String topic4 = "ATC/I/Steering";
        mqttClient.subscribe(topic1,1);
        mqttClient.subscribe(topic2,1);
        mqttClient.subscribe(topic3,1);
        mqttClient.subscribe(topic4,1);
    }
    // Update emergency status
    private void updateIsEmergencyStatus(String payload) {
        if (payload.contains("EMERGENCY")) {
            isEmergency = true;
        } else if (payload.contains("SAFE")) {
            isEmergency = false;
        }
    }
    // Identify blinking messages
    private boolean isBlinkingLightCommand(String payload) {
        return payload.contains("\"type\":\"lights\"");
    }

    class MQTTMessageHandler implements MqttCallback {

        @Override
        public void connectionLost(Throwable thrwbl) {
            System.out.println("Emergency stop connection Lost...");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            // Convert the received message payload to a string
            String receivedPayload = new String(message.getPayload());
            System.out.printf("Topic: (%s) Payload: (%s) \n", topic, receivedPayload);

            // update the emergency status
            updateIsEmergencyStatus(receivedPayload);
            System.out.println(isEmergency);

            for (String vehicleId : activeVehicles) {
                String carControlTopic = "Anki/Vehicles/U/" + vehicleId + "/I";
                if (isBlinkingLightCommand(receivedPayload)) { // always blinking
                    mqttClient.publish(carControlTopic, receivedPayload.getBytes(), 0, false);
                }
                if (isEmergency) { // if is emergency, stop !
                    String payload = "{\"type\":\"speed\",\"payload\":{\"velocity\":0,\"acceleration\":1500}}";
                    mqttClient.publish(carControlTopic, payload.getBytes(), 0, false);
                } else {
                    mqttClient.publish(carControlTopic, receivedPayload.getBytes(), 0, false);
                }
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken imdt) {
            System.out.println("EmergencyStop delivery complete...");
        }
    }

    public static void main(String[] args) throws MqttException, IOException {
        UltimateRelay ultimateRelay = new UltimateRelay();

        // 1. Connect to the broker.
        ultimateRelay.connectToBroker();

        // 2. Discover cars.
        ultimateRelay.carDiscovery();

        // 3. Connect to the vehicle.
        ultimateRelay.connectToVehicle();

        // 4. Subscribe to topics.
        ultimateRelay.subscribeToTopics();
        System.out.println("UltimateRelay subscribed to topics");

        System.in.read();

        // 5. Disconnect from the broker when exiting
        ultimateRelay.disconnectFromBroker();
        System.out.println("UltimateRelay disconnected from the broker.");
    }
}
