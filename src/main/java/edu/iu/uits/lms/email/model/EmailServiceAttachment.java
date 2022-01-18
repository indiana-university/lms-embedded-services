package edu.iu.uits.lms.email.model;

import lombok.Data;

import java.io.Serializable;
import java.net.URL;

@Data
public class EmailServiceAttachment implements Serializable {
    private String filename;
    private URL url;
}
