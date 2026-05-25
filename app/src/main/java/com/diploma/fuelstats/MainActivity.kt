package com.diploma.fuelstats

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.diploma.fuelstats.data.notification.RefuelNotifier
import com.diploma.fuelstats.data.obd.ObdTelemetrySampler
import com.diploma.fuelstats.data.obd.RefuelDetector
import com.diploma.fuelstats.databinding.ActivityMainBinding
import com.diploma.fuelstats.di.ServiceLocator
import com.diploma.obd.BluetoothObdClient
import com.diploma.obd.FakeObdClient
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val obd = FakeObdClient()
    // = BluetoothObdClient() - реализация
    private lateinit var telemetrySampler: ObdTelemetrySampler

    private lateinit var refuelNotifier: RefuelNotifier
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(this, "Без разрешения уведомления о заправке не будут приходить", Toast.LENGTH_LONG).show()
            }
        }

    private val clearTelemetryOnStart = false

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ServiceLocator.init(this)

        refuelNotifier = RefuelNotifier(this)
        telemetrySampler = ObdTelemetrySampler(
            obdClient = obd,
            telemetryRepository = ServiceLocator.telemetryRepository,
            externalScope = lifecycleScope,
            refuelDetector = RefuelDetector(),
            onRefuelDetected = { refuelNotifier.showRefuelDetected() }
        )

        requestNotificationPermissionIfNeeded()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        setSupportActionBar(binding.topAppBar)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.refuelsListFragment,
                R.id.statsFragment,
                R.id.carProfileViewFragment
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNav.setupWithNavController(navController)

        startFakeObdSampling()
    }

    private fun startFakeObdSampling() {
        lifecycleScope.launch {
            if (clearTelemetryOnStart) {
                ServiceLocator.telemetryRepository.deleteSamplesForCar(1L)
            }

            val connected = obd.connect("FAKE_OBD")

            if (connected) {
                telemetrySampler.startSampling(carId = 1L)
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        telemetrySampler.stopSampling()
        lifecycleScope.launch {
            obd.disconnect()
        }
        super.onDestroy()
    }
}