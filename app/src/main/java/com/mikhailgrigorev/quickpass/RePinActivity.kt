package com.mikhailgrigorev.quickpass

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.mikhailgrigorev.quickpass.dbhelpers.DataBaseHelper
import kotlinx.android.synthetic.main.activity_pin.*
import java.util.concurrent.Executor

class RePinActivity : AppCompatActivity() {

    private val _keyUsername = "prefUserNameKey"
    private val _keyTheme = "themePreference"
    private val _preferenceFile = "quickPassPreference"
    private val _keyUsePin = "prefUsePinKey"
    private val _keyBio = "prefUserBioKey"
    private lateinit var login: String
    private lateinit var passName: String
    private lateinit var account: String
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    @SuppressLint("SetTextI18n", "Recycle")
    override fun onCreate(savedInstanceState: Bundle?) {
        val pref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
        when(pref.getString(_keyTheme, "none")){
            "yes" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "no" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "none", "default" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            "battery" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
        }
        when(pref.getString("themeAccentPreference", "none")){
            "Red" -> setTheme(R.style.AppThemeRed)
            "Pink" -> setTheme(R.style.AppThemePink)
            "Purple" -> setTheme(R.style.AppThemePurple)
            "Violet" -> setTheme(R.style.AppThemeViolet)
            "DViolet" -> setTheme(R.style.AppThemeDarkViolet)
            "Blue" -> setTheme(R.style.AppThemeBlue)
            "Cyan" -> setTheme(R.style.AppThemeCyan)
            "Teal" -> setTheme(R.style.AppThemeTeal)
            "Green" -> setTheme(R.style.AppThemeGreen)
            "LGreen" -> setTheme(R.style.AppThemeLightGreen)
            else -> setTheme(R.style.AppTheme)
        }
        super.onCreate(savedInstanceState)
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO ->
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        setContentView(R.layout.activity_pin)

        val args: Bundle? = intent.extras
        login = args?.get("login").toString()
        passName = args?.get("passName").toString()
        account = args?.get("activity").toString()
        val name: String? = getString(R.string.hi) + " " + login
        helloTextId.text = name



        val dbHelper = DataBaseHelper(this)
        val database = dbHelper.writableDatabase
        val cursor: Cursor = database.query(
                dbHelper.TABLE_USERS, arrayOf(dbHelper.KEY_IMAGE),
                "NAME = ?", arrayOf(login),
                null, null, null
        )
        if (cursor.moveToFirst()) {
            val imageIndex: Int = cursor.getColumnIndex(dbHelper.KEY_IMAGE)
            do {
                when(cursor.getString(imageIndex).toString()){
                    "ic_account" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account)
                    "ic_account_Pink" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Pink)
                    "ic_account_Red" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Red)
                    "ic_account_Purple" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Purple)
                    "ic_account_Violet" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Violet)
                    "ic_account_Dark_Violet" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Dark_Violet)
                    "ic_account_Blue" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Blue)
                    "ic_account_Cyan" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Cyan)
                    "ic_account_Teal" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Teal)
                    "ic_account_Green" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_Green)
                    "ic_account_lightGreen" -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account_lightGreen)
                    else -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account)
                }
                accountAvatarText.text = login[0].toString()
            } while (cursor.moveToNext())
        }

        // Checking prefs
        val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)

        val useBio = sharedPref.getString(_keyBio, "none")
        val usePin = sharedPref.getString(_keyUsePin, "none")

        if(useBio != "none"){
            finger.visibility = View.VISIBLE
            finger.isClickable = true
            executor = ContextCompat.getMainExecutor(this)
            biometricPrompt = BiometricPrompt(this, executor,
                    object : BiometricPrompt.AuthenticationCallback() {

                        override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            val intent = Intent()
                            intent.putExtra("login", login)
                            intent.putExtra("passName", passName)
                            setResult(1, intent)
                            finish()
                        }

                    })

            promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getString(R.string.biometricLogin))
                    .setSubtitle(getString(R.string.logWithBio))
                    .setNegativeButtonText(getString(R.string.usePass))
                    .build()

            // Prompt appears when user clicks "Log in".
            // Consider integrating with the keystore to unlock cryptographic operations,
            // if needed by your app.
            biometricPrompt.authenticate(promptInfo)

        }

        finger.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }


        num0.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "0")
        }
        num1.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "1")
        }
        num2.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "2")
        }
        num3.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "3")
        }
        num4.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "4")
        }
        num5.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "5")
        }
        num6.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "6")
        }
        num7.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "7")
        }
        num8.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "8")
        }
        num9.setOnClickListener {
            if(inputPinIdField.text.toString().length < 4)
                inputPinIdField.setText(inputPinIdField.text.toString() + "9")
        }
        erase.setOnClickListener {
            if(inputPinIdField.text.toString().isNotEmpty())
                inputPinIdField.setText(inputPinIdField.text.toString().substring(0, inputPinIdField.text.toString().length - 1))
        }

        exit.setOnClickListener {
            exit(sharedPref)
        }


        inputPinIdField.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if(inputPinIdField.text.toString().length == 4){
                    if(inputPinIdField.text.toString() == usePin){
                        val intent = Intent()
                        intent.putExtra("login", login)
                        intent.putExtra("passName", passName)
                        setResult(1, intent)
                        finish()
                    }
                    else{
                        inputPinId.error = getString(R.string.incorrectPin)
                    }
                }
                else{
                    inputPinId.error = null
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

    }
    private fun exit(sharedPref: SharedPreferences) {
        sharedPref.edit().remove(_keyUsername).apply()
        sharedPref.edit().remove(_keyUsePin).apply()
        sharedPref.edit().remove(_keyBio).apply()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

}