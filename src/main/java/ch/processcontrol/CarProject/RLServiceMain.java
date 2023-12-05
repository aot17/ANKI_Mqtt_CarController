package ch.processcontrol.CarProject;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;

public class RLServiceMain {
    public static void main(String[] args) throws MqttException, IOException, InterruptedException {
        RLService rlService = new RLService();

        // 1. Connect to the broker.
        rlService.connectToBroker();
        System.out.println("RLService connected to broker: " + RLService.BROKER);

        // 2. Discover cars.
        rlService.carDiscovery();

        // 3. Subscribe to topics.
        rlService.subscribeToTopics();
        System.out.println("RLService subscribed to ATC topic");

        new Thread(() -> {
            try {
                rlService.blinkVehicleForever(); // Blinking lights
            } catch (MqttException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                rlService.steering(); // Changing speeds
            } catch (MqttException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // 4. Wait for user input to end the session.
        System.out.println("RLService receiving updates... Press a key to end.");
        System.in.read();

        // 5. Disconnect from the broker.
        rlService.disconnectFromBroker();
        System.out.println("RLService disconnected from broker. Done.");

        System.exit(0);
    }
}
