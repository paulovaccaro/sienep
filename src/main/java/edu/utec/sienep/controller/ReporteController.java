package edu.utec.sienep.controller;

import edu.utec.sienep.service.PermisoService;
import edu.utec.sienep.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Generación de reportes PDF (permiso reportes.generar)")
public class ReporteController {

    private final ReporteService reporteService;
    private final PermisoService permisoService;

    @Operation(summary = "Reporte de estudiante (RF30/RF31)", description = "PDF A4 con datos personales, seguimientos, observaciones e informes finales")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF generado (Content-Type: application/pdf)"),
        @ApiResponse(responseCode = "400", description = "Estudiante no encontrado"),
        @ApiResponse(responseCode = "403", description = "Sin permiso reportes.generar")
    })
    @GetMapping("/estudiante/{idEstudiante}")
    public ResponseEntity<byte[]> reporteEstudiante(
            @PathVariable Integer idEstudiante,
            Authentication auth) {

        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "reportes.generar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        byte[] pdf = reporteService.generarReporteEstudiante(idEstudiante);
        return pdfResponse(pdf, "reporte-estudiante-" + idEstudiante + ".pdf");
    }

    @Operation(summary = "Reporte de grupo (RF30/RF31)", description = "PDF A4 horizontal con info del grupo y tabla de estudiantes con conteos")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF generado (Content-Type: application/pdf)"),
        @ApiResponse(responseCode = "400", description = "Grupo no encontrado"),
        @ApiResponse(responseCode = "403", description = "Sin permiso reportes.generar")
    })
    @GetMapping("/grupo/{idGrupo}")
    public ResponseEntity<byte[]> reporteGrupo(
            @PathVariable Integer idGrupo,
            Authentication auth) {

        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "reportes.generar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        byte[] pdf = reporteService.generarReporteGrupo(idGrupo);
        return pdfResponse(pdf, "reporte-grupo-" + idGrupo + ".pdf");
    }

    @Operation(summary = "Reporte de actividad por período (RF30/RF31)", description = "PDF A4 horizontal con instancias y recordatorios en el rango [fechaInicio, fechaFin]")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF generado (Content-Type: application/pdf)"),
        @ApiResponse(responseCode = "403", description = "Sin permiso reportes.generar")
    })
    @GetMapping("/actividad")
    public ResponseEntity<byte[]> reporteActividad(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Authentication auth) {

        Integer userId = (Integer) auth.getPrincipal();
        if (!permisoService.tienePermisoGlobal(userId, "reportes.generar"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        byte[] pdf = reporteService.generarReporteActividad(fechaInicio, fechaFin);
        return pdfResponse(pdf, "reporte-actividad.pdf");
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] pdf, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }
}
