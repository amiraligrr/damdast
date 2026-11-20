package ir.amiraligrr.damdast

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import ir.amiraligrr.damdast.ads.AdFrequencyController
import ir.amiraligrr.damdast.ads.AdsManager
import ir.amiraligrr.damdast.ads.NoOpAdsManager
import ir.amiraligrr.damdast.ads.TapsellAdsManager
import ir.amiraligrr.damdast.data.AppLanguage
import ir.amiraligrr.damdast.data.SettingsRepository
import ir.amiraligrr.damdast.data.UserDataRepository
import java.util.Locale

class DamdastApp : Application() {
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var userDataRepository: UserDataRepository
        private set
    lateinit var adsManager: AdsManager
        private set

    override fun onCreate() {
        super.onCreate()
        installInitialLocale()
        val fallbackLanguage = if (Locale.getDefault().language == "en") {
            AppLanguage.ENGLISH
        } else {
            AppLanguage.PERSIAN
        }
        settingsRepository = SettingsRepository(this, fallbackLanguage)
        userDataRepository = UserDataRepository(this)
        adsManager = if (BuildConfig.ADS_CONFIGURED && !BuildConfig.ADS_DISABLED_BY_CONFIG) {
            TapsellAdsManager(this, AdFrequencyController(this)).also { it.initialize() }
        } else {
            NoOpAdsManager()
        }
        PDFBoxResourceLoader.init(this)
    }

    private fun installInitialLocale() {
        if (!AppCompatDelegate.getApplicationLocales().isEmpty) return
        val systemLanguage = Locale.getDefault().language
        val tag = if (systemLanguage == "en") "en" else "fa"
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
    }
}

val Context.damdastApp: DamdastApp
    get() = applicationContext as DamdastApp
