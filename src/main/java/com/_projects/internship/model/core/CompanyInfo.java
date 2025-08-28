package com._projects.internship.model.core;

import com._projects.internship.model.security.User;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private String email;
    private String phone;
    private String website;
    private String logoPath;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private User company;
}