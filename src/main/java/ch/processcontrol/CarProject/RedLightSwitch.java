package ch.processcontrol.CarProject;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class RedLightSwitch {
    private final MqttAsyncClient mqttClient;
    public static final String BROKER = "tcp://192.168.4.1:1883";
    public static final String BASE_ID = "ATClient5";

    public RedLightSwitch() throws MqttException {
        this.mqttClient = new MqttAsyncClient(BROKER, BASE_ID, null);
    }

    public void connectToBroker() throws MqttException {
        mqttClient.connect().waitForCompletion();
        System.out.println("RedLightSwitch connected to broker: " + BROKER);
    }

    public void disconnectFromBroker() throws MqttException {
        mqttClient.disconnect();
    }

    public void redLightSignal() throws MqttException, InterruptedException {
        while (true) {
            // Send GREENLight message
            String greenLightPayload = "{\"signal\":\"GREENLight\"}";
            mqttClient.publish("ATC/RedLight", greenLightPayload.getBytes(), 0, false);
            System.out.println("GREENLight signal sent");

            // Wait for 5 seconds
            Thread.sleep(5000);

            // Send REDLight message
            String redLightPayload = "{\"signal\":\"REDLight\"}";
            mqttClient.publish("ATC/RedLight", redLightPayload.getBytes(), 0, false);
            System.out.println("REDLight signal sent");

            // Wait for 5 seconds
            Thread.sleep(5000);
        }
    }
    public static void main(String[] args) throws Exception {
        RedLightSwitch redLightSwitch = new RedLightSwitch();

        redLightSwitch.connectToBroker();
        redLightSwitch.redLightSignal();

        System.in.read();

        redLightSwitch.disconnectFromBroker();
        System.out.println("RedLightSwitch disconnected from the broker.");
    }
}



