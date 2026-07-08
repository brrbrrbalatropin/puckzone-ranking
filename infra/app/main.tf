resource "azurerm_container_app" "ranking" {
  name                         = "puckzone-ranking"
  resource_group_name          = data.terraform_remote_state.base.outputs.resource_group_name
  container_app_environment_id = data.terraform_remote_state.base.outputs.container_app_environment_id
  revision_mode                = "Single"

  # Credenciales sensibles como secrets de la Container App (no aparecen en
  # texto plano en el portal ni en `az containerapp show`).
  secret {
    name  = "db-password"
    value = data.terraform_remote_state.base.outputs.postgres_admin_password
  }

  template {
    # Ranking es stateless (todo vive en Postgres): 1 replica alcanza para la
    # carga del proyecto.
    min_replicas = 1
    max_replicas = 1

    container {
      # 0.5/1Gi como game y auth: con menos CPU el arranque de Spring supera
      # los ~30s y la liveness probe mata el contenedor antes de abrir el puerto.
      name   = "ranking"
      image  = var.image
      cpu    = 0.5
      memory = "1Gi"

      # El application.yaml arma la URL con POSTGRES_HOST/PORT/DB y no permite
      # agregar sslmode; se sobreescribe la propiedad completa via env vars
      # SPRING_DATASOURCE_* (tienen precedencia sobre el yaml). Azure Flexible
      # Server exige TLS, por eso sslmode=require.
      env {
        name  = "SPRING_DATASOURCE_URL"
        value = "jdbc:postgresql://${data.terraform_remote_state.base.outputs.postgres_fqdn}:5432/ranking_db?sslmode=require"
      }
      env {
        name  = "SPRING_DATASOURCE_USERNAME"
        value = data.terraform_remote_state.base.outputs.postgres_admin_login
      }
      env {
        name        = "SPRING_DATASOURCE_PASSWORD"
        secret_name = "db-password"
      }
      env {
        name  = "APPLICATIONINSIGHTS_CONNECTION_STRING"
        value = data.terraform_remote_state.base.outputs.application_insights_connection_string
      }

      liveness_probe {
        transport = "HTTP"
        port      = 8084
        path      = "/actuator/health/liveness"
        # Margen para el arranque de Spring; sin esto la probe empieza a fallar
        # de inmediato y ACA reinicia el contenedor en bucle.
        initial_delay = 20
      }
      readiness_probe {
        transport = "HTTP"
        port      = 8084
        path      = "/actuator/health/readiness"
        initial_delay = 10
      }
    }
  }

  # Interno: solo game y el gateway (en el mismo environment) le hablan.
  # allow_insecure permite trafico HTTP plano entre apps del environment (el
  # redirect a HTTPS rompe las llamadas: cert de .internal. no confiable en Java).
  ingress {
    external_enabled           = false
    target_port                = 8084
    transport                  = "auto"
    allow_insecure_connections = true

    traffic_weight {
      latest_revision = true
      percentage      = 100
    }
  }

  lifecycle {
    # El pipeline actualiza la imagen con az containerapp update; sin esto,
    # cada terraform apply intentaria devolver la app a la imagen inicial.
    ignore_changes = [template[0].container[0].image]
  }
}
