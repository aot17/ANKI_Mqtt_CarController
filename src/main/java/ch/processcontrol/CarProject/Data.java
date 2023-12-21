package ch.processcontrol.CarProject;

import java.util.Arrays;
import java.util.List;

public interface Data {
    String BROKER = "tcp://192.168.4.1:1883";

    //Track the Tracks ONE Vehicle ID
    String VEHICLEID = "f4c22c6c0382"; // One Vehicle

    //Traffic Light Multiple Vehicles
    List<String> activeVehicles = Arrays.asList(
            "f4c22c6c0382",
            "d205effe02cb",
            "f2e85f2f5770",
            "cb443e1e4025"
    );
}
