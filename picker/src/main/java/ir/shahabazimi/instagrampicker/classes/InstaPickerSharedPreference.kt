package ir.shahabazimi.instagrampicker.classes

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class InstaPickerSharedPreference private constructor(ctx: Context) {
    private val masterKey =
        MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
    private val sp = EncryptedSharedPreferences.create(
        ctx,
        "InstagramPickerSharedPreference",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )


    companion object {
        @Volatile
        private var instance: InstaPickerSharedPreference? = null
        @JvmStatic
        operator fun invoke(ctx: Context): InstaPickerSharedPreference =
            instance ?: synchronized(this) {
                instance ?: InstaPickerSharedPreference(ctx).also { instance = it }
            }
    }

    fun getCameraPermission() = sp.getBoolean("camera", false)
    fun setCameraPermission() = sp.edit { putBoolean("camera", true) }

    fun getStoragePermission() = sp.getBoolean("external", false)
    fun setStoragePermission() = sp.edit { putBoolean("external", true) }
}

