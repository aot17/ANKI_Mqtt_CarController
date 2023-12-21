package ch.processcontrol.CarProject;

import org.eclipse.paho.client.mqttv3.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class TrafficLightService {

    private final MqttAsyncClient mqttClient;
    public static final String BASE_ID = "ATClient_TrafficLightService";
    private volatile boolean isRedLight = false;
    private final int REDLIGHTTRACKID = 23;

    public TrafficLightService() throws MqttException {
        this.mqttClient = new MqttAsyncClient(Data.BROKER, BASE_ID, null);
    }

    // Connection and subscription handling
    public void connectToBroker() throws MqttException {
        mqttClient.connect().waitForCompletion();
        System.out.println("Emergency Service Connected to broker: " + Data.BROKER);
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
        for (String vehicleId : Data.activeVehicles) {
            String topic = "Anki/Vehicles/U/" + vehicleId +"/I" ;
            String payload = "{\"type\":\"connect\", \"payload\":{ \"value\":true } }";
            mqttClient.publish(topic, payload.getBytes(), 0, false);
            System.out.println("Connection request sent to vehicle: " + vehicleId);
        }}
    public void subscribeToTopics() throws MqttException {
        mqttClient.setCallback(new MQTTMessageHandler());
        String topic1 = "ATC/I/Speed";
        String topic2 = "ATC/I/TrafficLights";
        String topic4 = "Anki/Vehicles/U/+/E/track";
        mqttClient.subscribe(topic1,1);
        mqttClient.subscribe(topic2,1);
        mqttClient.subscribe(topic4,1);
    }

    // Update redlight status based on received message
    private void updateRedLightStatus(String payload) {
        if (payload.contains("\"REDLight\"")) {
            isRedLight = true;
        } else if (payload.contains("\"GREENLight\"")) {
            isRedLight = false;
        }
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

            // Parse the JSON payload to extract the trackId
            String json = receivedPayload;
            int trackId = JsonUtils.getIntValue(json, "trackId");
            System.out.println("trackId:" + trackId);

            // Extract the vehicleId from the topic
            String vehicleInTopic = extractVehicleId(topic);

            // Update the red light status based on the received payload
            updateRedLightStatus(receivedPayload);
            System.out.println("Redlight status :" + isRedLight);

            for (String vehicleId : Data.activeVehicles) {
                // Filer logic
                if (Data.activeVehicles.contains(vehicleInTopic) && isRedLight && trackId == REDLIGHTTRACKID) { // stop vehicle if it is at the traffic light and it is red
                    System.out.println("VEHICLEID_topic:" + vehicleInTopic);
                    String StopPayload = "{\"type\":\"speed\",\"payload\":{\"velocity\":0,\"acceleration\":1200}}";
                    String carControlTopic = "Anki/Vehicles/U/" + vehicleInTopic + "/I";
                    mqttClient.publish(carControlTopic, StopPayload.getBytes(), 0, false);
                } else if (!isRedLight) {
                    mqttClient.publish("ATC/I/Relay", receivedPayload.getBytes(), 0, false);
                }
            }
        }
        private String extractVehicleId(String topic) {
            // Assuming the topic format is "Anki/Vehicles/U/{vehicleId}/I"
            String[] parts = topic.split("/");
            if (parts.length > 3) {
                return parts[3]; // This is the vehicleId part
            }
            return null; // Or handle this scenario appropriately
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken imdt) {
            System.out.println("trafficLightService delivery complete...");
        }
    }

    public static void main(String[] args) throws MqttException, IOException, InterruptedException {
        TrafficLightService trafficLightService = new TrafficLightService();

        // 1. Connect to the broker.
        trafficLightService.connectToBroker();
        System.out.println("trafficLightService connected to broker: " + Data.BROKER);

        // 2. Discover cars.
        trafficLightService.carDiscovery();

        // 3. Connect to the vehicle.
        trafficLightService.connectToVehicle();

        // 4. Subscribe to topics.
        trafficLightService.subscribeToTopics();
        System.out.println("trafficLightService subscribed to topics");

        System.in.read();

        // 5. Disconnect from the broker when exiting
        trafficLightService.disconnectFromBroker();
        System.out.println("trafficLightService disconnected from the broker.");
    }
}
