package com.romlab.app_security.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigInteger;

@Entity
@Getter
@Setter
@Table(name = "partners")
public class PartnerEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;
    private String clientId;
    private String clientName;
    private String clientSecret;
    private String scopes;
    private String grantTypes;
    private String authenticationMethods;
    private String redirectUri;
    private String redirectUriLogout;

}
