package com.romlab.app_security.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

@Entity
@Getter
@Setter
@Table(name = "roles")
public class RolesEntity {

//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private BigInteger id;
    @Id
    @Column(name = "role_name")
    private String name;
    private String description;

}
