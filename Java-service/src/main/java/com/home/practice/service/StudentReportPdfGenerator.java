package com.home.practice.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class StudentReportPdfGenerator {

    private static final float MARGIN = 50f;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;
    private static final float LABEL_WIDTH = 150f;
    private static final float ROW_HEIGHT = 22f;
    private static final float SECTION_HEADER_HEIGHT = 26f;

    private static final Color HEADER_COLOR = new Color(30, 41, 59);    // slate-800
    private static final Color SECTION_BG = new Color(30, 41, 59);
    private static final Color ROW_ALT_BG = new Color(241, 245, 249);   // slate-100
    private static final Color TEXT_COLOR = new Color(15, 23, 42);      // slate-900
    private static final Color MUTED_TEXT = new Color(100, 116, 139);   // slate-500
    private static final Color BORDER_COLOR = new Color(203, 213, 225); // slate-300

    private static final PDFont FONT_REGULAR = PDType1Font.HELVETICA;
    private static final PDFont FONT_BOLD = PDType1Font.HELVETICA_BOLD;

    public byte[] generate(JsonNode student) {
        try (PDDocument document = new PDDocument()) {
            ReportContext ctx = new ReportContext(document);
            ctx.newPage();

            drawTitleBlock(ctx, student);
            drawSection(ctx, "Basic Information", basicFields(student));
            drawSection(ctx, "Academic Information", academicFields(student));
            drawSection(ctx, "Parent and Guardian Information", parentFields(student));
            drawSection(ctx, "Address Information", addressFields(student));

            ctx.close();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate student report PDF", e);
        }
    }

    // ---------- Field builders ----------

    private List<Field> basicFields(JsonNode student) {
        List<Field> fields = new ArrayList<>();
        fields.add(field("Student ID", student, "id"));
        fields.add(field("Name", student, "name"));
        fields.add(field("Email", student, "email"));
        fields.add(field("Phone", student, "phone"));
        fields.add(field("Gender", student, "gender"));
        fields.add(field("Date of Birth", student, "dob"));
        fields.add(field("System Access", student, "systemAccess"));
        fields.add(field("Reporter", student, "reporterName"));
        return fields;
    }

    private List<Field> academicFields(JsonNode student) {
        List<Field> fields = new ArrayList<>();
        fields.add(field("Class", student, "class"));
        fields.add(field("Section", student, "section"));
        fields.add(field("Roll", student, "roll"));
        fields.add(field("Admission Date", student, "admissionDate"));
        return fields;
    }

    private List<Field> parentFields(JsonNode student) {
        List<Field> fields = new ArrayList<>();
        fields.add(field("Father Name", student, "fatherName"));
        fields.add(field("Father Phone", student, "fatherPhone"));
        fields.add(field("Mother Name", student, "motherName"));
        fields.add(field("Mother Phone", student, "motherPhone"));
        fields.add(field("Guardian Name", student, "guardianName"));
        fields.add(field("Guardian Phone", student, "guardianPhone"));
        fields.add(field("Relation of Guardian", student, "relationOfGuardian"));
        return fields;
    }

    private List<Field> addressFields(JsonNode student) {
        List<Field> fields = new ArrayList<>();
        fields.add(field("Current Address", student, "currentAddress"));
        fields.add(field("Permanent Address", student, "permanentAddress"));
        return fields;
    }

    private Field field(String label, JsonNode student, String fieldName) {
        return new Field(label, getValue(student, fieldName));
    }

    private String getValue(JsonNode student, String fieldName) {
        JsonNode value = student.get(fieldName);
        if (value == null || value.isNull()) {
            return "N/A";
        }
        if (value.isBoolean()) {
            return value.asBoolean() ? "Enabled" : "Disabled";
        }
        if(fieldName.equalsIgnoreCase("dob")|| fieldName.equalsIgnoreCase("admissionDate")){
            Instant instant = Instant.parse(value.asText());
            return DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH)
                    .format(instant.atZone(ZoneOffset.UTC));
        }
        String text = value.asText();
        return text == null || text.isBlank() ? "N/A" : text;
    }

    // ---------- Drawing ----------

    private void drawTitleBlock(ReportContext ctx, JsonNode student) throws IOException {
        PDPageContentStream cs = ctx.contentStream;
        String studentName = getValue(student, "name");

        cs.setNonStrokingColor(HEADER_COLOR);
        cs.addRect(MARGIN, ctx.y - 50, CONTENT_WIDTH, 50);
        cs.fill();

        drawText(cs, FONT_BOLD, 18, Color.WHITE, MARGIN + 14, ctx.y - 22, "Student Report");
        drawText(cs, FONT_REGULAR, 10, new Color(203, 213, 225), MARGIN + 14, ctx.y - 38,
                "Student: " + studentName);

        String generated = "Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
        float genWidth = FONT_REGULAR.getStringWidth(generated) / 1000 * 9;
        drawText(cs, FONT_REGULAR, 9, new Color(203, 213, 225),
                MARGIN + CONTENT_WIDTH - genWidth - 14, ctx.y - 22, generated);

        ctx.y -= 50 + 20;
    }

    private void drawSection(ReportContext ctx, String title, List<Field> fields) throws IOException {
        ctx.ensureSpace(SECTION_HEADER_HEIGHT + ROW_HEIGHT);

        PDPageContentStream cs = ctx.contentStream;
        cs.setNonStrokingColor(SECTION_BG);
        cs.addRect(MARGIN, ctx.y - SECTION_HEADER_HEIGHT, CONTENT_WIDTH, SECTION_HEADER_HEIGHT);
        cs.fill();
        drawText(cs, FONT_BOLD, 12, Color.WHITE, MARGIN + 10, ctx.y - 18, title);
        ctx.y -= SECTION_HEADER_HEIGHT;

        boolean alt = false;
        for (Field f : fields) {
            List<String> wrappedValue = wrap(f.value(), FONT_REGULAR, 10, CONTENT_WIDTH - LABEL_WIDTH - 20);
            float rowHeight = Math.max(ROW_HEIGHT, wrappedValue.size() * 13 + 8);

            ctx.ensureSpace(rowHeight);
            cs = ctx.contentStream;

            if (alt) {
                cs.setNonStrokingColor(ROW_ALT_BG);
                cs.addRect(MARGIN, ctx.y - rowHeight, CONTENT_WIDTH, rowHeight);
                cs.fill();
            }
            alt = !alt;

            drawText(cs, FONT_BOLD, 10, TEXT_COLOR, MARGIN + 10, ctx.y - 15, f.label());

            float lineY = ctx.y - 15;
            for (String line : wrappedValue) {
                drawText(cs, FONT_REGULAR, 10, TEXT_COLOR, MARGIN + LABEL_WIDTH, lineY, line);
                lineY -= 13;
            }

            cs.setStrokingColor(BORDER_COLOR);
            cs.setLineWidth(0.5f);
            cs.moveTo(MARGIN, ctx.y - rowHeight);
            cs.lineTo(MARGIN + CONTENT_WIDTH, ctx.y - rowHeight);
            cs.stroke();

            ctx.y -= rowHeight;
        }

        ctx.y -= 16;
    }

    private void drawText(PDPageContentStream cs, PDFont font, float size, Color color,
                          float x, float y, String text) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.setNonStrokingColor(color);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    private List<String> wrap(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\n")) {
            StringBuilder current = new StringBuilder();
            for (String word : paragraph.split(" ")) {
                String candidate = current.isEmpty() ? word : current + " " + word;
                float width = font.getStringWidth(candidate) / 1000 * fontSize;
                if (width > maxWidth && !current.isEmpty()) {
                    lines.add(current.toString());
                    current = new StringBuilder(word);
                } else {
                    current = new StringBuilder(candidate);
                }
            }
            if (!current.isEmpty()) {
                lines.add(current.toString());
            }
        }
        return lines.isEmpty() ? List.of("") : lines;
    }

    // ---------- Page/context management ----------

    private record Field(String label, String value) {}

    private class ReportContext {
        final PDDocument document;
        PDPageContentStream contentStream;
        float y;
        int pageNumber = 0;

        ReportContext(PDDocument document) {
            this.document = document;
        }

        void newPage() throws IOException {
            if (contentStream != null) {
                drawFooter();
                contentStream.close();
            }
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page);
            y = PAGE_HEIGHT - MARGIN;
            pageNumber++;
        }

        void ensureSpace(float needed) throws IOException {
            if (y - needed < MARGIN + 20) {
                newPage();
            }
        }

        void drawFooter() throws IOException {
            String footer = "Page " + pageNumber;
            float width = FONT_REGULAR.getStringWidth(footer) / 1000 * 8;
            drawText(contentStream, FONT_REGULAR, 8, MUTED_TEXT,
                    (PAGE_WIDTH - width) / 2, MARGIN - 20, footer);
        }

        void close() throws IOException {
            if (contentStream != null) {
                drawFooter();
                contentStream.close();
            }
        }
    }
}