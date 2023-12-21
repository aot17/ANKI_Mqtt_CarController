package ch.processcontrol.CarProject;

import org.eclipse.paho.client.mqttv3.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class EmergencyService {

    private final MqttAsyncClient mqttClient;
    public static final String BASE_ID = "ATClient_EmergencyService";
    private volatile boolean isEmergency = false;

    public EmergencyService() throws MqttException {
        this.mqttClient = new MqttAsyncClient(Data.BROKER, BASE_ID, null);
    }

    // Connections handling
    public void connectToBroker() throws MqttException {
        mqttClient.connect().waitForCompletion();
    }
    public void disconnectFromBroker() throws MqttException {
        mqttClient.disconnect();
    }

    // Getters and Setters for isEmergency
    public boolean getIsEmergency() {
        return isEmergency;
    }

    public void setIsEmergency(boolean isEmergency) throws MqttException {
        this.isEmergency = isEmergency;
        String emergencyTopic = "ATC/I/Emergency";
        String emergencyStopPayload = "EMERGENCY STOP!";
        String nonEmergencyPayload = "DRIVE SAFE!";

        // Publish the emergency stop message when the emergency state is toggled
        if (isEmergency) {
            mqttClient.publish(emergencyTopic, emergencyStopPayload.getBytes(), 0, false);
            System.out.println("Emergency stop signal sent.");
        } else {
            System.out.println("Emergency status deactivated.");
            mqttClient.publish(emergencyTopic, nonEmergencyPayload.getBytes(), 0, false);
        }
    }

    public static void main(String[] args) throws MqttException, IOException {
        EmergencyService emergencyService = new EmergencyService();

        // Connect to the broker.
        emergencyService.connectToBroker();
        System.out.println("EmergencyService connected to broker: " + Data.BROKER);

        // Loop to toggle emergency status on Enter key press.
        System.out.println("Press Enter to toggle emergency status. Type 'exit' to quit.");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String input = "";
        while (!(input = reader.readLine()).equalsIgnoreCase("exit")) {
            emergencyService.setIsEmergency(!emergencyService.getIsEmergency()); // toggle the emergency state
            System.out.println("Emergency status set to " + emergencyService.getIsEmergency());
        }
        System.in.read();

        // Disconnect from the broker when exiting
        emergencyService.disconnectFromBroker();
        System.out.println("EmergencyService disconnected from the broker.");
    }
}
