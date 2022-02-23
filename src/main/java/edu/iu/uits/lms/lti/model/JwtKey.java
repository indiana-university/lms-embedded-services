package edu.iu.uits.lms.lti.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class JwtKey implements Serializable {
   private String e;
   private String use;
   private String alg;
   private String kty;
   private String n;
   private String kid;
}
