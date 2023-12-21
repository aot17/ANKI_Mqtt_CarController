package ch.processcontrol.CarProject;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class RedLightSwitch {
    private final MqttAsyncClient mqttClient;
    public static final String BASE_ID = "ATClient_RedLightSwitch";

    public RedLightSwitch() throws MqttException {
        this.mqttClient = new MqttAsyncClient(Data.BROKER, BASE_ID, null);
    }

    // Connections handling
    public void connectToBroker() throws MqttException {
        mqttClient.connect().waitForCompletion();
    }

    public void disconnectFromBroker() throws MqttException {
        mqttClient.disconnect();
    }

    // Sending light signal
    public void redLightSignal() throws MqttException, InterruptedException {
        while (true) {
            // Send GREENLight message
            String greenLightPayload = "{\"signal\":\"GREENLight\"}";
            mqttClient.publish("ATC/I/TrafficLights", greenLightPayload.getBytes(), 0, false);
            System.out.println("GREENLight signal sent");

            // Wait for 5 seconds
            Thread.sleep(5000);

            // Send REDLight message
            String redLightPayload = "{\"signal\":\"REDLight\"}";
            mqttClient.publish("ATC/I/TrafficLights", redLightPayload.getBytes(), 0, false);
            System.out.println("REDLight signal sent");

            // Wait for 5 seconds
            Thread.sleep(5000);
        }
    }
    public static void main(String[] args) throws Exception {
        RedLightSwitch redLightSwitch = new RedLightSwitch();

        // 1. Connect to the broker.
        redLightSwitch.connectToBroker();
        System.out.println("RedLightSwitch connected to broker: " + Data.BROKER);

        // 2. Initiate RedLightSignal
        redLightSwitch.redLightSignal();

        System.in.read();

        redLightSwitch.disconnectFromBroker();
        System.out.println("RedLightSwitch disconnected from the broker.");
    }
}



