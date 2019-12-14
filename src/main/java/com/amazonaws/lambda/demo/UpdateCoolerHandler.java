package com.amazonaws.lambda.demo;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import com.amazonaws.services.iotdata.AWSIotData;
import com.amazonaws.services.iotdata.AWSIotDataClientBuilder;
import com.amazonaws.services.iotdata.model.UpdateThingShadowRequest;
import com.amazonaws.services.iotdata.model.UpdateThingShadowResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.annotation.JsonCreator;

public class UpdateCoolerHandler implements RequestHandler<Event, String> {

    @Override
    public String handleRequest(Event event, Context context) {
        context.getLogger().log("Input: " + event);

        AWSIotData iotData = AWSIotDataClientBuilder.standard().build();

        String payload = getPayload(event.tags);

        UpdateThingShadowRequest updateThingShadowRequest  = 
                new UpdateThingShadowRequest()
                    .withThingName(event.device)
                    .withPayload(ByteBuffer.wrap(payload.getBytes()));

        UpdateThingShadowResult result = iotData.updateThingShadow(updateThingShadowRequest);
        byte[] bytes = new byte[result.getPayload().remaining()];
        result.getPayload().get(bytes);
        String resultString = new String(bytes);
        return resultString;
    }

    private String getPayload(ArrayList<Tag> tags) {
        String tagstr = "";
        String motor_step = "";
        
            if(tags.get(0).tagName.equals("user_mode"))
            {
               if(tags.get(0).tagValue.equals("auto"))
               {
                  float temp = Float.parseFloat(tags.get(2).tagValue);
                  if(temp < 20)
                     motor_step = "0";
                  else if(temp >= 20 && temp < 25)
                     motor_step = "1";
                  else if(temp >= 25 && temp < 30)
                     motor_step = "2";
                  else
                     motor_step = "3";
               }
               else
                   motor_step = tags.get(1).tagValue;
            }   
            tagstr = String.format("\"%s\" : \"%s\",\"%s\" : \"%s\",\"%s\" : \"%s\"", 
            		tags.get(2).tagName, tags.get(2).tagValue, tags.get(1).tagName, motor_step, tags.get(0).tagName, tags.get(0).tagValue);
            
       
        return String.format("{ \"state\": { \"desired\": {%s} } }", tagstr);
    }

}

class Event {
    public String device;
    public ArrayList<Tag> tags;

    public Event() {
         tags = new ArrayList<Tag>();
    }
}

class Tag {
    public String tagName;
    public String tagValue;

    @JsonCreator 
    public Tag() {
    }

    public Tag(String n, String v) {
        tagName = n;
        tagValue = v;
    }
}
