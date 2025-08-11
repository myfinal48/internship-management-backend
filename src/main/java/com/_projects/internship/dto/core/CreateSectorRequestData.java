package com._projects.internship.dto.core;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateSectorRequestData {
  @NotBlank(message = "The name can not be empty")
  @Size(min = 3, message = "The name must be at least 3 characters long")
  private String name;
}