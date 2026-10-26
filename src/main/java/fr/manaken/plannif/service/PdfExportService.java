package fr.manaken.plannif.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import fr.manaken.plannif.dto.PlanningDTO;
import fr.manaken.plannif.dto.SeanceDTO;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class PdfExportService {

    private static final Color HEADER_BG = new Color(30, 41, 59); // Slate 800
    private static final Color DAY_HEADER_BG = new Color(51, 65, 85); // Slate 700
    private static final Color TIME_COL_BG = new Color(241, 245, 249); // Slate 100
    private static final Color BORDER_COLOR = new Color(203, 213, 225); // Slate 300
    private static final Color CARD_BG = new Color(238, 242, 255); // Indigo 50
    private static final Color CARD_BORDER = new Color(129, 140, 248); // Indigo 400
    private static final Color LUNCH_BG = new Color(254, 243, 199); // Amber 100

    private static final int START_HOUR = 8;
    private static final int END_HOUR = 18; // 8h to 18h

    private final Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.WHITE);
    private final Font fontSubtitle = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(226, 232, 240));
    private final Font fontHeaderCol = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private final Font fontHour = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(71, 85, 105));
    private final Font fontSubject = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(15, 23, 42));
    private final Font fontDetails = FontFactory.getFont(FontFactory.HELVETICA, 7, new Color(51, 65, 85));
    private final Font fontRoom = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, new Color(2, 132, 199));
    private final Font fontFooter = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(100, 116, 139));

    private final PlanningService planningService;

    public PdfExportService(PlanningService planningService) {
        this.planningService = planningService;
    }

    /**
     * Génère un PDF A4 Paysage pour une seule classe.
     */
    public byte[] generatePdfForClasse(Long planningId, String classeNom) throws DocumentException {
        PlanningDTO planning = planningService.getPlanning(planningId);
        List<SeanceDTO> seancesClasse = planning.seances().stream()
                .filter(s -> classeNom.equalsIgnoreCase(s.classeNom()))
                .collect(Collectors.toList());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 25);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPageEvent(new FooterPageEvent());

        document.open();
        addSchedulePage(document, "CLASSE : " + classeNom.toUpperCase(), planning.nom(), seancesClasse, false);
        document.close();

        return baos.toByteArray();
    }

    /**
     * Génère un livret PDF complet de toutes les classes (1 page par classe).
     */
    public byte[] generateLivretClasses(Long planningId) throws DocumentException {
        PlanningDTO planning = planningService.getPlanning(planningId);
        List<String> classes = planning.seances().stream()
                .map(SeanceDTO::classeNom)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 25);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPageEvent(new FooterPageEvent());

        document.open();
        boolean first = true;
        for (String classe : classes) {
            if (!first) {
                document.newPage();
            }
            first = false;
            List<SeanceDTO> seances = planning.seances().stream()
                    .filter(s -> classe.equalsIgnoreCase(s.classeNom()))
                    .collect(Collectors.toList());
            addSchedulePage(document, "CLASSE : " + classe.toUpperCase(), planning.nom(), seances, false);
        }
        if (classes.isEmpty()) {
            document.add(new Paragraph("Aucune classe trouvée dans ce planning."));
        }
        document.close();

        return baos.toByteArray();
    }

    /**
     * Génère un PDF A4 Paysage pour un seul professeur.
     */
    public byte[] generatePdfForProfesseur(Long planningId, String profNom) throws DocumentException {
        PlanningDTO planning = planningService.getPlanning(planningId);
        List<SeanceDTO> seancesProf = planning.seances().stream()
                .filter(s -> profNom.equalsIgnoreCase(s.professeurNomComplet()))
                .collect(Collectors.toList());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 25);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPageEvent(new FooterPageEvent());

        document.open();
        addSchedulePage(document, "PROFESSEUR : " + profNom.toUpperCase(), planning.nom(), seancesProf, true);
        document.close();

        return baos.toByteArray();
    }

    /**
     * Génère un livret PDF complet de tous les professeurs (1 page par enseignant).
     */
    public byte[] generateLivretProfesseurs(Long planningId) throws DocumentException {
        PlanningDTO planning = planningService.getPlanning(planningId);
        List<String> profs = planning.seances().stream()
                .map(SeanceDTO::professeurNomComplet)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 25);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPageEvent(new FooterPageEvent());

        document.open();
        boolean first = true;
        for (String prof : profs) {
            if (!first) {
                document.newPage();
            }
            first = false;
            List<SeanceDTO> seances = planning.seances().stream()
                    .filter(s -> prof.equalsIgnoreCase(s.professeurNomComplet()))
                    .collect(Collectors.toList());
            addSchedulePage(document, "PROFESSEUR : " + prof.toUpperCase(), planning.nom(), seances, true);
        }
        if (profs.isEmpty()) {
            document.add(new Paragraph("Aucun professeur trouvé dans ce planning."));
        }
        document.close();

        return baos.toByteArray();
    }

    /**
     * Crée une archive ZIP complète contenant :
     * - livret_classes.pdf
     * - livret_professeurs.pdf
     * - classes/*.pdf (PDF individuel de chaque classe)
     * - professeurs/*.pdf (PDF individuel de chaque professeur)
     */
    public byte[] generateZipArchive(Long planningId) throws DocumentException, IOException {
        PlanningDTO planning = planningService.getPlanning(planningId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {

            // 1. Livret classes
            zos.putNextEntry(new ZipEntry("Livret_Classes_Complet.pdf"));
            zos.write(generateLivretClasses(planningId));
            zos.closeEntry();

            // 2. Livret profs
            zos.putNextEntry(new ZipEntry("Livret_Professeurs_Complet.pdf"));
            zos.write(generateLivretProfesseurs(planningId));
            zos.closeEntry();

            // 3. Fichiers individuels classes
            List<String> classes = planning.seances().stream()
                    .map(SeanceDTO::classeNom)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            for (String classe : classes) {
                String safeName = classe.replaceAll("[^a-zA-Z0-9-_]", "_");
                zos.putNextEntry(new ZipEntry("classes/Classe_" + safeName + ".pdf"));
                zos.write(generatePdfForClasse(planningId, classe));
                zos.closeEntry();
            }

            // 4. Fichiers individuels profs
            List<String> profs = planning.seances().stream()
                    .map(SeanceDTO::professeurNomComplet)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            for (String prof : profs) {
                String safeName = prof.replaceAll("[^a-zA-Z0-9-_]", "_");
                zos.putNextEntry(new ZipEntry("professeurs/Prof_" + safeName + ".pdf"));
                zos.write(generatePdfForProfesseur(planningId, prof));
                zos.closeEntry();
            }
        }

        return baos.toByteArray();
    }

    /**
     * Construit une page d'emploi du temps (titre + cartouche + grille hebdo Lundi->Vendredi).
     */
    private void addSchedulePage(Document document, String title, String planningName, List<SeanceDTO> seances, boolean isProfMode) throws DocumentException {
        // En-tête principal
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100f);
        headerTable.setWidths(new float[]{65f, 35f});
        headerTable.setSpacingAfter(8f);

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBackgroundColor(HEADER_BG);
        titleCell.setPadding(8f);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.addElement(new Paragraph(title, fontTitle));
        titleCell.addElement(new Paragraph("Planning : " + (planningName != null ? planningName : "Général"), fontSubtitle));
        headerTable.addCell(titleCell);

        PdfPCell metaCell = new PdfPCell();
        metaCell.setBackgroundColor(HEADER_BG);
        metaCell.setPadding(8f);
        metaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        metaCell.setBorder(Rectangle.NO_BORDER);
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        Paragraph metaP = new Paragraph("Année scolaire\nÉdité le " + dateStr, fontSubtitle);
        metaP.setAlignment(Element.ALIGN_RIGHT);
        metaCell.addElement(metaP);
        headerTable.addCell(metaCell);

        document.add(headerTable);

        // Grille hebdomadaire (Heure + 5 jours Lundi -> Vendredi)
        DayOfWeek[] days = new DayOfWeek[]{
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
        };
        String[] dayNames = new String[]{"LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI"};

        PdfPTable grid = new PdfPTable(6);
        grid.setWidthPercentage(100f);
        grid.setWidths(new float[]{10f, 18f, 18f, 18f, 18f, 18f});

        // Ligne des en-têtes de colonnes
        PdfPCell hTime = new PdfPCell(new Phrase("HORAIRE", fontHeaderCol));
        hTime.setBackgroundColor(DAY_HEADER_BG);
        hTime.setHorizontalAlignment(Element.ALIGN_CENTER);
        hTime.setVerticalAlignment(Element.ALIGN_MIDDLE);
        hTime.setPadding(5f);
        grid.addCell(hTime);

        for (String dayName : dayNames) {
            PdfPCell hDay = new PdfPCell(new Phrase(dayName, fontHeaderCol));
            hDay.setBackgroundColor(DAY_HEADER_BG);
            hDay.setHorizontalAlignment(Element.ALIGN_CENTER);
            hDay.setVerticalAlignment(Element.ALIGN_MIDDLE);
            hDay.setPadding(5f);
            grid.addCell(hDay);
        }

        // Lignes par heure : 8h -> 18h (10 créneaux d'une heure)
        for (int h = START_HOUR; h < END_HOUR; h++) {
            LocalTime slotStart = LocalTime.of(h, 0);
            LocalTime slotEnd = LocalTime.of(h + 1, 0);

            // Cellule d'heure
            PdfPCell timeCell = new PdfPCell(new Phrase(String.format("%02dh-%02dh", h, h + 1), fontHour));
            timeCell.setBackgroundColor(h == 12 ? LUNCH_BG : TIME_COL_BG);
            timeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            timeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            timeCell.setBorderColor(BORDER_COLOR);
            timeCell.setPadding(3f);
            grid.addCell(timeCell);

            // Pour chaque jour
            for (DayOfWeek day : days) {
                // Trouver les séances qui couvrent cette tranche horaire [h, h+1) ce jour-là
                List<SeanceDTO> matching = seances.stream()
                        .filter(s -> s.debut() != null && s.fin() != null)
                        .filter(s -> s.debut().getDayOfWeek() == day)
                        .filter(s -> {
                            LocalTime sStart = s.debut().toLocalTime();
                            LocalTime sEnd = s.fin().toLocalTime();
                            return !sEnd.isBefore(slotEnd) && !sStart.isAfter(slotStart)
                                    || (sStart.isBefore(slotEnd) && sEnd.isAfter(slotStart));
                        })
                        .collect(Collectors.toList());

                PdfPCell dayCell = new PdfPCell();
                dayCell.setBorderColor(BORDER_COLOR);
                dayCell.setPadding(2f);

                if (matching.isEmpty()) {
                    if (h == 12) {
                        dayCell.setBackgroundColor(new Color(254, 249, 195)); // Léger beige pause déjeuner
                    } else {
                        dayCell.setBackgroundColor(Color.WHITE);
                    }
                    dayCell.addElement(new Phrase(" ", fontDetails));
                } else {
                    dayCell.setBackgroundColor(CARD_BG);
                    dayCell.setBorderColor(CARD_BORDER);

                    for (SeanceDTO s : matching) {
                        Paragraph pMatiere = new Paragraph(s.matiereNom() != null ? s.matiereNom() : "Matière", fontSubject);
                        dayCell.addElement(pMatiere);

                        String targetInfo = isProfMode
                                ? (s.classeNom() != null ? "Classe : " + s.classeNom() : "")
                                : (s.professeurNomComplet() != null ? s.professeurNomComplet() : "");
                        if (!targetInfo.isEmpty()) {
                            dayCell.addElement(new Paragraph(targetInfo, fontDetails));
                        }

                        if (s.salleCode() != null && !s.salleCode().isBlank()) {
                            dayCell.addElement(new Paragraph("Salle " + s.salleCode(), fontRoom));
                        }

                        if (s.groupe() != null && !s.groupe().isBlank()) {
                            dayCell.addElement(new Paragraph("Gr: " + s.groupe(), fontDetails));
                        }
                    }
                }
                grid.addCell(dayCell);
            }
        }

        document.add(grid);
    }

    /**
     * Gestionnaire d'événements pour le pied de page OpenPDF ("Page X sur Y").
     */
    private class FooterPageEvent extends PdfPageEventHelper {
        private PdfTemplate total;

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            total = writer.getDirectContent().createTemplate(30, 16);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable footer = new PdfPTable(3);
            try {
                footer.setWidths(new float[]{40f, 40f, 20f});
                footer.setTotalWidth(document.right() - document.left());
                footer.getDefaultCell().setBorder(Rectangle.NO_BORDER);

                // Date & signature logicielle
                PdfPCell c1 = new PdfPCell(new Phrase("Plannif Lycée - Document officiel d'emploi du temps", fontFooter));
                c1.setBorder(Rectangle.NO_BORDER);
                footer.addCell(c1);

                PdfPCell c2 = new PdfPCell(new Phrase("", fontFooter));
                c2.setBorder(Rectangle.NO_BORDER);
                footer.addCell(c2);

                // Numérotation
                String pageText = String.format("Page %d / ", writer.getPageNumber());
                PdfPCell c3 = new PdfPCell(new Phrase(pageText, fontFooter));
                c3.setHorizontalAlignment(Element.ALIGN_RIGHT);
                c3.setBorder(Rectangle.NO_BORDER);
                footer.addCell(c3);

                footer.writeSelectedRows(0, -1, document.left(), document.bottom() - 5, writer.getDirectContent());

                // Emplacement pour le nombre total de pages
                PdfContentByte cb = writer.getDirectContent();
                cb.addTemplate(total, document.right() - 8, document.bottom() - 4);
            } catch (Exception ignored) {
            }
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(total, Element.ALIGN_LEFT,
                    new Phrase(String.valueOf(writer.getPageNumber() - 1), fontFooter),
                    2, 2, 0);
        }
    }
}
