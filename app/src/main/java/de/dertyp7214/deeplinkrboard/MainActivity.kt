package de.dertyp7214.deeplinkrboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import de.dertyp7214.deeplinkrboard.databinding.ActivityMainBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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