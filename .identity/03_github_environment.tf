resource "github_repository_environment" "github_repository_environment" {
  environment = var.env
  repository  = local.github.repository
  # filter teams reviewers from github_organization_teams
  # if reviewers_teams is null no reviewers will be configured for environment
  dynamic "reviewers" {
    for_each = (var.github_repository_environment.reviewers_teams == null || var.env_short != "p" ? [] : [1])
    content {
      teams = matchkeys(
        data.github_organization_teams.all.teams.*.id,
        data.github_organization_teams.all.teams.*.name,
        var.github_repository_environment.reviewers_teams
      )
    }
  }
  deployment_branch_policy {
    protected_branches     = var.github_repository_environment.protected_branches
    custom_branch_policies = var.github_repository_environment.custom_branch_policies
  }
}

locals {
  env_secrets = {
    "CLIENT_ID" : data.azurerm_user_assigned_identity.identity_cd_01.client_id,
    "TENANT_ID" : data.azurerm_client_config.current.tenant_id,
    "SUBSCRIPTION_ID" : data.azurerm_subscription.current.subscription_id,
    "COSMOS_DB_PRIMARY_KEY" : data.azurerm_cosmosdb_account.bizevents_cosmos.primary_key,
    "BIZ_COSMOS_KEY" : data.azurerm_cosmosdb_account.bizevents_cosmos.primary_readonly_key,
    "COSMOS_DB_CONN_STRING" : "AccountEndpoint=https://pagopa-${var.env_short}-${local.location_short}-bizevents-ds-cosmos-account.documents.azure.com:443/;AccountKey=${data.azurerm_cosmosdb_account.bizevents_cosmos.primary_key};",
    "SUBKEY" : data.azurerm_key_vault_secret.key_vault_integration_test_subkey.value
  }
  env_variables = {
    "CONTAINER_APP_ENVIRONMENT_NAME" : local.container_app_environment.name,
    "CONTAINER_APP_ENVIRONMENT_RESOURCE_GROUP_NAME" : local.container_app_environment.resource_group,
    "CLUSTER_NAME" : local.aks_cluster.name,
    "CLUSTER_RESOURCE_GROUP" : local.aks_cluster.resource_group_name,
    "DOMAIN" : local.domain,
    "NAMESPACE" : local.domain,
    "WORKLOAD_IDENTITY_ID": data.azurerm_user_assigned_identity.workload_identity_clientid.client_id
  }
}

###############
# ENV Secrets #
###############

resource "github_actions_environment_secret" "github_environment_runner_secrets" {
  for_each        = local.env_secrets
  repository      = local.github.repository
  environment     = var.env
  secret_name     = each.key
  plaintext_value = each.value
}

#################
# ENV Variables #
#################


resource "github_actions_environment_variable" "github_environment_runner_variables" {
  for_each      = local.env_variables
  repository    = local.github.repository
  environment   = var.env
  variable_name = each.key
  value         = each.value
}


###############
# ENV Secrets #
###############

#tfsec:ignore:github-actions-no-plain-text-action-secrets 
resource "github_actions_secret" "secret_sonar_token" {
  count  = var.env_short == "d" ? 1 : 0

  repository       = local.github.repository
  secret_name      = "SONAR_TOKEN"
  plaintext_value  = data.azurerm_key_vault_secret.key_vault_sonar[0].value
}

#tfsec:ignore:github-actions-no-plain-text-action-secrets 
resource "github_actions_secret" "secret_bot_token" {
  count  = var.env_short == "d" ? 1 : 0

  repository       = local.github.repository
  secret_name      = "BOT_TOKEN_GITHUB"
  plaintext_value  = data.azurerm_key_vault_secret.key_vault_bot_token[0].value
}

##tfsec:ignore:github-actions-no-plain-text-action-secrets # not real secret
#resource "github_actions_secret" "secret_cucumber_token" {
#  count  = var.env_short == "d" ? 1 : 0
#
#  repository       = local.github.repository
#  secret_name      = "CUCUMBER_PUBLISH_TOKEN"
#  plaintext_value  = data.azurerm_key_vault_secret.key_vault_cucumber_token[0].value
#}

#tfsec:ignore:github-actions-no-plain-text-action-secrets 
resource "github_actions_environment_secret" "secret_integration_test_cosmos_key" {
  count  = var.env_short != "p" ? 1 : 0
  repository       = local.github.repository
  environment      = var.env
  secret_name      = "COSMOS_DB_PRIMARY_KEY"
  plaintext_value  = data.azurerm_key_vault_secret.key_vault_integration_cosmos_negative_biz_key[0].value
}

#tfsec:ignore:github-actions-no-plain-text-action-secrets # not real secret
resource "github_actions_secret" "secret_slack_webhook" {
  # count        = var.env_short != "p" ? 1 : 0
  repository      = local.github.repository
  secret_name     = "SLACK_WEBHOOK_URL"
  plaintext_value = data.azurerm_key_vault_secret.key_vault_integration_test_webhook_slack.value
}