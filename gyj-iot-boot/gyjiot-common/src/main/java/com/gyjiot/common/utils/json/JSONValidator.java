package com.gyjiot.common.utils.json;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

public class JSONValidator {

    public static boolean isValidJSON(String jsonString) {
        return isValidJSONObject(jsonString) || isValidJSONArray(jsonString);
    }

    public static boolean isValidJSONObject(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return false;
        }

        String trimmed = jsonString.trim();
        if (!(trimmed.startsWith("{") && trimmed.endsWith("}"))) {
            return false;
        }

        try {
            JSONObject.parseObject(jsonString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidJSONArray(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return false;
        }

        String trimmed = jsonString.trim();
        if (!(trimmed.startsWith("[") && trimmed.endsWith("]"))) {
            return false;
        }

        try {
            JSONArray.parseArray(jsonString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
