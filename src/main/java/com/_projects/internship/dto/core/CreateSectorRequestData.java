package com._projects.internship.dto.core;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateSectorRequestData {
  @NotBlank(message = "The name can not be empty")
  private String name;
}