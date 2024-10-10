package edu.iu.uits.lms.common.server;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FaviconProperties {

    private boolean enabled;
    private String url;
    private TYPE type;


    public enum TYPE {
        URL, PATH
    }
}
