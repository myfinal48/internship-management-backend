package com._projects.internship.service.core;

import com._projects.internship.model.core.Convention;
import com._projects.internship.model.core.CompanyInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class ConventionPdfGenerationService {

    private final ConventionStorageService conventionStorageService;

    public byte[] generateConventionPdf(Convention convention) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            float margin = 50;
            float y = PDRectangle.A4.getHeight() - margin;
            float sectionSpacing = 25; // Espace entre sections (plus grand)
            float lineSpacing = 15;   // Espace entre lignes de texte (plus petit)

            // Affichage du logo et des infos entreprise depuis companyInfo si présent
            CompanyInfo companyInfo = convention.getCompanyInfo();
            try {
                if (companyInfo != null && companyInfo.getLogoPath() != null) {
                    byte[] logoBytes = conventionStorageService.getFile(companyInfo.getLogoPath());
                    PDImageXObject logo = PDImageXObject.createFromByteArray(document, logoBytes, "logo");
                    contentStream.drawImage(logo, margin, y - 60, 80, 60);
                }
            } catch (Exception e) {
                // Si pas de logo, ignorer
            }
            if (companyInfo != null) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
                contentStream.newLineAtOffset(PDRectangle.A4.getWidth() - margin - 200, y - 20);
                contentStream.showText("Entreprise : " + (companyInfo.getName() != null ? companyInfo.getName() : ""));
                contentStream.newLineAtOffset(0, -15);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.showText("Adresse : " + (companyInfo.getAddress() != null ? companyInfo.getAddress() : ""));
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("Email : " + (companyInfo.getEmail() != null ? companyInfo.getEmail() : ""));
                if (companyInfo.getPhone() != null) {
                    contentStream.newLineAtOffset(0, -15);
                    contentStream.showText("Téléphone : " + companyInfo.getPhone());
                }
                if (companyInfo.getWebsite() != null) {
                    contentStream.newLineAtOffset(0, -15);
                    contentStream.showText("Site web : " + companyInfo.getWebsite());
                }
                contentStream.endText();
            }

            y -= 70;

            // EN-TÊTE PRINCIPALE
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
            float titleWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD).getStringWidth("CONVENTION DE STAGE") / 1000 * 11;
            contentStream.newLineAtOffset((PDRectangle.A4.getWidth() - titleWidth) / 2, y);
            contentStream.showText("CONVENTION DE STAGE");
            contentStream.endText();
            y -= sectionSpacing + 6;

            // Séparateur
            contentStream.setStrokingColor(0.59f, 0.59f, 0.59f); // gris clair
            contentStream.moveTo(margin, y);
            contentStream.lineTo(PDRectangle.A4.getWidth() - margin, y);
            contentStream.stroke();
            y -= sectionSpacing + 10;

            // Titre de la convention
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Titre de la convention : " + (convention.getTitle() != null ? convention.getTitle() : ""));
            contentStream.endText();
            y -= lineSpacing ;

            // Date de début
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Date de début : " + (convention.getInternshipStartDate() != null ? convention.getInternshipStartDate().toString() : ""));
            contentStream.endText();
            y -= lineSpacing;

            // Date de fin
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Date de fin : " + (convention.getInternshipEndDate() != null ? convention.getInternshipEndDate().toString() : ""));
            contentStream.endText();
            y -= lineSpacing + 10;

            // Description du stage
            if (convention.getDescription() != null) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText("Description du stage :");
                contentStream.endText();
                y -= lineSpacing + 6;
                List<String> descLines = wrapText(convention.getDescription(), 90);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.newLineAtOffset(margin, y);
                for (String line : descLines) {
                    contentStream.showText(line.trim());
                    contentStream.newLineAtOffset(0, -lineSpacing);
                    y -= lineSpacing;
                }
                contentStream.endText();
                y -= sectionSpacing;
            }

              // Objectif du stage
              if (convention.getObjectives() != null && !convention.getObjectives().isEmpty()) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText("Objectif du stage :");
                contentStream.endText();
                y -= lineSpacing;
                List<String> objLines = wrapText(convention.getObjectives(), 90);
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.newLineAtOffset(margin, y);
                for (String line : objLines) {
                    contentStream.showText(line.trim());
                    contentStream.newLineAtOffset(0, -lineSpacing);
                    y -= lineSpacing;
                }
                contentStream.endText();
                y -= sectionSpacing;
            }
            y -= sectionSpacing + 6;

            // Nom de l'entreprise
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Nom du superviseur : " + (companyInfo != null && companyInfo.getName() != null ? companyInfo.getName() : ""));
            contentStream.endText();
            y -= lineSpacing + 10;

            // Nom du superviseur
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Nom du superviseur : " + (convention.getSupervisorName() != null ? convention.getSupervisorName() : ""));
            contentStream.endText();
            y -= lineSpacing;

            // Email du superviseur
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Email du superviseur : " + (convention.getSupervisorEmail() != null ? convention.getSupervisorEmail() : ""));
            contentStream.endText();
            y -= lineSpacing;

            // Heures/semaine
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Heures/semaine : " + (convention.getWeeklyHours() != null ? convention.getWeeklyHours() : ""));
            contentStream.endText();
            y -= lineSpacing + 10;

            // Section PARTIES CONCERNÉES
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("PARTIES CONCERNÉES");
            contentStream.endText();
            y -= sectionSpacing;

            // Étudiant
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Étudiant:");
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.endText();
            y -= lineSpacing;
            if (convention.getStudent() != null) {
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText("Nom: " + (convention.getStudent().getFirstName() != null ? convention.getStudent().getFirstName() : "") +
                                     " " + (convention.getStudent().getLastName() != null ? convention.getStudent().getLastName() : ""));
                contentStream.endText();
                y -= lineSpacing;
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText("Email: " + (convention.getStudent().getEmail() != null ? convention.getStudent().getEmail() : "Non spécifié"));
                contentStream.endText();
                y -= lineSpacing;
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText("ID: " + convention.getStudent().getId());
                contentStream.endText();
                y -= lineSpacing;
            } else {
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText("Non spécifié");
                contentStream.endText();
                y -= lineSpacing;
            }
            y -= sectionSpacing;

            // Entreprise
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
            contentStream.newLineAtOffset(margin, y);
            contentStream.showText("Entreprise:");
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.endText();
            y -= lineSpacing;
            if (convention.getCompany() != null) {
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText("Nom: " + (convention.getCompany().getFirstName() != null ? convention.getCompany().getFirstName() : "") +
                                     " " + (convention.getCompany().getLastName() != null ? convention.getCompany().getLastName() : ""));
                contentStream.endText();
                y -= lineSpacing;
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText("Email: " + (convention.getCompany().getEmail() != null ? convention.getCompany().getEmail() : "Non spécifié"));
                contentStream.endText();
                y -= lineSpacing;
            }
          
            if (convention.getCompanyAddress() != null && !convention.getCompanyAddress().isEmpty()) {
                contentStream.beginText();
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText("Adresse: " + convention.getCompanyAddress());
                contentStream.endText();
                y -= lineSpacing;
            }
            y -= sectionSpacing;

            // Signatures alignées (2 colonnes, superviseur à part)
            float signatureY = margin + 80;
            float colWidth = (PDRectangle.A4.getWidth() - 2 * margin) / 2;
            float col1X = margin;
            float col2X = margin + colWidth + 20;

            // Colonne 1 : Superviseur
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
            contentStream.newLineAtOffset(col1X, signatureY);
            contentStream.showText("Entreprise:");
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.newLineAtOffset(0, -lineSpacing);
            contentStream.showText("Nom: du signataire: _____________________ ");
            contentStream.newLineAtOffset(0, -lineSpacing);
            contentStream.showText("Fonction: _______________________________");
            contentStream.newLineAtOffset(0, -lineSpacing);
            contentStream.showText("Date: __________________________________");
            contentStream.newLineAtOffset(0, -lineSpacing);
            contentStream.showText("Signature: ______________________________");
            contentStream.endText();

            // Colonne 2 : Entreprise / Enseignant/Admin
            contentStream.beginText();
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 11);
            contentStream.newLineAtOffset(col2X, signatureY);
            contentStream.showText("Entreprise / Enseignant/Admin:");
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            contentStream.newLineAtOffset(0, -lineSpacing);
            contentStream.showText("Nom du signataire: ____________________");
            contentStream.newLineAtOffset(0, -lineSpacing);
            contentStream.showText("Fonction: _____________________________");
            contentStream.newLineAtOffset(0, -lineSpacing);
            contentStream.showText("Date: _________________________________");
            contentStream.newLineAtOffset(0, -lineSpacing);
            contentStream.showText("Signature: _____________________________");
            contentStream.endText();

            contentStream.close();
            document.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Erreur lors de la génération du PDF de convention", e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    public String generatePdf(Convention convention) {
        try {
            log.info("Début de la génération du PDF pour la convention ID: {}", convention.getId());
            
            // Générer le contenu PDF
            log.info("Génération du contenu PDF...");
            byte[] pdfContent = generateConventionPdf(convention);
            log.info("Contenu PDF généré, taille: {} bytes", pdfContent.length);
            
            // Sauvegarder dans MinIO avec un nom de fichier unique
            String fileName = "convention_" + convention.getId() + ".pdf";
            log.info("Tentative de sauvegarde dans MinIO avec le nom: {}", fileName);
            String pdfPath = conventionStorageService.storeFile(pdfContent, fileName, "application/pdf");
            
            log.info("PDF généré et sauvegardé pour la convention ID: {} sous le chemin: {}", convention.getId(), pdfPath);
            return pdfPath;
            
        } catch (Exception e) {
            log.error("Erreur lors de la génération et sauvegarde du PDF pour la convention ID: {}", convention.getId(), e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    public void deletePdf(String pdfPath) {
        if (pdfPath != null && !pdfPath.isEmpty()) {
            try {
                conventionStorageService.deleteFile(pdfPath);
                log.info("PDF supprimé avec succès: {}", pdfPath);
            } catch (Exception e) {
                log.error("Erreur lors de la suppression du PDF: {}", pdfPath, e);
                // Ne pas lever d'exception car la suppression peut échouer sans impacter le reste
            }
        }
    }

    public byte[] getLogoFile(String logoPath) {
        return conventionStorageService.getFile(logoPath); // Utilise déjà MinIO
    }

    private List<String> wrapText(String text, int maxLength) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            if (line.length() + word.length() > maxLength) {
                lines.add(line.toString());
                line = new StringBuilder();
            }
            line.append(word).append(" ");
        }
        if (!line.isEmpty()) lines.add(line.toString());
        return lines;
    }
}