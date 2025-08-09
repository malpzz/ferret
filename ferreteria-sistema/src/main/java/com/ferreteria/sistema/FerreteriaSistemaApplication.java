package com.ferreteria.sistema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Clase principal de la aplicación Sistema de Ferretería
 * 
 * Esta aplicación Spring Boot proporciona un sistema completo de gestión
 * para ferreterías, incluyendo:
 * - Gestión de inventario y productos
 * - Control de ventas y facturación
 * - Administración de clientes y proveedores
 * - Gestión de empleados y horarios
 * - Reportes y estadísticas
 * - Sistema de usuarios y roles
 * 
 * @author Sistema Ferretería
 * @version 1.0.0
 * @since 2024
 */
@SpringBootApplication
@EnableJpaAuditing // Habilita auditoría automática de entidades JPA
@EnableTransactionManagement // Habilita gestión declarativa de transacciones
@EnableCaching // Habilita cache de Spring
@EnableAsync // Habilita procesamiento asíncrono
@EnableScheduling // Habilita tareas programadas
@EnableConfigurationProperties // Habilita propiedades de configuración personalizadas
public class FerreteriaSistemaApplication {

    /**
     * Método principal que inicia la aplicación Spring Boot
     * 
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        // Configura propiedades del sistema antes de iniciar
        System.setProperty("spring.devtools.restart.enabled", "true");
        System.setProperty("spring.jpa.open-in-view", "false");
        
        // Inicia la aplicación Spring Boot
        SpringApplication application = new SpringApplication(FerreteriaSistemaApplication.class);
        
        // Configura propiedades adicionales de la aplicación
        application.setAdditionalProfiles("default");
        
        // Ejecuta la aplicación
        application.run(args);
    }
}

