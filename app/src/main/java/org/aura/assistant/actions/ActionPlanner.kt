package org.aura.assistant.actions

sealed interface ActionRequest {
    data class LaunchApp(val packageName: String) : ActionRequest
    data class CalendarReminder(val title: String) : ActionRequest
    data class HomeAssistantService(val entityId: String, val domain: String, val service: String) : ActionRequest
    data class ScheduledReminder(val title: String, val delayMinutes: Long) : ActionRequest
}

enum class ActionRisk { LOW, MEDIUM, HIGH }

data class ValidatedAction(val request: ActionRequest, val risk: ActionRisk, val requiresConfirmation: Boolean)

object ActionRequestValidator {
    fun validate(request: ActionRequest): ValidatedAction {
        return when (request) {
            is ActionRequest.LaunchApp -> {
                require(PackageNameValidator.isValid(request.packageName.trim())) { "Invalid app package." }
                ValidatedAction(request, ActionRisk.MEDIUM, true)
            }
            is ActionRequest.CalendarReminder -> {
                require(request.title.trim().length <= 120) { "Reminder title is too long." }
                ValidatedAction(request, ActionRisk.LOW, true)
            }
            is ActionRequest.HomeAssistantService -> {
                require(request.domain in setOf("light", "switch", "fan", "climate", "scene", "script")) { "Unsupported smart-home domain." }
                require(request.service in setOf("turn_on", "turn_off", "toggle")) { "Unsupported smart-home service." }
                require(request.entityId.startsWith(request.domain + ".") && request.entityId.length <= 180) { "Entity does not match its domain." }
                ValidatedAction(request, ActionRisk.HIGH, true)
            }
            is ActionRequest.ScheduledReminder -> {
                require(request.delayMinutes in 1L..10080L) { "Reminder delay must be between 1 minute and 7 days." }
                require(request.title.trim().length <= 120) { "Reminder title is too long." }
                ValidatedAction(request, ActionRisk.LOW, true)
            }
        }
    }
}
