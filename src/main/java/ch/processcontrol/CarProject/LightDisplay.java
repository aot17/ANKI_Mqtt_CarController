package ch.processcontrol.CarProject;

import org.eclipse.paho.client.mqttv3.*;
import javax.swing.*;
import java.awt.*;

public class LightDisplay implements MqttCallback {
    private JFrame frame;
    private JLabel lightLabel;
    private MqttClient mqttClient;

    public static final String BROKER = "tcp://192.168.4.1:1883";
    public static final String BASE_ID = "ATClient";

    public LightDisplay() throws MqttException {
        // Setup MQTT Client
        mqttClient = new MqttClient(BROKER, BASE_ID, null);
        mqttClient.setCallback(this);
        mqttClient.connect();
        mqttClient.subscribe("ATC/RedLight");

        // Setup GUI
        frame = new JFrame("Traffic light");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(200, 200);
        lightLabel = new JLabel("Light", SwingConstants.CENTER);
        lightLabel.setFont(new Font("Serif", Font.BOLD, 24));
        frame.add(lightLabel);
        frame.setVisible(true);
    }

    public void setLightColor(String color) {
        SwingUtilities.invokeLater(() -> {
            if ("GREENLight".equals(color)) {
                lightLabel.setText("GREEN Light");
                lightLabel.setBackground(Color.GREEN);
            } else if ("REDLight".equals(color)) {
                lightLabel.setText("RED Light");
                lightLabel.setBackground(Color.RED);
            }
            lightLabel.setOpaque(true);
        });
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Connection to MQTT broker lost!");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String payload = new String(message.getPayload());
        if (topic.equals("ATC/RedLight")) {
            // Extract the signal part of the payload
            if (payload.contains("REDLight")) {
                setLightColor("REDLight");
            } else if (payload.contains("GREENLight")) {
                setLightColor("GREENLight");
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Not used in this context
    }

    public static void main(String[] args) throws MqttException {
        new LightDisplay(); // Start the display
    }
}
