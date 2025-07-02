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
		  System.out.println(">>> Utilisateurs par défaut créés : admin, teacher, student, company");
		} else {
		  System.out.println(">>> Utilisateurs déjà existants, création par défaut ignorée.");
		}
	  };
	}

}

