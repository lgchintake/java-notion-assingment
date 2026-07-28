package com.home.practice.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class StudentReportPdfGenerator {

    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 842;
    private static final int LEFT_MARGIN = 54;
    private static final int TOP_MARGIN = 790;
    private static final int LINE_HEIGHT = 18;

    /**
     * Generates a PDF report from student data.
     * Constructs the report content, formats it as a PDF stream, and returns the binary PDF data.
     *
     * @param student a JsonNode containing the student data to include in the report
     * @return a byte array containing the complete PDF document
     */
    public byte[] generate(JsonNode student) {
        List<PdfLine> lines = buildLines(student);
        String content = buildContentStream(lines);
        return writePdf(content);
    }

    /**
     * Builds a list of PdfLine objects from student data.
     * Organizes the data into sections including Basic Information, Academic Information,
     * Parent and Guardian Information, and Address Information.
     *
     * @param student a JsonNode containing the student data
     * @return a List of PdfLine objects representing the formatted report content
     */
    private List<PdfLine> buildLines(JsonNode student) {
        List<PdfLine> lines = new ArrayList<>();
        lines.add(new PdfLine("Student Report", 20, true));
        lines.add(new PdfLine("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), 10, false));
        lines.add(PdfLine.blank());

        addSection(lines, "Basic Information");
        addField(lines, "Student ID", student, "id");
        addField(lines, "Name", student, "name");
        addField(lines, "Email", student, "email");
        addField(lines, "Phone", student, "phone");
        addField(lines, "Gender", student, "gender");
        addField(lines, "Date of Birth", student, "dob");
        addField(lines, "System Access", student, "systemAccess");
        addField(lines, "Reporter", student, "reporterName");

        addSection(lines, "Academic Information");
        addField(lines, "Class", student, "class");
        addField(lines, "Section", student, "section");
        addField(lines, "Roll", student, "roll");
        addField(lines, "Admission Date", student, "admissionDate");

        addSection(lines, "Parent and Guardian Information");
        addField(lines, "Father Name", student, "fatherName");
        addField(lines, "Father Phone", student, "fatherPhone");
        addField(lines, "Mother Name", student, "motherName");
        addField(lines, "Mother Phone", student, "motherPhone");
        addField(lines, "Guardian Name", student, "guardianName");
        addField(lines, "Guardian Phone", student, "guardianPhone");
        addField(lines, "Relation of Guardian", student, "relationOfGuardian");

        addSection(lines, "Address Information");
        addField(lines, "Current Address", student, "currentAddress");
        addField(lines, "Permanent Address", student, "permanentAddress");

        return lines;
    }

    /**
     * Adds a section header to the report.
     * Creates a blank line followed by a bold section title.
     *
     * @param lines the list of PdfLine objects to add the section to
     * @param title the title of the section to add
     */
    private void addSection(List<PdfLine> lines, String title) {
        lines.add(PdfLine.blank());
        lines.add(new PdfLine(title, 14, true));
    }

    /**
     * Adds a field label and value to the report.
     * Wraps long text to fit within the page width.
     *
     * @param lines the list of PdfLine objects to add the field to
     * @param label the field label to display
     * @param student the JsonNode containing the student data
     * @param fieldName the name of the field in the JSON object to retrieve the value from
     */
    private void addField(List<PdfLine> lines, String label, JsonNode student, String fieldName) {
        String value = getValue(student, fieldName);
        String line = label + ": " + value;
        for (String wrappedLine : wrap(line, 88)) {
            lines.add(new PdfLine(wrappedLine, 11, false));
        }
    }

    /**
     * Retrieves the value of a field from the student JSON object.
     * Handles null values, booleans, and text formatting.
     *
     * @param student the JsonNode containing the student data
     * @param fieldName the name of the field to retrieve
     * @return the formatted field value, or "N/A" if the field is null or blank
     */
    private String getValue(JsonNode student, String fieldName) {
        JsonNode value = student.get(fieldName);
        if (value == null || value.isNull()) {
            return "N/A";
        }
        if (value.isBoolean()) {
            return value.asBoolean() ? "Enabled" : "Disabled";
        }
        String text = value.asText();
        return text == null || text.isBlank() ? "N/A" : text;
    }

    /**
     * Wraps text to fit within a maximum line length.
     * Splits text at word boundaries when possible.
     *
     * @param text the text to wrap
     * @param maxLength the maximum length of each wrapped line
     * @return a List of wrapped text strings
     */
    private List<String> wrap(String text, int maxLength) {
        List<String> wrapped = new ArrayList<>();
        String remaining = text;
        while (remaining.length() > maxLength) {
            int splitAt = remaining.lastIndexOf(' ', maxLength);
            if (splitAt <= 0) {
                splitAt = maxLength;
            }
            wrapped.add(remaining.substring(0, splitAt).trim());
            remaining = remaining.substring(splitAt).trim();
        }
        wrapped.add(remaining);
        return wrapped;
    }

    /**
     * Builds the PDF content stream from a list of PdfLine objects.
     * Converts the lines into PDF text positioning and rendering commands.
     *
     * @param lines the list of PdfLine objects to render
     * @return a String containing the PDF content stream commands
     */
    private String buildContentStream(List<PdfLine> lines) {
        StringBuilder content = new StringBuilder();
        int y = TOP_MARGIN;

        for (PdfLine line : lines) {
            if (y < 54) {
                break;
            }
            if (line.isBlank()) {
                y -= LINE_HEIGHT / 2;
                continue;
            }
            content.append("BT\n")
                    .append(line.bold() ? "/F2 " : "/F1 ")
                    .append(line.fontSize())
                    .append(" Tf\n")
                    .append(LEFT_MARGIN)
                    .append(" ")
                    .append(y)
                    .append(" Td\n")
                    .append("(")
                    .append(escapePdfText(line.text()))
                    .append(") Tj\n")
                    .append("ET\n");
            y -= line.fontSize() >= 14 ? 23 : LINE_HEIGHT;
        }

        return content.toString();
    }

    /**
     * Escapes special characters in text for PDF output.
     * Handles backslashes, parentheses, and line breaks.
     *
     * @param text the text to escape
     * @return the escaped text safe for PDF encoding
     */
    private String escapePdfText(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("\r", " ")
                .replace("\n", " ");
    }

    /**
     * Constructs the complete PDF binary from the content stream.
     * Creates PDF objects and cross-reference table for a valid PDF document.
     *
     * @param content the PDF content stream string
     * @return a byte array containing the complete PDF document
     */
    private byte[] writePdf(String content) {
        List<String> objects = List.of(
                "<< /Type /Catalog /Pages 2 0 R >>",
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + PAGE_WIDTH + " " + PAGE_HEIGHT + "] /Resources << /Font << /F1 4 0 R /F2 5 0 R >> >> /Contents 6 0 R >>",
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>",
                "<< /Length " + content.getBytes(StandardCharsets.UTF_8).length + " >>\nstream\n" + content + "endstream"
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out, "%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);

        for (int i = 0; i < objects.size(); i++) {
            offsets.add(out.size());
            write(out, (i + 1) + " 0 obj\n");
            write(out, objects.get(i));
            write(out, "\nendobj\n");
        }

        int xrefOffset = out.size();
        write(out, "xref\n");
        write(out, "0 " + (objects.size() + 1) + "\n");
        write(out, "0000000000 65535 f \n");
        for (int i = 1; i < offsets.size(); i++) {
            write(out, String.format("%010d 00000 n \n", offsets.get(i)));
        }
        write(out, "trailer\n");
        write(out, "<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\n");
        write(out, "startxref\n");
        write(out, xrefOffset + "\n");
        write(out, "%%EOF\n");
        return out.toByteArray();
    }

    /**
     * Writes a string to the ByteArrayOutputStream using UTF-8 encoding.
     *
     * @param out the ByteArrayOutputStream to write to
     * @param value the string value to write
     */
    private void write(ByteArrayOutputStream out, String value) {
        out.writeBytes(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Represents a single line in the PDF document with text, font size, and formatting.
     *
     * @param text the text content of the line
     * @param fontSize the font size in points
     * @param bold whether the text should be rendered in bold
     */
    private record PdfLine(String text, int fontSize, boolean bold) {
        /**
         * Creates a blank PdfLine with no text.
         *
         * @return a blank PdfLine object
         */
        static PdfLine blank() {
            return new PdfLine("", 0, false);
        }

        /**
         * Checks if this PdfLine is blank.
         *
         * @return true if the text is empty or whitespace-only, false otherwise
         */
        boolean isBlank() {
            return text.isBlank();
        }
    }
}
