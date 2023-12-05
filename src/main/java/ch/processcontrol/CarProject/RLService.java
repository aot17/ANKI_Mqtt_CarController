package ch.processcontrol.CarProject;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.*;

public class RLService {
    private final MqttClient mqttClient;
    public static final String BROKER = "tcp://192.168.4.1:1883";
    public static final String BASE_ID = "ATClient4";

    public RLService() throws MqttException {
        this.mqttClient = new MqttClient(BROKER, BASE_ID, null);
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

    public void subscribeToTopics() throws MqttException {
        mqttClient.setCallback(new MQTTMessageHandler());
        String topic = "ATC";
        mqttClient.subscribe(topic);
    }

    public void blinkVehicleForever() throws MqttException, InterruptedException {
        while(true) {
            // Turn ON the back light
            String payloadOn = "{ \"type\":\"lights\", \"payload\":{\"front\":\"on\", \"back\":\"on\"}}";
            mqttClient.publish("ATC", payloadOn.getBytes(), 0, false);

            // Wait for 1 second
            Thread.sleep(1000);

            // Turn OFF the back light
            String payloadOff = "{ \"type\":\"lights\", \"payload\":{\"front\":\"off\", \"back\":\"off\"}}";
            mqttClient.publish("ATC", payloadOff.getBytes(), 0, false);

            // Wait for 1 second
            Thread.sleep(1000);
        }
    }

    public void steering() throws MqttException, InterruptedException {
        int velocities = 500;
        int constantAcceleration = 1000; // constant acceleration

        while (true) {
            String payload = String.format("{\"type\":\"speed\",\"payload\":{\"velocity\":%d,\"acceleration\":%d}}", velocities, constantAcceleration);
            mqttClient.publish("ATC", payload.getBytes(), 0, false);

            // Wait for 3 seconds
            Thread.sleep(1000);
        }
    }

    class MQTTMessageHandler implements MqttCallback {

        @Override
        public void connectionLost(Throwable thrwbl) {
            System.out.println("Connection Lost...");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            System.out.printf("Topic: (%s) Payload: (%s) \n", topic, new String(message.getPayload()));
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken imdt) {
            System.out.println("Delivery Complete...");
        }
    }
}
