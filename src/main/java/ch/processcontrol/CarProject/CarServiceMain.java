package ch.processcontrol.CarProject;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;

public class CarServiceMain {
    public static void main(String[] args) throws MqttException, IOException, InterruptedException {
        CarService carService = new CarService();

        // 1. Connect to the broker.
        carService.connectToBroker();
        System.out.println("CarService connected to broker: " + CarService.BROKER);

        // 2. Discover cars.
        carService.carDiscovery();

        // 3. Subscribe to topics.
        carService.subscribeToTopics();
        System.out.println("CarService subscribed to ATC topic");

        // Launch threads for each exercise:
        new Thread(() -> {
            try {
                carService.blinkVehicleForever(); // Blinking lights
            } catch (MqttException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                carService.driveVehicleWithChangingSpeeds(); // Changing speeds
            } catch (MqttException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                carService.changeLanesPeriodically(); // Changing lanes
            } catch (MqttException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // 4. Wait for user input to end the session.
        System.out.println("CarService receiving updates... Press a key to end.");
        System.in.read();

        // 5. Disconnect from the broker.
        carService.disconnectFromBroker();
        System.out.println("CarService disconnected from broker. Done.");

        System.exit(0);
    }
}
