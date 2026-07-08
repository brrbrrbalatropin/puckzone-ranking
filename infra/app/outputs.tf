output "ranking_internal_fqdn" {
  description = "FQDN interno de ranking; gateway y game lo usan como RANKING_SERVICE_URL"
  value       = "https://${azurerm_container_app.ranking.name}.internal.${data.terraform_remote_state.base.outputs.container_app_environment_default_domain}"
}
