package com._projects.internship;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com._projects.internship.model.security.Role;
import com._projects.internship.model.security.User;
import com._projects.internship.repository.core.SectorRepository;
import com._projects.internship.repository.security.UserRepository;

import lombok.RequiredArgsConstructor;

@SpringBootApplication
@RequiredArgsConstructor
public class InternshipApplication {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final SectorRepository sectorRepository;

	public static void main(String[] args) {
		SpringApplication.run(InternshipApplication.class, args);
	}

	@Bean
	public CommandLineRunner createDefaultAdminUser() {
	  return args -> {
		// Création automatique des secteurs si la table est vide
		if (sectorRepository.count() == 0) {
		  var secteur1 = new com._projects.internship.model.core.Sector();
		  secteur1.setName("Informatique");
		  var secteur2 = new com._projects.internship.model.core.Sector();
		  secteur2.setName("Génie Civil");
		  sectorRepository.save(secteur1);
		  sectorRepository.save(secteur2);
		  System.out.println(">>> Secteurs par défaut créés : Informatique, Génie Civil");
		}

		// Création automatique des utilisateurs si la table user est vide
		if (userRepository.count() == 0) {
		  var sector2 = sectorRepository.findById(2L).orElse(null);
		  var sector1 = sectorRepository.findById(1L).orElse(null);
		  if (sector2 == null || sector1 == null) {
			System.err.println(">>> Erreur : Les secteurs nécessaires n'existent pas en base de données.");
			return;
		  }
		  User adminUser = User.builder()
			  .firstName("Admin")
			  .lastName("User")
			  .username("admin")
			  .email("admin@admin.com")
			  .password(passwordEncoder.encode("password"))
			  .role(Role.ADMIN)
			  .sector(sector2)
			  .build();
		  User teacherUser = User.builder()
			  .firstName("Murphy")
			  .lastName("Parker")
			  .username("teacher")
			  .email("teacher@teacher.com")
			  .password(passwordEncoder.encode("password"))
			  .role(Role.TEACHER)
			  .sector(sector1)
			  .build();
		  User studentUser = User.builder()
			  .firstName("Alice")
			  .lastName("Parker")
			  .username("student")
			  .email("student@student.com")
			  .password(passwordEncoder.encode("password"))
			  .role(Role.STUDENT)
			  .sector(sector1)
			  .build();
		  User companyUser = User.builder()
			  .firstName("Jake")
			  .lastName("Argent")
			  .username("Afriland")
			  .email("company@company.com")
			  .password(passwordEncoder.encode("password"))
			  .role(Role.COMPANY)
			  .sector(sector1)
			  .build();
		  userRepository.save(adminUser);
		  userRepository.save(studentUser);
		  userRepository.save(teacherUser);
		  userRepository.save(companyUser);
		  
		  // Création d'utilisateurs supplémentaires pour les tests
		  try {
			  User student2 = User.builder()
				  .firstName("Kamto")
				  .lastName("Maurice")
				  .username("student2")
				  .email("student2@student.com")
				  .password(passwordEncoder.encode("password"))
				  .role(Role.STUDENT)
				  .sector(sector1)
				  .build();
			  
			  User company2 = User.builder()
				  .firstName("pfk")
				  .lastName("Paul")
				  .username("Orange")
				  .email("company2@company.com")
				  .password(passwordEncoder.encode("password"))
				  .role(Role.COMPANY)
				  .sector(sector1)
				  .build();
			  
			  userRepository.save(student2);
			  userRepository.save(company2);
			  System.out.println(">>> Utilisateurs supplémentaires créés avec succès");
		  } catch (Exception e) {
			  System.err.println(">>> Erreur lors de la création des utilisateurs supplémentaires: " + e.getMessage());
			  e.printStackTrace();
		  }
		  System.out.println(">>> Utilisateurs par défaut créés : admin, teacher, student, company, student2, company2");
		} else {
		  System.out.println(">>> Utilisateurs déjà existants, création par défaut ignorée.");
		  
		  // Création des utilisateurs supplémentaires même si la base n'est pas vide
		  var sector1 = sectorRepository.findById(1L).orElse(null);
		  if (sector1 == null) {
			System.err.println(">>> Erreur : Le secteur nécessaire n'existe pas en base de données.");
			return;
		  }
		  
		  // Vérifier si student2 existe déjà
		  if (userRepository.findByEmail("student2@student.com").isEmpty()) {
			try {
			  User student2 = User.builder()
				.firstName("Kamto")
				.lastName("Maurice")
				.username("student2")
				.email("student2@student.com")
				.password(passwordEncoder.encode("password"))
				.role(Role.STUDENT)
				.sector(sector1)
				.build();
			  userRepository.save(student2);
			  System.out.println(">>> Utilisateur student2 créé avec succès");
			} catch (Exception e) {
			  System.err.println(">>> Erreur lors de la création de student2: " + e.getMessage());
			}
		  }
		  
		  // Vérifier si company2 existe déjà
		  if (userRepository.findByEmail("company2@company.com").isEmpty()) {
			try {
			  User company2 = User.builder()
				.firstName("pfk")
				.lastName("Paul")
				.username("Orange")
				.email("company2@company.com")
				.password(passwordEncoder.encode("password"))
				.role(Role.COMPANY)
				.sector(sector1)
				.build();
			  userRepository.save(company2);
			  System.out.println(">>> Utilisateur company2 créé avec succès");
			} catch (Exception e) {
			  System.err.println(">>> Erreur lors de la création de company2: " + e.getMessage());
			}
		  }
		}
	  };
	}

}

