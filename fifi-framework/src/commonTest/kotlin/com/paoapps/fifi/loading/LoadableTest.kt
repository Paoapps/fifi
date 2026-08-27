package com.paoapps.fifi.loading

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LoadableTest {

    @Test
    fun dataLoadableExposesValue() {
        val loadable = Loadable.data("hello")
        assertEquals("hello", loadable.data)
        assertFalse(loadable.isLoading)
    }

    @Test
    fun loadingLoadableKeepsPlaceholder() {
        val loadable = Loadable.loading("placeholder")
        assertEquals("placeholder", loadable.data)
        assertTrue(loadable.isLoading)
    }

    @Test
    fun errorAndOfflineHaveNoData() {
        assertNull(Loadable.error<String>(message = "boom").data)
        assertTrue(Loadable.error<String>().result.isError)
        assertNull(Loadable.offline<String>().data)
        assertTrue(Loadable.offline<String>().result is Loadable.Result.Offline)
        assertNull(Loadable.empty<String>().data)
    }

    @Test
    fun mapTransformsData() {
        val loadable = Loadable.data(2).map { it * 2 }
        assertEquals(4, loadable.data)
        assertFalse(loadable.isLoading)
    }

    @Test
    fun mapPreservesError() {
        val loadable = Loadable.error<Int>(message = "fail").map { it * 2 }
        assertNull(loadable.data)
        assertTrue(loadable.result is Loadable.Result.Error)
    }

    @Test
    fun asLoadableListWrapsItems() {
        val list = listOf("a", "b").asLoadableList()
        assertEquals(2, list.size)
        assertEquals("a", list[0].data)
        assertEquals("b", list[1].data)
    }
}
