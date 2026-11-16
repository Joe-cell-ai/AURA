package org.aura.assistant.automation

enum class AutomationRepeat(val intervalDays: Int) {
    ONCE(0),
    DAILY(1),
    WEEKLY(7)
}

data class AutomationRule(
    val title: String,
    val repeat: AutomationRepeat,
    val enabled: Boolean = true
)

object AutomationRuleValidator {
    fun validate(rule: AutomationRule): AutomationRule {
        val title = rule.title.trim()
        require(title.isNotEmpty()) { "Add a title for this automation." }
        require(title.length <= 120) { "Automation titles must be 120 characters or fewer." }
        require(AutomationRepeat.values().contains(rule.repeat)) { "Choose a supported repeat schedule." }
        return rule.copy(title = title)
    }
}
