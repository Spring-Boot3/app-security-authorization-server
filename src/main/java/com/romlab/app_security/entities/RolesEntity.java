package com.romlab.app_security.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigInteger;

@Entity
@Table(name = "roles")
@Data
public class RolesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;
    private String role;
    private String description;

}
