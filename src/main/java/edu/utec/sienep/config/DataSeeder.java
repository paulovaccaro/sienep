package edu.utec.sienep.config;

import edu.utec.sienep.entity.*;
import edu.utec.sienep.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final RolRepository rolRepo;
    private final PermisoRepository permisoRepo;
    private final RolPermisoRepository rolPermisoRepo;
    private final CiudadRepository ciudadRepo;
    private final DireccionRepository direccionRepo;
    private final ITRRepository itrRepo;
    private final CarreraRepository carreraRepo;
    private final GrupoRepository grupoRepo;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedRolesYPermisos();
        seedPermisosRecordatorioSiNecesario();
        seedPermisosInstanciaSiNecesario();
        seedEstructuraAcademica();
    }

    private void seedRolesYPermisos() {
        if (rolRepo.count() > 0) {
            log.info("Roles ya presentes, se omite seed de RBAC.");
            return;
        }

        log.info("Seeding roles y permisos...");

        Rol admin   = rol("Administrador",         "Acceso total al sistema");
        Rol psico   = rol("Psicopedagogo",         "Gestiona seguimientos, instancias y observaciones");
        Rol analista= rol("Analista Educativo",    "Acceso operativo en su scope");
        Rol resp    = rol("Responsable Educativo", "Acceso operativo en su scope");
        rol("Area Legal",            "Consulta de información para procesos legales");
        rol("Estudiante",            "Acceso a información propia");

        List<String> todosLosCodigos = List.of(
            "usuarios.leer", "usuarios.crear", "usuarios.modificar", "usuarios.eliminar",
            "funcionarios.leer", "funcionarios.crear", "funcionarios.modificar", "funcionarios.eliminar",
            "estudiantes.leer", "estudiantes.crear", "estudiantes.modificar", "estudiantes.eliminar",
            "seguimientos.leer", "seguimientos.crear", "seguimientos.modificar", "seguimientos.eliminar",
            "instancias.leer", "instancias.crear", "instancias.modificar", "instancias.eliminar",
            "observaciones.leer", "observaciones.crear", "observaciones.modificar", "observaciones.eliminar",
            "info_final.leer", "info_final.crear", "info_final.modificar", "info_final.eliminar",
            "arch_adjuntos.leer", "arch_adjuntos.crear", "arch_adjuntos.modificar", "arch_adjuntos.eliminar",
            "notificaciones.leer", "notificaciones.crear", "notificaciones.modificar", "notificaciones.eliminar",
            "incidencias.leer", "incidencias.crear", "incidencias.modificar", "incidencias.eliminar",
            "recordatorios.leer", "recordatorios.crear", "recordatorios.modificar", "recordatorios.eliminar",
            "recordatorios.gestionar",
            "instancias.gestionar",
            "roles.gestionar", "asignaciones.gestionar",
            "grupos.gestionar", "carreras.gestionar", "itr.gestionar",
            "reportes.generar", "reportes.exportar"
        );

        List<Permiso> permisos = todosLosCodigos.stream().map(this::permiso).toList();

        // Administrador: todos los permisos
        permisos.forEach(p -> rolPermiso(admin, p));

        // Psicopedagogo: operativo
        List<String> codigosPsico = List.of(
            "estudiantes.leer", "seguimientos.leer", "seguimientos.crear", "seguimientos.modificar",
            "instancias.leer", "instancias.crear", "instancias.modificar",
            "observaciones.leer", "observaciones.crear", "observaciones.modificar",
            "arch_adjuntos.leer", "arch_adjuntos.crear",
            "notificaciones.leer", "notificaciones.crear",
            "info_final.leer", "info_final.crear",
            "recordatorios.leer", "recordatorios.crear", "recordatorios.modificar", "recordatorios.eliminar"
        );
        permisos.stream()
            .filter(p -> codigosPsico.contains(p.getCodigo()))
            .forEach(p -> rolPermiso(psico, p));

        // Analista y Responsable: lectura operativa
        List<String> codigosLectura = List.of(
            "estudiantes.leer", "seguimientos.leer", "instancias.leer",
            "observaciones.leer", "info_final.leer", "arch_adjuntos.leer",
            "notificaciones.leer", "recordatorios.leer", "reportes.generar"
        );
        permisos.stream()
            .filter(p -> codigosLectura.contains(p.getCodigo()))
            .forEach(p -> { rolPermiso(analista, p); rolPermiso(resp, p); });

        log.info("Seed RBAC completado: 6 roles, {} permisos.", permisos.size());
    }

    private void seedPermisosRecordatorioSiNecesario() {
        if (permisoRepo.existsByCodigo("recordatorios.gestionar")) {
            log.info("Permisos de recordatorios ya presentes, se omite.");
            return;
        }

        log.info("Seeding permisos de recordatorios...");

        Rol admin = rolRepo.findByNombre("Administrador")
                .orElseThrow(() -> new IllegalStateException("Rol Administrador no encontrado"));
        Rol psico = rolRepo.findByNombre("Psicopedagogo")
                .orElseThrow(() -> new IllegalStateException("Rol Psicopedagogo no encontrado"));
        Rol analista = rolRepo.findByNombre("Analista Educativo")
                .orElseThrow(() -> new IllegalStateException("Rol Analista Educativo no encontrado"));
        Rol resp = rolRepo.findByNombre("Responsable Educativo")
                .orElseThrow(() -> new IllegalStateException("Rol Responsable Educativo no encontrado"));

        List<String> operativos = List.of(
                "recordatorios.leer", "recordatorios.crear",
                "recordatorios.modificar", "recordatorios.eliminar");
        List<String> todos = List.of(
                "recordatorios.leer", "recordatorios.crear",
                "recordatorios.modificar", "recordatorios.eliminar",
                "recordatorios.gestionar");

        List<Permiso> permisos = todos.stream().map(this::permiso).toList();

        permisos.forEach(p -> rolPermiso(admin, p));
        permisos.stream().filter(p -> operativos.contains(p.getCodigo()))
                .forEach(p -> { rolPermiso(psico, p); rolPermiso(analista, p); rolPermiso(resp, p); });

        log.info("Permisos de recordatorios seeded.");
    }

    private void seedPermisosInstanciaSiNecesario() {
        if (permisoRepo.existsByCodigo("instancias.gestionar")) {
            log.info("Permiso instancias.gestionar ya presente, se omite.");
            return;
        }

        log.info("Seeding permiso instancias.gestionar...");

        Rol admin = rolRepo.findByNombre("Administrador")
                .orElseThrow(() -> new IllegalStateException("Rol Administrador no encontrado"));

        Permiso p = permiso("instancias.gestionar");
        rolPermiso(admin, p);

        log.info("Permiso instancias.gestionar seeded.");
    }

    private void seedEstructuraAcademica() {
        if (itrRepo.count() > 0) {
            log.info("ITRs ya presentes, se omite seed académico.");
            return;
        }

        log.info("Seeding estructura académica...");

        Ciudad mvd = ciudad(11000, "Montevideo", "Montevideo");
        Ciudad riv = ciudad(35000, "Rivera",     "Rivera");
        Ciudad pun = ciudad(60000, "Paysandú",   "Paysandú");

        Direccion dirSur   = direccion("Avenida Italia",    "6201", null, mvd);
        Direccion dirNorte = direccion("Avenida Sarandí",   "570",  null, riv);
        Direccion dirLito  = direccion("Leandro Gómez",     "1870", null, pun);

        ITR itrSur   = itr("ITR-SUR",  "ITR Sur",           dirSur);
        ITR itrNorte = itr("ITR-NOR",  "ITR Norte",         dirNorte);
        ITR itrLito  = itr("ITR-LITO", "ITR Litoral Norte", dirLito);

        Carrera tic  = carrera("TIC",  "Tecnología de la Información y Comunicación", "2019");
        Carrera info = carrera("INFO", "Ingeniería en Tecnologías de la Información",  "2019");

        grupo("TIC-SUR-2025-1",   tic,  itrSur,   2025, 1);
        grupo("INFO-SUR-2025-1",  info, itrSur,   2025, 1);
        grupo("TIC-NOR-2025-1",   tic,  itrNorte, 2025, 1);
        grupo("INFO-LITO-2025-1", info, itrLito,  2025, 1);

        log.info("Seed académico completado: 3 ITRs, 2 carreras, 4 grupos.");
    }

    // helpers

    private Rol rol(String nombre, String descripcion) {
        Rol r = new Rol();
        r.setNombre(nombre);
        r.setDescripcion(descripcion);
        r.setEsSistema(true);
        r.setEstActivo(true);
        return rolRepo.save(r);
    }

    private Permiso permiso(String codigo) {
        Permiso p = new Permiso();
        p.setCodigo(codigo);
        return permisoRepo.save(p);
    }

    private void rolPermiso(Rol rol, Permiso permiso) {
        RolPermiso rp = new RolPermiso();
        rp.setId(new RolPermisoId(rol.getIdRol(), permiso.getIdPermiso()));
        rp.setRol(rol);
        rp.setPermiso(permiso);
        rolPermisoRepo.save(rp);
    }

    private Ciudad ciudad(int codPostal, String nombre, String depto) {
        Ciudad c = new Ciudad();
        c.setCodPostal(codPostal);
        c.setNombre(nombre);
        c.setDepartamento(depto);
        return ciudadRepo.save(c);
    }

    private Direccion direccion(String calle, String numPuerta, String numApto, Ciudad ciudad) {
        Direccion d = new Direccion();
        d.setCalle(calle);
        d.setNumPuerta(numPuerta);
        d.setNumApto(numApto);
        d.setCiudad(ciudad);
        return direccionRepo.save(d);
    }

    private ITR itr(String codigo, String nombre, Direccion direccion) {
        ITR i = new ITR();
        i.setCodigo(codigo);
        i.setNombre(nombre);
        i.setDireccion(direccion);
        i.setEstActivo(true);
        return itrRepo.save(i);
    }

    private Carrera carrera(String codigo, String nombre, String plan) {
        Carrera c = new Carrera();
        c.setCodigo(codigo);
        c.setNombre(nombre);
        c.setPlan(plan);
        c.setEstActivo(true);
        return carreraRepo.save(c);
    }

    private Grupo grupo(String nombre, Carrera carrera, ITR itr, int anio, int semestre) {
        Grupo g = new Grupo();
        g.setNomGrupo(nombre);
        g.setCarrera(carrera);
        g.setItr(itr);
        g.setAnio(anio);
        g.setSemestre(semestre);
        g.setEstActivo(true);
        return grupoRepo.save(g);
    }
}
