package ch.processcontrol.CarProject;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.*;

public class CarService {
    private final MqttClient mqttClient;
    public static final String BROKER = "tcp://192.168.4.1:1883";
    public static final String BASE_ID = "ATClient";

    public CarService() throws MqttException {
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

    public void driveVehicleWithChangingSpeeds() throws MqttException, InterruptedException {
        int[] velocities = {100, 200, 500};
        int constantAcceleration = 500; // constant acceleration

        int currentIndex = 0;

        while (true) {
            // Format the payload based on the provided structure
            String payload = String.format("{\"type\":\"speed\",\"payload\":{\"velocity\":%d,\"acceleration\":%d}}", velocities[currentIndex], constantAcceleration);
            mqttClient.publish("ATC", payload.getBytes(), 0, false);

            // Wait for 3 seconds
            Thread.sleep(1000);

            // Update the current index to switch to the next speed
            currentIndex = (currentIndex + 1) % velocities.length;
        }
    }
    public void changeLanesPeriodically() throws MqttException, InterruptedException {
        int[] offsets = {-10, 0, 10}; // Possible lane offsets
        int constantVelocity = 500; // Setting a constant velocity for demonstration
        int constantAcceleration = 500; // Setting a constant acceleration for demonstration

        int currentIndex = 0;

        String payload1 = "{\"type\":\"speed\",\"payload\":{\"velocity\":500,\"acceleration\":1000}}";//, constantVelocity, constantAcceleration);

        mqttClient.publish("ATC", payload1.getBytes(), 0, false);

        while (true) {
            // Constructing the JSON payload manually
            String payload = String.format("{\"type\":\"lane\",\"payload\":{\"offset\":%d,\"velocity\":%d,\"acceleration\":%d}}", offsets[currentIndex], constantVelocity, constantAcceleration);//, constantVelocity, constantAcceleration);

            mqttClient.publish("ATC", payload.getBytes(), 0, false);

            // Wait for 5 seconds
            Thread.sleep(5000);

            // Update the current index to switch to the next lane
            currentIndex = (currentIndex + 1) % offsets.length;
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
