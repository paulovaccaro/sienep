package edu.utec.sienep.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import edu.utec.sienep.entity.*;
import edu.utec.sienep.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final EstudianteRepository estudianteRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final ObservacionRepository observacionRepository;
    private final InstanciaRepository instanciaRepository;
    private final RecordatorioRepository recordatorioRepository;
    private final GrupoRepository grupoRepository;

    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_DT   = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Color HEADER_BG  = new Color(41, 128, 185);
    private static final Color SECTION_BG = new Color(236, 240, 241);
    private static final Color ALT_ROW    = new Color(250, 250, 250);

    private Font titleFont()   { return FontFactory.getFont(FontFactory.HELVETICA_BOLD,  16, Color.BLACK); }
    private Font sectionFont() { return FontFactory.getFont(FontFactory.HELVETICA_BOLD,  11, new Color(41, 128, 185)); }
    private Font labelFont()   { return FontFactory.getFont(FontFactory.HELVETICA_BOLD,   9, Color.BLACK); }
    private Font headerFont()  { return FontFactory.getFont(FontFactory.HELVETICA_BOLD,   9, Color.WHITE); }
    private Font dataFont()    { return FontFactory.getFont(FontFactory.HELVETICA,         9, Color.BLACK); }
    private Font infoFont()    { return FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);  }

    // ─── RF30 Reporte Estudiante ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public byte[] generarReporteEstudiante(Integer idEstudiante) {
        Estudiante est = estudianteRepository.findById(idEstudiante)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado: " + idEstudiante));

        List<Seguimiento> seguimientos = seguimientoRepository.findByEstudianteIdWithDetails(idEstudiante);
        List<Observacion> observaciones = observacionRepository.findByEstudianteIdWithDetails(idEstudiante);

        Document doc = new Document(PageSize.A4, 40, 40, 60, 40);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(doc, baos);
            doc.open();
            addCabecera(doc, "Reporte de Estudiante",
                    est.getUsuario().getNombre() + " " + est.getUsuario().getApellido());

            // Datos del estudiante
            doc.add(seccionTitulo("Datos del Estudiante"));
            PdfPTable info = infoTable(2);
            addInfoRow(info, "Nombre",   est.getUsuario().getNombre() + " " + est.getUsuario().getApellido());
            addInfoRow(info, "CI",       est.getUsuario().getCedula());
            addInfoRow(info, "Correo",   est.getUsuario().getCorreo());
            addInfoRow(info, "Grupo",    est.getGrupo().getNomGrupo());
            addInfoRow(info, "Carrera",  est.getGrupo().getCarrera().getNombre());
            addInfoRow(info, "ITR",      est.getGrupo().getItr().getNombre());
            addInfoRow(info, "Anio/Sem", est.getGrupo().getAnio() + " / " + est.getGrupo().getSemestre());
            addInfoRow(info, "Estado",   est.getEstActivo() ? "Activo" : "Inactivo");
            doc.add(info);
            doc.add(new Paragraph(" "));

            // Seguimientos
            doc.add(seccionTitulo("Seguimientos (" + seguimientos.size() + ")"));
            PdfPTable segT = dataTable(new String[]{"#", "Fecha Inicio", "Fecha Cierre", "Estado"}, new float[]{1, 3, 3, 2});
            int row = 0;
            for (Seguimiento s : seguimientos) {
                boolean alt = (row++ % 2 == 1);
                addDataRow(segT, alt,
                        s.getIdSeguimiento().toString(),
                        fmt(s.getFecInicio()),
                        s.getFecCierre() != null ? fmt(s.getFecCierre()) : "-",
                        s.getEstActivo() ? "Activo" : "Cerrado");
            }
            if (seguimientos.isEmpty()) addEmptyRow(segT, 4);
            doc.add(segT);
            doc.add(new Paragraph(" "));

            // Observaciones
            doc.add(seccionTitulo("Observaciones (" + observaciones.size() + ")"));
            PdfPTable obsT = dataTable(new String[]{"#", "Fecha", "Funcionario", "Titulo", "Contenido"}, new float[]{1, 3, 4, 4, 8});
            row = 0;
            for (Observacion o : observaciones) {
                boolean alt = (row++ % 2 == 1);
                String func = o.getFuncionario().getUsuario().getNombre()
                            + " " + o.getFuncionario().getUsuario().getApellido();
                String contenido = truncar(o.getContenido(), 120);
                addDataRow(obsT, alt,
                        o.getIdObservacion().toString(),
                        fmt(o.getFecHora()),
                        func,
                        o.getTitulo(),
                        contenido);
            }
            if (observaciones.isEmpty()) addEmptyRow(obsT, 5);
            doc.add(obsT);
            doc.add(new Paragraph(" "));

            // Informes finales (via seguimientos)
            List<InformeFinal> informes = seguimientos.stream()
                    .map(Seguimiento::getInforme)
                    .filter(i -> i != null)
                    .distinct().toList();
            doc.add(seccionTitulo("Informes Finales (" + informes.size() + ")"));
            PdfPTable infT = dataTable(new String[]{"#", "Fecha", "Valoracion", "Contenido"}, new float[]{1, 3, 2, 14});
            row = 0;
            for (InformeFinal i : informes) {
                boolean alt = (row++ % 2 == 1);
                addDataRow(infT, alt,
                        i.getIdInfFinal().toString(),
                        fmt(i.getFecCreacion()),
                        i.getValoracion() + "/10",
                        truncar(i.getContenido(), 200));
            }
            if (informes.isEmpty()) addEmptyRow(infT, 4);
            doc.add(infT);

            doc.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generando PDF de estudiante", e);
        }
        return baos.toByteArray();
    }

    // ─── RF30 Reporte Grupo ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public byte[] generarReporteGrupo(Integer idGrupo) {
        Grupo grupo = grupoRepository.findById(idGrupo)
                .orElseThrow(() -> new IllegalArgumentException("Grupo no encontrado: " + idGrupo));

        List<Estudiante> estudiantes = estudianteRepository.findByGruposIn(List.of(idGrupo));

        Document doc = new Document(PageSize.A4.rotate(), 40, 40, 60, 40);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(doc, baos);
            doc.open();
            addCabecera(doc, "Reporte de Grupo", grupo.getNomGrupo());

            // Info del grupo
            doc.add(seccionTitulo("Informacion del Grupo"));
            PdfPTable info = infoTable(2);
            addInfoRow(info, "Grupo",    grupo.getNomGrupo());
            addInfoRow(info, "Carrera",  grupo.getCarrera().getNombre());
            addInfoRow(info, "ITR",      grupo.getItr().getNombre());
            addInfoRow(info, "Anio",     grupo.getAnio().toString());
            addInfoRow(info, "Semestre", grupo.getSemestre().toString());
            addInfoRow(info, "Estado",   grupo.getEstActivo() ? "Activo" : "Inactivo");
            doc.add(info);
            doc.add(new Paragraph(" "));

            // Estudiantes
            doc.add(seccionTitulo("Estudiantes (" + estudiantes.size() + ")"));
            PdfPTable estT = dataTable(
                    new String[]{"#", "Nombre", "CI", "Correo", "Seguimientos", "Observaciones", "Estado"},
                    new float[]{1, 6, 3, 6, 3, 3, 2});
            int row = 0;
            for (Estudiante e : estudiantes) {
                boolean alt = (row++ % 2 == 1);
                long seg = seguimientoRepository.countByEstudiante_IdUsuario(e.getIdUsuario());
                long obs = observacionRepository.countByEstudiante_IdUsuario(e.getIdUsuario());
                addDataRow(estT, alt,
                        String.valueOf(row),
                        e.getUsuario().getNombre() + " " + e.getUsuario().getApellido(),
                        e.getUsuario().getCedula(),
                        e.getUsuario().getCorreo(),
                        String.valueOf(seg),
                        String.valueOf(obs),
                        e.getEstActivo() ? "Activo" : "Inactivo");
            }
            if (estudiantes.isEmpty()) addEmptyRow(estT, 7);
            doc.add(estT);

            doc.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generando PDF de grupo", e);
        }
        return baos.toByteArray();
    }

    // ─── RF30 Reporte Actividad por Período ──────────────────────────────────

    @Transactional(readOnly = true)
    public byte[] generarReporteActividad(LocalDate fechaInicio, LocalDate fechaFin) {
        OffsetDateTime ini = fechaInicio.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
        OffsetDateTime fin = fechaFin.plusDays(1).atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();

        List<Instancia> instancias = instanciaRepository.findByPeriodo(ini, fin);
        List<Recordatorio> recordatorios = recordatorioRepository.findByPeriodo(ini, fin);

        Document doc = new Document(PageSize.A4.rotate(), 40, 40, 60, 40);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(doc, baos);
            doc.open();
            addCabecera(doc, "Reporte de Actividad",
                    "Periodo: " + fmt(fechaInicio) + " - " + fmt(fechaFin));

            // Instancias
            doc.add(seccionTitulo("Instancias en el Periodo (" + instancias.size() + ")"));
            PdfPTable insT = dataTable(
                    new String[]{"#", "Titulo", "Tipo", "Fecha/Hora", "Funcionario", "Estado"},
                    new float[]{1, 6, 4, 4, 6, 2});
            int row = 0;
            for (Instancia i : instancias) {
                boolean alt = (row++ % 2 == 1);
                String func = i.getFuncionario().getUsuario().getNombre()
                            + " " + i.getFuncionario().getUsuario().getApellido();
                addDataRow(insT, alt,
                        String.valueOf(row),
                        i.getTitulo(),
                        i.getTipo(),
                        fmt(i.getFecHora()),
                        func,
                        i.getEstActivo() ? "Activa" : "Inactiva");
            }
            if (instancias.isEmpty()) addEmptyRow(insT, 6);
            doc.add(insT);
            doc.add(new Paragraph(" "));

            // Recordatorios
            doc.add(seccionTitulo("Recordatorios en el Periodo (" + recordatorios.size() + ")"));
            PdfPTable recT = dataTable(
                    new String[]{"#", "Titulo", "Recurrencia", "Fecha/Hora", "Funcionario", "Estudiante", "Estado"},
                    new float[]{1, 6, 3, 4, 6, 6, 2});
            row = 0;
            for (Recordatorio r : recordatorios) {
                boolean alt = (row++ % 2 == 1);
                String func = r.getFuncionario().getUsuario().getNombre()
                            + " " + r.getFuncionario().getUsuario().getApellido();
                String est = r.getEstudiante() != null
                        ? r.getEstudiante().getUsuario().getNombre() + " " + r.getEstudiante().getUsuario().getApellido()
                        : "-";
                addDataRow(recT, alt,
                        String.valueOf(row),
                        r.getTitulo(),
                        r.getRecurrencia().name(),
                        fmt(r.getFecHora()),
                        func,
                        est,
                        r.getEstActivo() ? "Activo" : "Inactivo");
            }
            if (recordatorios.isEmpty()) addEmptyRow(recT, 7);
            doc.add(recT);

            doc.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error generando PDF de actividad", e);
        }
        return baos.toByteArray();
    }

    // ─── Helpers PDF ──────────────────────────────────────────────────────────

    private void addCabecera(Document doc, String titulo, String subtitulo) throws DocumentException {
        Paragraph t = new Paragraph("SIENEP  |  " + titulo, titleFont());
        t.setAlignment(Element.ALIGN_CENTER);
        doc.add(t);

        Paragraph sub = new Paragraph(subtitulo, infoFont());
        sub.setAlignment(Element.ALIGN_CENTER);
        doc.add(sub);

        Paragraph fecha = new Paragraph("Generado: " + fmt(LocalDate.now()), infoFont());
        fecha.setAlignment(Element.ALIGN_RIGHT);
        doc.add(fecha);
        doc.add(new Paragraph(" "));
    }

    private Paragraph seccionTitulo(String texto) {
        Paragraph p = new Paragraph(texto, sectionFont());
        p.setSpacingBefore(8);
        p.setSpacingAfter(4);
        return p;
    }

    private PdfPTable infoTable(int cols) throws DocumentException {
        PdfPTable t = new PdfPTable(cols * 2);
        t.setWidthPercentage(100);
        return t;
    }

    private void addInfoRow(PdfPTable t, String label, String value) {
        PdfPCell lCell = new PdfPCell(new Phrase(label, labelFont()));
        lCell.setBackgroundColor(SECTION_BG);
        lCell.setPadding(4);
        lCell.setBorderColor(Color.LIGHT_GRAY);

        PdfPCell vCell = new PdfPCell(new Phrase(value != null ? value : "-", dataFont()));
        vCell.setPadding(4);
        vCell.setBorderColor(Color.LIGHT_GRAY);

        t.addCell(lCell);
        t.addCell(vCell);
    }

    private PdfPTable dataTable(String[] headers, float[] widths) throws DocumentException {
        PdfPTable t = new PdfPTable(headers.length);
        t.setWidthPercentage(100);
        t.setWidths(widths);
        t.setSpacingBefore(4);

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont()));
            cell.setBackgroundColor(HEADER_BG);
            cell.setPadding(5);
            cell.setBorderColor(Color.WHITE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            t.addCell(cell);
        }
        t.setHeaderRows(1);
        return t;
    }

    private void addDataRow(PdfPTable t, boolean alt, String... values) {
        Color bg = alt ? ALT_ROW : Color.WHITE;
        for (String v : values) {
            PdfPCell cell = new PdfPCell(new Phrase(v != null ? v : "-", dataFont()));
            cell.setBackgroundColor(bg);
            cell.setPadding(4);
            cell.setBorderColor(Color.LIGHT_GRAY);
            t.addCell(cell);
        }
    }

    private void addEmptyRow(PdfPTable t, int cols) {
        PdfPCell cell = new PdfPCell(new Phrase("Sin registros", infoFont()));
        cell.setColspan(cols);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        t.addCell(cell);
    }

    private String fmt(LocalDate d)       { return d != null ? d.format(FMT_DATE)           : "-"; }
    private String fmt(OffsetDateTime dt)  { return dt != null ? dt.format(FMT_DT)            : "-"; }
    private String truncar(String s, int n){ return s != null && s.length() > n ? s.substring(0, n) + "..." : s; }
}
