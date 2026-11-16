package org.aura.assistant.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchResultFormatterTest {
    @Test fun formatsSearchResultsWithSourceUrls() {
        val text = SearchResultFormatter.format(
            listOf(WebSearchResult("AURA docs", "https://example.org/docs", "Reference material"))
        )
        assertTrue(text.contains("AURA docs"))
        assertTrue(text.contains("https://example.org/docs"))
        assertTrue(text.contains("Reference material"))
    }

    @Test fun reportsEmptySearchResults() {
        assertEquals("No search results were returned.", SearchResultFormatter.format(emptyList()))
    }
}
