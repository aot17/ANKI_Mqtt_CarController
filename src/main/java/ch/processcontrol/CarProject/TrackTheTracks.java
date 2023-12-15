package ch.processcontrol.CarProject;

import org.eclipse.paho.client.mqttv3.*;

public class TrackTheTracks {

    private final MqttAsyncClient mqttClient;
    public static final String BROKER = "tcp://192.168.4.1:1883";
    public static final String VEHICLEID = "f4c22c6c0382"; // One Vehicle
    public static final String BASE_ID = "ATClient_TrackTheTracks";

    public TrackTheTracks() throws MqttException {
        this.mqttClient = new MqttAsyncClient(BROKER, BASE_ID, null);
    }

    // Connection and subscription handling
    public void connectToBroker() throws MqttException {
        mqttClient.connect().waitForCompletion();
        System.out.println("TrackTheTracks connected to broker: " + BROKER);
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
        String topic = "Anki/Vehicles/U/+/E/track";
        String topicW = "Anki/Vehicles/U/+/E/wheelDistance";
        mqttClient.subscribe(topic, 1);
        mqttClient.subscribe(topicW, 1);
    }

    // Car turning logic
    private String isTurning(int leftWheelSpeed, int rightWheelSpeed) {
        int difference = leftWheelSpeed - rightWheelSpeed;

        if (difference > 5) {
            return "right";
        } else if (difference < -5) {
            return "left";
        } else {
            return "straight";
        }
    }

    class MQTTMessageHandler implements MqttCallback {

        @Override
        public void connectionLost(Throwable thrwbl) {
            System.out.println("TrackTheTracks connection Lost...");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            String receivedPayload = new String(message.getPayload());
            //System.out.printf("Topic: (%s) Payload: (%s) \n", topic, receivedPayload);
            String trackTopic = "Anki/Vehicles/U/" + VEHICLEID + "/E/track";
            String wheelDistanceTopic = "Anki/Vehicles/U/" + VEHICLEID + "/E/wheelDistance";

           if (trackTopic.equals(topic)) {
                String json = receivedPayload;
                int trackId = JsonUtils.getIntValue(json, "trackId");
                System.out.println("trackId:" + trackId);

            } else if (wheelDistanceTopic.equals(topic)) {
                String json = receivedPayload;
                int speedLeft = JsonUtils.getIntValue(json, "left");
                int speedRight = JsonUtils.getIntValue(json, "right");
                String turningDirection = isTurning(speedLeft, speedRight);
                System.out.println("Turning direction: " + turningDirection);
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken imdt) {
            System.out.println("TrackTheTracks delivery complete...");
        }
    }

    public static void main(String[] args) throws Exception {
        TrackTheTracks trackTheTracks = new TrackTheTracks();

        // 1. Connect to the broker.
        trackTheTracks.connectToBroker();
        System.out.println("TrackTheTracks connected to broker: " + TrackTheTracks.BROKER);

        // 2. Discover cars.
        trackTheTracks.carDiscovery();

        // 3. Subscribe to topics.
        trackTheTracks.subscribeToTopics();

        System.in.read();

        // Disconnect from the broker when exiting
        trackTheTracks.disconnectFromBroker();
        System.out.println("TrackTheTracks disconnected from the broker.");
    }
}
