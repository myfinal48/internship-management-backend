package com._projects.internship.service.core;

import com._projects.internship.dto.core.TemplateDTO;
import com._projects.internship.model.core.Template;

import java.util.List;

public interface TemplateService {

    Template createTemplate(TemplateDTO dto);

    Template updateTemplate(TemplateDTO dto);

    Template getTemplateById(Long id);
    
    List<Template> getTemplates();

    void deleteTemplate(Long id);
    
    //A completer avec d'autres services si necessaire
}
