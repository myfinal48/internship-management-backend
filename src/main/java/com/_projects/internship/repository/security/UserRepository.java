package com._projects.internship.repository.security;

import com._projects.internship.model.core.Sector;
import com._projects.internship.model.security.Role;
import com._projects.internship.model.security.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

    long countByRoleIn(Collection<Role> roles);

    boolean findByIdAndRole(Long companyId, Role company);

    boolean existsByIdAndRole(Long companyId, Role company);

    List<User> findByRoleAndSector(Role role, Sector sector);

    @Query("SELECT u FROM User u WHERE " +
            "(:role IS NULL OR u.role = :role) AND " +
            "(:sector IS NULL OR u.sector = :sector)")
    List<User> findByCriteria(@Param("role") Role role,
            @Param("sector") Sector sector);

    boolean existsBySector(Sector sector);
}