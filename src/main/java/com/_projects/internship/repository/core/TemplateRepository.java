package com._projects.internship.repository.core;

import com._projects.internship.model.core.Template;
import org.springframework.data.jpa.repository.JpaRepository;

//A modifier
public interface TemplateRepository extends JpaRepository<Template, Integer> {
}
