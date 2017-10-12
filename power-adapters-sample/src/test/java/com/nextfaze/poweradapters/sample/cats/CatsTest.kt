@file:Suppress("IllegalIdentifier")

package com.nextfaze.poweradapters.sample.cats

import com.google.common.truth.Truth.assertThat
import org.junit.Test

private const val LINE = """
"American Curl","United States","Mutation","","Short/Long","All",""
"""

class CsvTest {
    @Test fun `cat is parsed from csv line`() {
        assertThat(parseCat(LINE)).isEqualTo(Cat("American Curl", "United States"))
    }

    @Test fun `paging middle range returns next page`() {
        assertThat((2..5).next(step = 3, total = 10)).isEqualTo(6..8)
    }

    @Test fun `paging last range returns last page capped`() {
        assertThat((5..7).next(step = 3, total = 10)).isEqualTo(8..9)
    }

    @Test fun `paging final range returns empty page`() {
        assertThat((8..9).next(step = 3, total = 10).isEmpty()).isTrue()
    }
}
