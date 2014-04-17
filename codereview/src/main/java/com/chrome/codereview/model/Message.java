package com.chrome.codereview.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergeyv on 18/4/14.
 */
public class Message {

    public Message(String text, String sender) {
        this.text = text;
        this.sender = sender;
    }

    private final String text;

    public String getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }

    private final String sender;

    public static Message from(JSONObject jsonObject) throws JSONException {
        String sender = jsonObject.getString("sender");
        String text = jsonObject.getString("text");
        return new Message(text, sender);
    }

    public static List<Message> from (JSONArray jsonArray) {
        ArrayList<Message> result = new ArrayList<Message>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                result.add(from(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}