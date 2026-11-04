package com.vcc.aprender.playenglish

import org.junit.Assert.assertEquals
import org.junit.Test

class BootstrapSmokeTest {
    @Test
    fun applicationPackageDoesNotCollideWithVCCAprenderHost() {
        assertEquals("com.vcc.aprender.playenglish", MainActivity::class.java.packageName)
    }
}
