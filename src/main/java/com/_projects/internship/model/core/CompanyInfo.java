package com._projects.internship.model.core;

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

    private String name;           // Raison sociale
    private String address;        // Adresse
    private String email;          // Email de contact
    private String phone;          // Téléphone
    private String website;        // Site web
    private String logoPath;       // Chemin du logo dans MinIO ou stockage
    // Ajoute d'autres champs si besoin (SIRET, secteur, etc.)

    @ManyToOne
    @JoinColumn(name = "company_id")
    private com._projects.internship.model.security.User company;
} 