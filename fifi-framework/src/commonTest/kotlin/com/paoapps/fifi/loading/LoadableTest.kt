package com.paoapps.fifi.loading

import com.paoapps.blockedcache.CacheResult
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
    fun cacheResultSuccessMapsToData() {
        val result = CacheResult.Success("value")
        val loadable = result.asLoadable(loadingPlaceholder = "loading")
        assertEquals("value", loadable.data)
        assertFalse(loadable.isLoading)
    }

    @Test
    fun cacheResultLoadingMapsToLoadingPlaceholder() {
        val result = CacheResult.Loading(null, 0L)
        val loadable = result.asLoadable(loadingPlaceholder = "loading")
        assertEquals("loading", loadable.data)
        assertTrue(loadable.isLoading)
    }

    @Test
    fun cacheResultOfflineMapsToOffline() {
        val result = CacheResult.Offline(null, 0L)
        val loadable = result.asLoadable<String>(loadingPlaceholder = null)
        assertNull(loadable.data)
        assertTrue(loadable.result is Loadable.Result.Offline)
    }

    @Test
    fun mapTransformsData() {
        val loadable = Loadable.data(2).map { it * 2 }
        assertEquals(4, loadable.data)
    }
}
