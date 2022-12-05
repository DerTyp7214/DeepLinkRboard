package de.dertyp7214.deeplinkrboard.screens

import android.app.Activity
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.navigation.ui.AppBarConfiguration
import de.dertyp7214.deeplinkrboard.BuildConfig
import de.dertyp7214.deeplinkrboard.R
import de.dertyp7214.deeplinkrboard.core.content
import de.dertyp7214.deeplinkrboard.core.openDialog
import de.dertyp7214.deeplinkrboard.core.toHumanReadableBytes
import de.dertyp7214.deeplinkrboard.databinding.ActivityMainBinding
import de.dertyp7214.deeplinkrboard.utils.AppStartUp
import de.dertyp7214.deeplinkrboard.utils.PackageUtils
import de.dertyp7214.deeplinkrboard.utils.UpdateHelper
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private val updateUrl by lazy {
        if (BuildConfig.DEBUG){
            "https://github.com/DerTyp7214/DeepLinkRboard/releases/download/latest-debug/app-debug.apk"
        }
        else{
            "https://github.com/DerTyp7214/DeepLinkRboard/releases/download/latest-release/app-release.apk"
        }
    }
    private lateinit var downloadResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        downloadResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    content.setRenderEffect(null)
                }
            }
        AppStartUp(this).apply {
            setUp()
            onCreate { intent ->
                checkUpdate(intent)
            }

        }

        val radioGroup = findViewById<RadioGroup>(R.id.radio_group)
        val input = findViewById<EditText>(R.id.input)
        val highlight = findViewById<EditText>(R.id.highlight)

        val map = listOf(
            "info",
            "settings",
            "flags",
            "all_flags",
            "all_preferences",
            "repos",
            "about",
            "props"
        ).associateWith {
            it.split("_").joinToString(" ") { s -> s.capitalize(Locale.getDefault()) }
        }
        map.forEach {
            radioGroup.addView(RadioButton(this).apply {
                text = it.value
            })
        }

        binding.fab.setOnClickListener {
            radioGroup.checkedRadioButtonId.let {
                val radioButton = radioGroup.findViewById<RadioButton>(it)
                if (radioButton != null) {
                    val jsonArray = JSONArray()
                    val jsonObject = JSONObject()
                    jsonObject.put(
                        "screen",
                        map.map { m -> Pair(m.value, m.key) }.toMap()[radioButton.text]
                    )
                    jsonObject.put("args", JSONObject().apply {
                        input.text.toString().let { s -> if (s.isNotEmpty()) put("input", s) }
                        highlight.text.toString()
                            .let { s -> if (s.isNotEmpty()) put("highlight", s) }
                    })
                    jsonArray.put(jsonObject)
                    val file = File(filesDir, "link.rboard")
                    file.writeText("link=$jsonArray")
                    file.share(
                        this, "application/rboard",
                        Intent.ACTION_SEND, R.string.share
                    )
                }
            }
        }
    }
    private fun checkUpdate(intent: Intent) {

        if (intent.getBooleanExtra(
                "update",
                this@MainActivity.intent.getBooleanExtra("update", false)
            )
        ) {
            openDialog(R.string.update_ready, R.string.update) { update() }
        }
    }

    private fun update() {
        val maxProgress = 100
        val builder =
            NotificationCompat.Builder(this, getString(R.string.download_notification_channel_id))
                .apply {
                    setContentTitle(getString(R.string.update))
                    setContentText(getString(R.string.download_update))
                    setSmallIcon(R.drawable.ic_baseline_get_app_24)
                    priority = NotificationCompat.PRIORITY_LOW
                }
        val manager = NotificationManagerCompat.from(this).apply {
            builder.setProgress(maxProgress, 0, false)
        }
        var finished = false
        UpdateHelper(updateUrl, this).apply {
            addOnProgressListener { progress, bytes, total ->
                if (!finished) {
                    builder
                        .setContentText(
                            getString(
                                R.string.download_update_progress,
                                "${bytes.toHumanReadableBytes(this@MainActivity)}/${
                                    total.toHumanReadableBytes(this@MainActivity)
                                }"
                            )
                        )
                        .setProgress(maxProgress, progress.toInt(), false)
                }
            }
            setFinishListener { path, _ ->
                finished = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    content.setRenderEffect(
                        RenderEffect.createBlurEffect(
                            10F,
                            10F,
                            Shader.TileMode.REPEAT
                        )
                    )
                }
                PackageUtils.install(this@MainActivity, File(path), downloadResultLauncher) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        content.setRenderEffect(null)
                    }
                    Toast.makeText(this@MainActivity, R.string.error, Toast.LENGTH_LONG).show()
                }
            }
            setErrorListener {
                finished = true
                builder.setContentText(getString(R.string.download_error))
                    .setProgress(0, 0, false)
                it?.connectionException?.printStackTrace()
                Log.d("ERROR", it?.serverErrorMessage ?: "NOO")
            }
        }.start()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}

fun File.share(
    activity: Activity,
    type: String = "*/*",
    action: String = Intent.ACTION_SEND,
    shareText: Int = R.string.share
) {
    val uri = FileProvider.getUriForFile(activity, activity.packageName, this)
    ShareCompat.IntentBuilder(activity)
        .setStream(uri)
        .setType(type)
        .intent.setAction(action)
        .setDataAndType(uri, type)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).apply {
            activity.startActivity(
                Intent.createChooser(
                    this,
                    activity.getString(shareText)
                )
            )
        }
}