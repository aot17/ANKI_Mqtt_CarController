package ch.processcontrol.CarProject;

import org.eclipse.paho.client.mqttv3.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.io.IOException;

public class EmergencyStop {

    private final MqttAsyncClient mqttClient;
    public static final String BROKER = "tcp://192.168.4.1:1883";
    public static final String VEHICLEID = "f4c22c6c0382";
    public static final String BASE_ID = "ATClient2";
    private volatile boolean isEmergency = false;
    private volatile boolean isRedLight = false;
    private final int REDLIGHTTRACKID = 23;


    public EmergencyStop() throws MqttException {
        this.mqttClient = new MqttAsyncClient(BROKER, BASE_ID, null);
    }

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
        String topic = "Anki/Vehicles/U/" + VEHICLEID +"/I" ;
        String payload = "{\"type\":\"connect\", \"payload\":{ \"value\":true } }";
        mqttClient.publish(topic, payload.getBytes(), 0, false);
    }
    public void subscribeToTopics() throws MqttException {
        mqttClient.setCallback(new MQTTMessageHandler());
        String topic = "ATC";
        String topic2 = "ATC/RedLight";
        String topic3 = "Anki/Vehicles/U/+/E/track";
        mqttClient.subscribe(topic,1);
        mqttClient.subscribe(topic2,1);
        mqttClient.subscribe(topic3,1);

    }
    // Getters and Setters for isEmergency
    public boolean getIsEmergency() {
        return isEmergency;
    }

    public void setIsEmergency(boolean isEmergency) throws MqttException {
        this.isEmergency = isEmergency;

        if (isEmergency) {
            String topic = "Anki/Vehicles/U/" + VEHICLEID + "/I" ;
            String emergencyStopPayload = "{\"type\":\"speed\",\"payload\":{\"velocity\":0,\"acceleration\":2000}}";
            mqttClient.publish(topic, emergencyStopPayload.getBytes(), 0, false);
        }
    }

    private void updateRedLightStatus(String payload) {
        if (payload.contains("\"REDLight\"")) {
            isRedLight = true;
        } else if (payload.contains("\"GREENLight\"")) {
            isRedLight = false;
        }
    }

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

                // Parse the JSON payload to extract the trackId
                String json = receivedPayload;
                int trackId = JsonUtils.getIntValue(json, "trackId");
                System.out.println("trackId:" + trackId);

                // Define the topic for controlling the car
                String carControlTopic = "Anki/Vehicles/U/" + VEHICLEID + "/I";

                // Update the red light status based on the received payload
                updateRedLightStatus(receivedPayload);
                System.out.println("Redlight status :" + isRedLight);

                // If in emergency mode and a blinking light command is received, publish it directly
                if(isEmergency && isBlinkingLightCommand(receivedPayload)){
                    mqttClient.publish(carControlTopic, receivedPayload.getBytes(), 0, false);
                }
                // If not in emergency mode, not at a red light, and the topic is "ATC", publish the received payload
                if (!isEmergency && "ATC".equals(topic) && !isRedLight) {
                    mqttClient.publish(carControlTopic, receivedPayload.getBytes(), 0, false);
                }
                // If the car is at a red light, stop the car
                if (isRedLight && trackId == REDLIGHTTRACKID) {
                    String payload = "{\"type\":\"speed\",\"payload\":{\"velocity\":0,\"acceleration\":1200}}";
                    mqttClient.publish(carControlTopic, payload.getBytes(), 0, false);
                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken imdt) {
                System.out.println("EmergencyStop delivery complete...");
            }
        }

    public static void main(String[] args) throws MqttException, IOException, InterruptedException {
        EmergencyStop emergencyStop = new EmergencyStop();

        // 1. Connect to the broker.
        emergencyStop.connectToBroker();
        System.out.println("EmergencyStop connected to broker: " + EmergencyStop.BROKER);

        // 2. Discover cars.
        emergencyStop.carDiscovery();

        // 3. Connect to the vehicle.
        emergencyStop.connectToVehicle();
        System.out.println("EmergencyStop sent connection request to vehicle: " + EmergencyStop.VEHICLEID);

        // 4. Subscribe to topics.
        emergencyStop.subscribeToTopics();
        System.out.println("EmergencyStop subscribed to topics");

        // Loop to toggle emergency status on Enter key press.
        System.out.println("Press Enter to toggle emergency status. Type 'exit' to quit.");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input = "";
        while (!(input = reader.readLine()).equalsIgnoreCase("exit")) {
            emergencyStop.setIsEmergency(!emergencyStop.getIsEmergency()); // toggle the emergency state
            System.out.println("Emergency status set to " + emergencyStop.getIsEmergency());
        }

        // Disconnect from the broker when exiting
        emergencyStop.disconnectFromBroker();
        System.out.println("EmergencyStop disconnected from the broker.");
    }
}