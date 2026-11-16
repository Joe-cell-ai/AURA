package org.aura.assistant.actions

sealed interface ActionRequest {
    data class LaunchApp(val packageName: String) : ActionRequest
    data class CalendarReminder(val title: String) : ActionRequest
    data class HomeAssistantService(val entityId: String, val domain: String, val service: String) : ActionRequest
    data class ScheduledReminder(val title: String, val delayMinutes: Long) : ActionRequest
    data class DialNumber(val phoneNumber: String) : ActionRequest
    data class ComposeSms(val phoneNumber: String, val message: String) : ActionRequest
    data class OpenSystemSettings(val destination: SettingsDestination) : ActionRequest
}

enum class SettingsDestination { WIFI, BLUETOOTH, SOUND, DISPLAY, APPLICATIONS, SECURITY, LOCATION }

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
                require(request.title.trim().isNotEmpty() && request.title.trim().length <= 120) { "Reminder title must contain 1–120 characters." }
                ValidatedAction(request, ActionRisk.LOW, true)
            }
            is ActionRequest.DialNumber -> {
                require(normalizePhone(request.phoneNumber).length in 3..20) { "Enter a valid phone number." }
                ValidatedAction(request.copy(phoneNumber = normalizePhone(request.phoneNumber)), ActionRisk.MEDIUM, true)
            }
            is ActionRequest.ComposeSms -> {
                val number = normalizePhone(request.phoneNumber)
                require(number.length in 3..20) { "Enter a valid phone number." }
                require(request.message.isNotBlank() && request.message.length <= 2000) { "SMS must contain 1–2,000 characters." }
                ValidatedAction(request.copy(phoneNumber = number, message = request.message.trim()), ActionRisk.HIGH, true)
            }
            is ActionRequest.OpenSystemSettings ->
                ValidatedAction(request, ActionRisk.MEDIUM, true)
        }
    }

    private fun normalizePhone(value: String): String {
        val trimmed = value.trim()
        require(Regex("^\\+?[0-9 ()-]+$").matches(trimmed)) { "Phone number contains unsupported characters." }
        return trimmed.filter { it.isDigit() || it == '+' }
    }
}
