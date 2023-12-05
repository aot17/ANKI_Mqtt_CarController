package ch.processcontrol.CarProject;

public class JsonUtils {

    public static int getIntValue(String json, String key) {
        String part = getPart(json, key);
        if (part != null) {
            return Integer.parseInt(part);
        }
        return -1; // Or handle error
    }

    public static String getStringValue(String json, String key) {
        return getPart(json, key);
    }

    private static String getPart(String json, String key) {
        String keyWithQuote = "\"" + key + "\":";
        int startIndex = json.indexOf(keyWithQuote) + keyWithQuote.length();
        if (startIndex < keyWithQuote.length()) {
            return null; // Key not found
        }
        int endIndex = json.indexOf(",", startIndex);
        if (endIndex == -1) { // For last element
            endIndex = json.indexOf("}", startIndex);
        }
        return json.substring(startIndex, endIndex).replace("\"", "").trim();
    }
}
