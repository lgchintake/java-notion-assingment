package com.home.practice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.home.practice.components.StudentApiClient;
import com.home.practice.service.StudentReportPdfGenerator;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/students")
public class StudentReportController {

    private final StudentApiClient studentApiClient;
    private final StudentReportPdfGenerator pdfGenerator;

    /**
     * Constructs a StudentReportController with required dependencies.
     *
     * @param studentApiClient the client for fetching student data from the Node API
     * @param pdfGenerator the service for generating PDF reports
     */
    public StudentReportController(StudentApiClient studentApiClient, StudentReportPdfGenerator pdfGenerator) {
        this.studentApiClient = studentApiClient;
        this.pdfGenerator = pdfGenerator;
    }

    /**
     * REST endpoint that generates and returns a PDF report for a student.
     * Requires authentication headers (Access-Token, Refresh-Token, Csrf-Token) to be provided.
     * The report is returned as an attachment for download.
     *
     * @param id the unique identifier of the student
     * @param accessToken the access token for authenticating the request
     * @param refreshToken the refresh token for token renewal
     * @param csrfToken the CSRF token for security validation
     * @return a ResponseEntity containing the PDF file as bytes with appropriate headers
     */
    @GetMapping("/{id}/report")
    public ResponseEntity<byte[]> getStudentReport(
            @PathVariable long id,
            @RequestHeader("Access-Token") String accessToken,
            @RequestHeader("Refresh-Token") String refreshToken,
            @RequestHeader("Csrf-Token") String csrfToken) {
        JsonNode student = studentApiClient.getStudentById(id, accessToken,refreshToken, csrfToken);
        byte[] pdf = pdfGenerator.generate(student);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename("student-" + id + "-report.pdf")
                                .build()
                                .toString()
                )
                .body(pdf);
    }
}
