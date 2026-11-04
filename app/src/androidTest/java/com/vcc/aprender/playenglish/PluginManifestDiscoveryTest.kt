package com.vcc.aprender.playenglish

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.vcc.aprender.playenglish.plugin.PlayEnglishPlugin
import com.vcc.aprender.playenglish.plugin.PlayEnglishPluginActivity
import com.vcc.aprender.pluginsdk.GameContract
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PluginManifestDiscoveryTest {
    private val appContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun exportedActivityDeclaresExactContractMetadataAndPermission() {
        val manager = appContext.packageManager
        val component = ComponentName(appContext, PlayEnglishPluginActivity::class.java)
        val info = if (Build.VERSION.SDK_INT >= 33) {
            manager.getActivityInfo(
                component,
                PackageManager.ComponentInfoFlags.of(PackageManager.GET_META_DATA.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            manager.getActivityInfo(component, PackageManager.GET_META_DATA)
        }
        assertTrue(info.exported)
        assertEquals(GameContract.PERMISSION_PLAY, info.permission)
        val meta = requireNotNull(info.metaData)
        assertEquals(1, meta.getInt(GameContract.META_PROTOCOL_VERSION, -1))
        assertEquals(PlayEnglishPlugin.GAME_ID, meta.getString(GameContract.META_GAME_ID))
        assertEquals(PlayEnglishPlugin.GAME_TITLE, meta.getString(GameContract.META_GAME_TITLE))
    }

    @Test
    fun actionAndDefaultCategoryResolveToExactlyOneOwnActivity() {
        val manager = appContext.packageManager
        val query = Intent(GameContract.ACTION_PLAY)
            .addCategory(Intent.CATEGORY_DEFAULT)
            .setPackage(appContext.packageName)
        val matches = if (Build.VERSION.SDK_INT >= 33) {
            manager.queryIntentActivities(
                query,
                PackageManager.ResolveInfoFlags.of(PackageManager.GET_META_DATA.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            manager.queryIntentActivities(query, PackageManager.GET_META_DATA)
        }
        val own = matches.filter {
            it.activityInfo?.packageName == appContext.packageName
        }
        assertEquals(1, own.size)
        assertEquals(PlayEnglishPluginActivity::class.java.name, own.single().activityInfo.name)
    }

    @Test
    fun pluginRequestsOnlyHostSignaturePermission() {
        val manager = appContext.packageManager
        val pkg = if (Build.VERSION.SDK_INT >= 33) {
            manager.getPackageInfo(
                appContext.packageName,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            manager.getPackageInfo(appContext.packageName, PackageManager.GET_PERMISSIONS)
        }
        val permissions = pkg.requestedPermissions?.toSet().orEmpty()
        assertTrue(permissions.contains(GameContract.PERMISSION_PLAY))
    }
}
