package com._projects.internship.service.core;

import com._projects.internship.model.core.ConventionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Service pour générer des documents PDF de conventions de stage
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConventionPdfGenerationService {

    /**
     * Génère un document PDF pour une convention de stage
     * @param convention L'entité convention
     * @return Le contenu du PDF sous forme de tableau d'octets
     * @throws RuntimeException si une erreur survient lors de la génération du PDF
     */
    public byte[] generateConventionPdf(ConventionEntity convention) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Titre
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 18);
                contentStream.newLineAtOffset(100, 750);
                contentStream.showText("CONVENTION DE STAGE");
                contentStream.endText();
 
                // Date
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(450, 750);
                contentStream.showText("Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                contentStream.endText();
 
                // Informations de la convention
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText("Référence: CONV-" + convention.getId());
                contentStream.newLineAtOffset(0, -20);
                contentStream.endText(); // Added missing endText()

                // Parties concernées
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.newLineAtOffset(50, 620);
                contentStream.showText("PARTIES CONCERNÉES");
                contentStream.endText();
 
                // Étudiant
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.newLineAtOffset(50, 590);
                contentStream.showText("Étudiant:");
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("ID: " + convention.getStudentId());
                contentStream.endText();
 
                // Entreprise
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.newLineAtOffset(50, 540);
                contentStream.showText("Entreprise:");
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("ID: " + convention.getCompanyId());
                contentStream.endText();
 
                // Enseignant
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.newLineAtOffset(50, 490);
                contentStream.showText("Enseignant référent:");
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("ID: " + convention.getTeacherId());
                contentStream.endText();
 
                // Signatures
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                contentStream.newLineAtOffset(50, 400);
                contentStream.showText("SIGNATURES");
                contentStream.endText();
 
                // Lignes de signature
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 370);
                contentStream.showText("Entreprise: ___________________");
                contentStream.newLineAtOffset(0, -40);
                contentStream.showText("Enseignant référent: ___________________");
                contentStream.endText();
 
                // Pied de page
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.newLineAtOffset(50, 50);
                contentStream.showText("Ce document est généré automatiquement et ne nécessite pas de signature manuscrite.");
                contentStream.endText();
            }
            
            document.save(baos);
            return baos.toByteArray();
            
        } catch (IOException e) {
            log.error("Erreur lors de la génération du PDF de convention", e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }
}