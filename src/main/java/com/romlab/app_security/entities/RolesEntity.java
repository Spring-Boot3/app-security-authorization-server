package com.romlab.app_security.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigInteger;

@Entity
@Table(name = "roles")
@Data
public class RolesEntity {

//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private BigInteger id;
    @Id
    @Column(name = "role_name")
    private String name;
    private String description;

}
