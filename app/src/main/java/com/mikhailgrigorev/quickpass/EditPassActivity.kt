package com.mikhailgrigorev.quickpass

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.database.SQLException
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.chip.Chip
import com.mikhailgrigorev.quickpass.dbhelpers.DataBaseHelper
import com.mikhailgrigorev.quickpass.dbhelpers.PasswordsDataBaseHelper
import kotlinx.android.synthetic.main.activity_edit_pass.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*


class EditPassActivity : AppCompatActivity() {

    private var isImage = false
    private val _keyTheme = "themePreference"
    private val _keyUsername = "prefUserNameKey"
    private val _preferenceFile = "quickPassPreference"
    private var length = 20
    private var useSymbols = false
    private var useUC = false
    private var useLetters = false
    private var useNumbers = false
    private lateinit var login: String
    private lateinit var passName: String
    private var imageName: String = ""
    @SuppressLint("Recycle", "SetTextI18n", "ClickableViewAccessibility")
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

        val handler = Handler()
        val r = Runnable {
            if(condition) {
                condition=false
                val intent = Intent(this, LoginAfterSplashActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        val time: Long =  100000
        val sharedPref = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE)
        val lockTime = sharedPref.getString("appLockTime", "6")
        if(lockTime != null) {
            if (lockTime != "0")
                handler.postDelayed(r, time * lockTime.toLong())
        }
        else
            handler.postDelayed(r, time * 6L)

        setContentView(R.layout.activity_edit_pass)

        val args: Bundle? = intent.extras
        login = args?.get("login").toString()
        val newLogin = getSharedPreferences(_preferenceFile, Context.MODE_PRIVATE).getString(
                _keyUsername,
                login
        )
        if(newLogin != login)
            login = newLogin.toString()
        passName = args?.get("passName").toString()

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
                    "ic_account" -> accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account
                            )
                    "ic_account_Pink" -> accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Pink
                            )
                    "ic_account_Red" -> accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Red
                            )
                    "ic_account_Purple" -> accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Purple
                            )
                    "ic_account_Violet" -> accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Violet
                            )
                    "ic_account_Dark_Violet" -> accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Dark_Violet
                            )
                    "ic_account_Blue" -> accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Blue
                            )
                    "ic_account_Cyan" -> accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Cyan
                            )
                    "ic_account_Teal" -> accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Teal
                            )
                    "ic_account_Green" -> accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_Green
                            )
                    "ic_account_lightGreen" -> accountAvatar.backgroundTintList =
                            ContextCompat.getColorStateList(
                                    this, R.color.ic_account_lightGreen
                            )
                    else -> accountAvatar.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.ic_account
                    )
                }
                accountAvatarText.text = login[0].toString()
            } while (cursor.moveToNext())
        }

        accountAvatar.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            intent.putExtra("login", login)
            intent.putExtra("activity", "menu")
            startActivityForResult(intent, 1)
        }


        var dbLogin = ""
        var dbPassword: String

        val list = mutableListOf<String>()

        val pdbHelper = PasswordsDataBaseHelper(this, login)
        val pDatabase = pdbHelper.writableDatabase
        try {
            val pCursor: Cursor = pDatabase.query(
                    pdbHelper.TABLE_USERS, arrayOf(
                    pdbHelper.KEY_NAME,
                    pdbHelper.KEY_PASS,
                    pdbHelper.KEY_2FA,
                    pdbHelper.KEY_USE_TIME,
                    pdbHelper.KEY_TIME,
                    pdbHelper.KEY_DESC,
                    pdbHelper.KEY_TAGS,
                    pdbHelper.KEY_CIPHER,
                    pdbHelper.KEY_LOGIN
            ),
                    "NAME = ?", arrayOf(passName),
                    null, null, null
            )


            if (pCursor.moveToFirst()) {
                val nameIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_NAME)
                val passIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_PASS)
                val aIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_2FA)
                val uTIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_USE_TIME)
                val descIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_DESC)
                val tagsIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_TAGS)
                val loginIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_LOGIN)
                val cryptIndex: Int = pCursor.getColumnIndex(pdbHelper.KEY_CIPHER)
                do {
                    dbLogin = pCursor.getString(nameIndex).toString()
                    helloTextId.text = dbLogin
                    newNameField.setText(dbLogin)
                    val dbCryptIndex = pCursor.getString(cryptIndex).toString()
                    dbPassword = pCursor.getString(passIndex).toString()
                    if (dbCryptIndex == "crypted") {
                        cryptToggle.isChecked = true
                        val pm = PasswordManager()
                        dbPassword = pm.decrypt(dbPassword).toString()
                    }
                    genPasswordIdField.setText(dbPassword)
                    if (dbPassword != "") {
                        length = dbPassword.length
                        seekBar.progress = length
                        lengthToggle.text = getString(R.string.length) + ": " + length
                        val myPasswordManager = PasswordManager()
                        val evaluation: String = myPasswordManager.evaluatePasswordString(
                                genPasswordIdField.text.toString()
                        )
                        passQuality.text = evaluation
                        when (evaluation) {
                            "low" -> passQuality.text = getString(R.string.low)
                            "high" -> passQuality.text = getString(R.string.high)
                            else -> passQuality.text = getString(R.string.medium)
                        }
                        when (evaluation) {
                            "low" -> passQuality.setTextColor(
                                    ContextCompat.getColor(
                                            this,
                                            R.color.negative
                                    )
                            )
                            "high" -> passQuality.setTextColor(
                                    ContextCompat.getColor(
                                            this,
                                            R.color.positive
                                    )
                            )
                            else -> passQuality.setTextColor(
                                    ContextCompat.getColor(
                                            this,
                                            R.color.fixable
                                    )
                            )
                        }
                        lettersToggle.isChecked = myPasswordManager.isLetters(genPasswordIdField.text.toString())
                        upperCaseToggle.isChecked = myPasswordManager.isUpperCase(genPasswordIdField.text.toString())
                        numbersToggle.isChecked = myPasswordManager.isNumbers(genPasswordIdField.text.toString())
                        symToggles.isChecked = myPasswordManager.isSymbols(genPasswordIdField.text.toString())
                    }
                    val db2FAIndex = pCursor.getString(aIndex).toString()

                    if (db2FAIndex == "1") {
                        authToggle.isChecked = true
                    }
                    val dbUTIndex = pCursor.getString(uTIndex).toString()
                    if (dbUTIndex == "1") {
                        timeLimit.isChecked = true
                    }
                    val dbDescIndex = pCursor.getString(descIndex).toString()
                    noteField.setText(dbDescIndex)
                    val dbTagsIndex = pCursor.getString(tagsIndex).toString()
                    keyWordsField.setText(dbTagsIndex)

                    val dbEmailIndex = pCursor.getString(loginIndex).toString()
                    if (dbEmailIndex != "") {
                        email.visibility = View.VISIBLE
                        emailSwitch.isChecked = true
                        emailField.setText(dbEmailIndex)
                    }

                } while (pCursor.moveToNext())
                if(lettersToggle.isChecked ){
                    useLetters = true
                    list.add(lettersToggle.text.toString())
                }
                if(upperCaseToggle.isChecked){
                    list.add(upperCaseToggle.text.toString())
                    useUC = true
                }
                if(numbersToggle.isChecked ){
                    list.add(numbersToggle.text.toString())
                    useNumbers = true
                }
                if( symToggles.isChecked ){
                    list.add(symToggles.text.toString())
                    useSymbols = true
                }
            } else {
                helloTextId.text = getString(R.string.no_text)
            }

        } catch (e: SQLException) {
            helloTextId.text = getString(R.string.no_text)
        }




        lengthToggle.setOnClickListener {
            if (seekBar.visibility == View.GONE) {
                seekBar.visibility = View.VISIBLE
            } else {
                seekBar.visibility = View.GONE
            }
        }

        // Set a SeekBar change listener
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                // Display the current progress of SeekBar
                length = i
                lengthToggle.text = getString(R.string.length) + ": " + length
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Do something
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Do something
            }
        })

        // Loop through the chips
        for (index in 0 until passSettings.childCount) {
            val chip: Chip = passSettings.getChildAt(index) as Chip

            // Set the chip checked change listener
            chip.setOnCheckedChangeListener { view, isChecked ->
                val deg = generatePassword.rotation + 30f
                generatePassword.animate().rotation(deg).interpolator = AccelerateDecelerateInterpolator()
                if (isChecked) {
                    if (view.id == R.id.lettersToggle)
                        useLetters = true
                    if (view.id == R.id.symToggles)
                        useSymbols = true
                    if (view.id == R.id.numbersToggle)
                        useNumbers = true
                    if (view.id == R.id.upperCaseToggle)
                        useUC = true
                    list.add(view.text.toString())
                } else {
                    if (view.id == R.id.lettersToggle)
                        useLetters = false
                    if (view.id == R.id.symToggles)
                        useSymbols = false
                    if (view.id == R.id.numbersToggle)
                        useNumbers = false
                    if (view.id == R.id.upperCaseToggle)
                        useUC = false
                    list.remove(view.text.toString())
                }
            }
        }

        genPasswordIdField.addTextChangedListener(object : TextWatcher {
            @SuppressLint("ResourceAsColor")
            override fun afterTextChanged(s: Editable?) {
                if (genPasswordIdField.hasFocus()) {
                    length = s.toString().length
                    lengthToggle.text = getString(R.string.length) + ": " + length
                    seekBar.progress = length
                    val deg = generatePassword.rotation + 10f
                    generatePassword.animate().rotation(deg).interpolator =
                            AccelerateDecelerateInterpolator()
                    val myPasswordManager = PasswordManager()
                    lettersToggle.isChecked =
                            myPasswordManager.isLetters(genPasswordIdField.text.toString())
                    upperCaseToggle.isChecked =
                            myPasswordManager.isUpperCase(genPasswordIdField.text.toString())
                    numbersToggle.isChecked =
                            myPasswordManager.isNumbers(genPasswordIdField.text.toString())
                    symToggles.isChecked =
                            myPasswordManager.isSymbols(genPasswordIdField.text.toString())
                    val evaluation: String =
                            myPasswordManager.evaluatePasswordString(genPasswordIdField.text.toString())
                    passQuality.text = evaluation
                    when (evaluation) {
                        "low" -> passQuality.text = getString(R.string.low)
                        "high" -> passQuality.text = getString(R.string.high)
                        else -> passQuality.text = getString(R.string.medium)
                    }
                    when (evaluation) {
                        "low" -> passQuality.setTextColor(
                                ContextCompat.getColor(
                                        applicationContext,
                                        R.color.negative
                                )
                        )
                        "high" -> passQuality.setTextColor(
                                ContextCompat.getColor(
                                        applicationContext,
                                        R.color.positive
                                )
                        )
                        else -> passQuality.setTextColor(
                                ContextCompat.getColor(
                                        applicationContext,
                                        R.color.fixable
                                )
                        )
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        generatePassword.setOnClickListener {
            val deg = 0f
            generatePassword.animate().rotation(deg).interpolator = AccelerateDecelerateInterpolator()
            genPasswordIdField.clearFocus()
            val myPasswordManager = PasswordManager()
            //Create a password with letters, uppercase letters, numbers but not special chars with 17 chars
            if(list.size == 0 || (list.size == 1 && lengthToggle.isChecked)|| (list.size == 1 && list[0].contains(
                        getString(
                                R.string.length
                        )
                ))){
                genPasswordId.error = getString(R.string.noRules)
            } else {
                genPasswordId.error = null
                val newPassword: String =
                    myPasswordManager.generatePassword(
                            useLetters,
                            useUC,
                            useNumbers,
                            useSymbols,
                            length
                    )
                genPasswordIdField.setText(newPassword)

                val evaluation: String = myPasswordManager.evaluatePasswordString(genPasswordIdField.text.toString())
                when (evaluation) {
                    "low" -> passQuality.text = getString(R.string.low)
                    "high" -> passQuality.text = getString(R.string.high)
                    else -> passQuality.text = getString(R.string.medium)
                }
                when (evaluation) {
                    "low" -> passQuality.setTextColor(
                            ContextCompat.getColor(
                                    applicationContext,
                                    R.color.negative
                            )
                    )
                    "high" -> passQuality.setTextColor(
                            ContextCompat.getColor(
                                    applicationContext,
                                    R.color.positive
                            )
                    )
                    else -> passQuality.setTextColor(
                            ContextCompat.getColor(
                                    applicationContext,
                                    R.color.fixable
                            )
                    )
                }
            }
        }
        generatePassword.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    cardPass.elevation = 50F
                    generatePassword.background = ContextCompat.getDrawable(this, R.color.grey)
                    v.invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    generatePassword.background = ContextCompat.getDrawable(this, R.color.white)
                    cardPass.elevation = 10F
                    v.invalidate()
                }
            }
            false
        }



        genPasswordId.setOnClickListener {
            if (genPasswordIdField.text.toString() != "") {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Password", genPasswordIdField.text.toString())
                clipboard.setPrimaryClip(clip)
                toast(getString(R.string.passCopied))
            }
        }

        genPasswordIdField.setOnClickListener {
            if (genPasswordIdField.text.toString() != "") {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Password", genPasswordIdField.text.toString())
                clipboard.setPrimaryClip(clip)
                toast(getString(R.string.passCopied))
            }
        }

        emailSwitch.setOnClickListener {
            if (emailSwitch.isChecked)
                email.visibility = View.VISIBLE
            else
                email.visibility = View.GONE

        }

        savePass.setOnClickListener {
            val login2 = newNameField.text
            if (login2 != null) {
                if (login2.isEmpty() || login2.length < 2) {
                    newName.error = getString(R.string.errNumOfText)
                }
                else if (genPasswordIdField.text.toString() == "" || genPasswordIdField.text.toString().length < 3){
                    genPasswordId.error = getString(R.string.errPass)
                }
                else {
                    val contentValues = ContentValues()
                    contentValues.put(pdbHelper.KEY_PASS, genPasswordIdField.text.toString())

                    val pm = PasswordManager()

                    if (cryptToggle.isChecked) {
                        val dc = pm.encrypt(genPasswordIdField.text.toString())
                        contentValues.put(
                                pdbHelper.KEY_PASS,
                                dc
                        )
                        contentValues.put(pdbHelper.KEY_CIPHER, "crypted")
                    }
                    else{
                        contentValues.put(pdbHelper.KEY_PASS, genPasswordIdField.text.toString())
                        contentValues.put(pdbHelper.KEY_CIPHER, "none")
                    }




                    contentValues.put(pdbHelper.KEY_NAME, newNameField.text.toString())
                    contentValues.put(pdbHelper.KEY_LOGIN, emailField.text.toString())
                    var keyFA = "0"
                    if (authToggle.isChecked)
                        keyFA = "1"
                    var keyTimeLimit = "0"
                    if (timeLimit.isChecked)
                        keyTimeLimit = "1"
                    contentValues.put(pdbHelper.KEY_2FA, keyFA)
                    contentValues.put(pdbHelper.KEY_USE_TIME, keyTimeLimit)
                    contentValues.put(pdbHelper.KEY_TIME, getDateTime())
                    contentValues.put(pdbHelper.KEY_DESC, noteField.text.toString())
                    contentValues.put(pdbHelper.KEY_TAGS, keyWordsField.text.toString())
                    pDatabase.update(
                            pdbHelper.TABLE_USERS, contentValues,
                            "NAME = ?",
                            arrayOf(dbLogin)
                    )
                    val intent = Intent(this, PasswordViewActivity::class.java)
                    intent.putExtra("login", login)
                    intent.putExtra("passName", newNameField.text.toString())
                    with(sharedPref.edit()) {
                        putString("__PASSNAME", newNameField.text.toString())
                        commit()
                    }
                    pdbHelper.close()
                    setResult(1, intent)

                    val mediaStorageDir = File(
                            applicationContext.getExternalFilesDir("QuickPassPhotos")!!.absolutePath
                    )
                    if (!mediaStorageDir.exists()) {
                        mediaStorageDir.mkdirs()
                        Toast.makeText(applicationContext, "Directory Created", Toast.LENGTH_LONG).show()
                    }

                    if (!mediaStorageDir.exists()) {
                        if (!mediaStorageDir.mkdirs()) {
                            Log.d("App", "failed to create directory")
                        }
                    }

                    if (mediaStorageDir.exists()) {
                        if(imageName != "") {
                            val from = File(mediaStorageDir, "$imageName.jpg")
                            val to = File(mediaStorageDir, "${newNameField.text}.jpg")
                            if (from.exists()) from.renameTo(to)
                        }
                    }

                    finish()
                }
            }

        }

        back.setOnClickListener {
            val intent = Intent()
            intent.putExtra("login", login)
            intent.putExtra("passName", passName)
            setResult(1, intent)
            finish()
        }

        upload.setOnClickListener{
            checkPermissionForImage()
        }



        val mediaStorageDir = File(
                applicationContext.getExternalFilesDir("QuickPassPhotos")!!.absolutePath
        )
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs()
            Toast.makeText(applicationContext, "Directory Created", Toast.LENGTH_LONG).show()
        }

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("App", "failed to create directory")
            }
        }

        val file = File(mediaStorageDir, "$passName.jpg")
        if (file.exists()){
            imageName = passName
            val uri = Uri.fromFile(file)
            attachedImage.setImageURI(uri)
            clearImage.visibility = View.VISIBLE

            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val widthMax: Int = size.x
            val width = (widthMax/1.3).toInt()
            val height = attachedImage.drawable.minimumHeight * width /  attachedImage.drawable.minimumWidth
            attachedImage.layoutParams.height = height
            attachedImage.layoutParams.width = width
            attachedImage.layoutParams.height = height
            attachedImage.layoutParams.width = width

            attachedImage.setOnClickListener {
                val uriForOpen = FileProvider.getUriForFile(
                        this,
                        this.applicationContext.packageName.toString() + ".provider",
                        file
                )
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.setDataAndType(uriForOpen, "image/*")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)
            }
        }

        clearImage.setOnClickListener {
            file.delete()
            attachedImage.setImageURI(null)
        }

    }


    private val PERMISSION_CODE_READ = 1001
    private val PERMISSION_CODE_WRITE = 1002
    private val IMAGE_PICK_CODE = 1000

    private fun checkPermissionForImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            ) {
                val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                val permissionCoarse = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

                requestPermissions(permission, PERMISSION_CODE_READ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_READ LIKE 1001
                requestPermissions(permissionCoarse, PERMISSION_CODE_WRITE) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_WRITE LIKE 1002
            } else {
                pickImageFromGallery()
            }
        }
    }


    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE) // GIVE AN INTEGER VALUE FOR IMAGE_PICK_CODE LIKE 1000
    }

    override fun onKeyUp(keyCode: Int, msg: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                val intent = Intent()
                intent.putExtra("login", login)
                intent.putExtra("passName", passName)
                setResult(1, intent)
                finish()
            }
        }
        return false
    }

    @Throws(IOException::class)
    private fun copyFile(sourceFile: File, destFile: File) {
        if (!sourceFile.exists()) {
            return
        }
        val source: FileChannel? = FileInputStream(sourceFile).channel
        val destination: FileChannel? = FileOutputStream(destFile).channel
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size())
        }
        source?.close()
        destination?.close()
    }

    private fun getImagePath(context: Context, uri: Uri): String? {
        var filePath = ""
        try {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return context.getExternalFilesDir(null).toString() + "/" + split[1]
                    }
                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            java.lang.Long.valueOf(id)
                    )
                    return getDataColumn(context, contentUri, null, null)
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf<String?>(
                            split[1]
                    )
                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {

                // Return the remote address
                return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                        context,
                        uri,
                        null,
                        null
                )
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }

        } catch (e: java.lang.Exception) {
            filePath = ""
        }
        return filePath
    }

    private fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String?>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
                column
        )
        try {
            cursor = context.contentResolver.query(
                    uri!!, projection, selection, selectionArgs,
                    null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }


    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    @SuppressLint("SdCardPath")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (resultCode == 1) {
                recreate()
            }
        }
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            // I'M GETTING THE URI OF THE IMAGE AS DATA AND SETTING IT TO THE IMAGEVIEW
            attachedImage.setImageURI(data?.data)
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            val widthMax: Int = size.x
            val width = (widthMax/1.3).toInt()
            val height = attachedImage.drawable.minimumHeight * width /  attachedImage.drawable.minimumWidth
            attachedImage.layoutParams.height = height
            attachedImage.layoutParams.width = width
            attachedImage.layoutParams.height = height
            attachedImage.layoutParams.width = width
            if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        PackageManager.PERMISSION_GRANTED
                )
            }

            val selectedImageURI: Uri = data?.data!!

            val mediaStorageDir = File(
                    applicationContext.getExternalFilesDir("QuickPassPhotos")!!.absolutePath
            )
            if (!mediaStorageDir.exists()) {
                mediaStorageDir.mkdirs()
                Toast.makeText(applicationContext, "Directory Created", Toast.LENGTH_LONG).show()
            }

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("App", "failed to create directory")
                }
            }

            imageName = passName

            val file = File(mediaStorageDir, "$passName.jpg")

            val resultURI = getImagePath(this, selectedImageURI)
            if (resultURI != null){
                copyFile(File(resultURI), file)
            }

            isImage = true

        }
    }
    private fun Context.toast(message: String)=
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun getDateTime(): String? {
        val dateFormat = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
        )
        val date = Date()
        return dateFormat.format(date)
    }
}