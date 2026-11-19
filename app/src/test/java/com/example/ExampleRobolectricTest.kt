package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SamsungDeviceCatalog
import com.example.data.SamsungTweaksCatalog
import com.example.data.SynonymSearchEngine
import com.example.shizuku.ShizukuManager
import com.example.terminal.GitHubInstaller
import com.example.terminal.TerminalRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches A-Tweaker`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("A-Tweaker", appName)
    }

    @Test
    fun `galaxy A01 to A57 tweaks filter by hardware and dangerous flag`() {
        val a01 = SamsungDeviceCatalog.findByModelCode("SM-A015F")
        val a55 = SamsungDeviceCatalog.findByModelCode("SM-A556B")
        val a57 = SamsungDeviceCatalog.findByModelCode("SM-A576B")
        assertNotNull(a01)
        assertNotNull(a55)
        assertNotNull(a57)

        val a01Tweaks = SamsungTweaksCatalog.tweaksForDevice(a01!!, sdkInt = 31, includeDangerous = false)
        val a55Tweaks = SamsungTweaksCatalog.tweaksForDevice(a55!!, sdkInt = 34, includeDangerous = false)
        val a55WithDanger = SamsungTweaksCatalog.tweaksForDevice(a55, sdkInt = 34, includeDangerous = true)

        assertFalse(a01Tweaks.any { it.id == "force_peak_refresh_90_120" })
        assertTrue(a55Tweaks.any { it.id == "force_peak_refresh_90_120" })
        assertFalse(a55Tweaks.any { it.isDangerous })
        assertTrue(a55WithDanger.any { it.isDangerous })
    }

    @Test
    fun `synonym search finds refresh rate and battery tweaks`() {
        val a54 = SamsungDeviceCatalog.findById("A54")!!
        val tweaks = SamsungTweaksCatalog.tweaksForDevice(a54, sdkInt = 34, includeDangerous = true)

        val smoothHits = SynonymSearchEngine.search("плавность", tweaks)
        assertTrue(smoothHits.any { it.first.id == "force_peak_refresh_90_120" })
        assertTrue(smoothHits.any { it.first.id == "animation_scale_quarter" })

        val drainHits = SynonymSearchEngine.search("разряд", tweaks)
        assertTrue(drainHits.any { it.first.id == "aggressive_doze_idle" })
    }

    @Test
    fun `terminal packages persist across repository restarts`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shizuku = ShizukuManager(context)
        val gh = GitHubInstaller(context)

        val repo1 = TerminalRepository(context, shizuku, gh)
        val res = repo1.installPackage("fastfetch")
        assertTrue(res.isSuccess)
        assertTrue(repo1.isPackageInstalled("fastfetch"))

        // Re-instantiate repository simulating app restart
        val repo2 = TerminalRepository(context, shizuku, gh)
        assertTrue(repo2.isPackageInstalled("fastfetch"))
    }
}
