data "azurerm_storage_account" "tfstate_app" {
  name                = "pagopainfraterraform${var.env}"
  resource_group_name = "io-infra-rg"
}

data "azurerm_user_assigned_identity" "identity_cd" {
  resource_group_name = "${local.product}-identity-rg"
  name                = "${local.product}-${local.domain}-01-github-cd-identity"
}

data "azurerm_resource_group" "dashboards" {
  name = "dashboards"
}

data "azurerm_resource_group" "apim_resource_group" {
  name = "${local.product}-api-rg"
}

data "azurerm_kubernetes_cluster" "aks" {
  name                = local.aks_cluster.name
  resource_group_name = local.aks_cluster.resource_group_name
}

data "github_organization_teams" "all" {
  root_teams_only = true
  summary_only    = true
}

data "azurerm_resource_group" "github_runner_rg" {
  name = "${local.runner}-github-runner-rg"
}

data "azurerm_key_vault" "key_vault" {
  name = "pagopa-${var.env_short}-kv"
  resource_group_name = "pagopa-${var.env_short}-sec-rg"
}

data "azurerm_key_vault" "domain_key_vault" {
  name = "pagopa-${var.env_short}-${local.domain}-kv"
  resource_group_name = "pagopa-${var.env_short}-${local.domain}-sec-rg"
}

data "azurerm_key_vault_secret" "key_vault_sonar" {
  count  = var.env_short == "d" ? 1 : 0

  name = "sonar-token"
  key_vault_id = data.azurerm_key_vault.key_vault.id
}

data "azurerm_key_vault_secret" "key_vault_bot_token" {
  count  = var.env_short == "d" ? 1 : 0

  name = "bot-token-github"
  key_vault_id = data.azurerm_key_vault.key_vault.id
}
data "azurerm_key_vault_secret" "key_vault_cucumber_token" {
  count  = var.env_short == "d" ? 1 : 0

  name = "cucumber-token"
  key_vault_id = data.azurerm_key_vault.key_vault.id
}

data "azurerm_key_vault_secret" "key_vault_integration_cosmos_negative_biz_key" {
  count  = var.env_short != "p" ? 1 : 0
  name = format("cosmos-%s-biz-key", var.env_short)
  key_vault_id = data.azurerm_key_vault.domain_key_vault.id
}

data "azurerm_cosmosdb_account" "bizevents_cosmos" {
  name                = "pagopa-${var.env_short}-${local.location_short}-bizevents-ds-cosmos-account"
  resource_group_name = "pagopa-${var.env_short}-${local.location_short}-bizevents-rg"
}

data "azurerm_key_vault_secret" "key_vault_integration_test_webhook_slack" {
  count        = var.env_short != "p" ? 1 : 0
  name         = "webhook-slack"
  key_vault_id = data.azurerm_key_vault.domain_key_vault.id
}
