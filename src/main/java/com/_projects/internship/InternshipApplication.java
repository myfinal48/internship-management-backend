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
		String adminEmail = "admin@admin.com";
		if (userRepository.findByEmail(adminEmail).isEmpty()) {
		  User adminUser = User.builder()
			  .firstName("Admin")
			  .lastName("User")
			  .username("admin")
			  .email(adminEmail)
				.password(passwordEncoder.encode("password"))
			  .role(Role.ADMIN)
			  .sector(sectorRepository.findById(2L).get())
			  .build();
		  User teacherUser = User.builder()
			  .firstName("Murphy")
			  .lastName("Parker")
			  .username("teacher")
			  .email("teacher@teacher.com")
			  .password(passwordEncoder.encode("password"))
			  .role(Role.TEACHER)
			  .sector(sectorRepository.findById(1L).get())
			  .build();
		  User studentUser = User.builder()
			  .firstName("Alice")
			  .lastName("Parker")
			  .username("student")
			  .email("student@student.com")
			  .password(passwordEncoder.encode("password"))
			  .role(Role.STUDENT)
			  .sector(sectorRepository.findById(1L).get())
			  .build();
			  User companyUser = User.builder()
			  .firstName("Jake")
			  .lastName("Argent")
			  .username("Afriland")
			  .email("company@company.com")
			  .password(passwordEncoder.encode("password")) // Encode the password
			  .role(Role.COMPANY)
			  .sector(sectorRepository.findById(1L).get())
			  .build();
		  userRepository.save(adminUser);
		  userRepository.save(studentUser);
		  userRepository.save(teacherUser);
		  userRepository.save(companyUser);
		  System.out.println(">>> Default admin user created: " + adminEmail);
		} else {
		  System.out.println(">>> Admin user already exists.");
		}
	  };
	}

}

