package com.example.gravityflip

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.SoundEffectConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                // Simple dark mode theme
                colorScheme = darkColorScheme(
                    background = Color(0xFF121212),
                    surface = Color(0xFF1E1E1E),
                    primary = Color(0xFFBB86FC),
                    secondary = Color(0xFF03DAC5)
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GravityApp()
                }
            }
        }
    }
}

@Composable
fun GravityApp() {
    val context = LocalContext.current
    val view = LocalView.current
    var isGravityFlipped by remember { mutableStateOf(false) }

    // Accelerometer reading states
    var accelX by remember { mutableFloatStateOf(0f) }
    var accelY by remember { mutableFloatStateOf(0f) }

    // Initialize accelerometer listener
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null) {
                    // Typical readings: upright -> Y approx 9.8, flat -> Z approx 9.8
                    accelX = event.values[0]
                    accelY = event.values[1]
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF232526), Color(0xFF414345))
                )
            )
    ) {
        var containerSize by remember { mutableStateOf(IntSize.Zero) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { containerSize = it },
            contentAlignment = Alignment.Center
        ) {
            
            // Text Element Physics
            PhysicsBody(
                isFlipped = isGravityFlipped,
                accelX = accelX,
                accelY = accelY,
                containerSize = containerSize,
                modifier = Modifier.offset(y = (-80).dp),
                popDirection = -1f // Fall UP
            ) {
                Text(
                    text = "Hello World",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            // Button Element Physics
            PhysicsBody(
                isFlipped = isGravityFlipped,
                accelX = accelX,
                accelY = accelY,
                containerSize = containerSize,
                modifier = Modifier.offset(y = 40.dp),
                popDirection = -1f
            ) {
                Button(
                    onClick = {
                        if (!isGravityFlipped) {
                            isGravityFlipped = true
                            // Play default click sound
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Activate Gravity",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            
            // Reset Button - Fades in at the bottom
            val resetAlpha by animateFloatAsState(
                targetValue = if (isGravityFlipped) 1f else 0f,
                animationSpec = tween(durationMillis = 1000)
            )
            
            if (resetAlpha > 0f) {
                Button(
                    onClick = {
                        isGravityFlipped = false
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 60.dp)
                        .graphicsLayer { alpha = resetAlpha }
                ) {
                    Text("Restore Gravity", color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun PhysicsBody(
    isFlipped: Boolean,
    accelX: Float,
    accelY: Float,
    containerSize: IntSize,
    modifier: Modifier = Modifier,
    popDirection: Float = -1f,
    content: @Composable () -> Unit
) {
    var elementSize by remember { mutableStateOf(IntSize.Zero) }
    
    // Position, velocity, rotation state
    var posX by remember { mutableFloatStateOf(0f) }
    var posY by remember { mutableFloatStateOf(0f) }
    var velX by remember { mutableFloatStateOf(0f) }
    var velY by remember { mutableFloatStateOf(0f) }
    var rot by remember { mutableFloatStateOf(0f) }
    var rotVel by remember { mutableFloatStateOf(0f) }

    // Use rememberUpdatedState to read latest accelerometer values non-reactively inside the loop
    val currentAccelX by rememberUpdatedState(accelX)
    val currentAccelY by rememberUpdatedState(accelY)

    LaunchedEffect(isFlipped) {
        if (isFlipped) {
            // Give an initial "pop" to simulate anti-gravity releasing
            velY = 25f * popDirection 
            velX = (Random.nextFloat() - 0.5f) * 10f
            rotVel = (Random.nextFloat() - 0.5f) * 15f
            
            while (isActive && containerSize.width > 0 && elementSize.width > 0) {
                // Invert the gravity. Normally accelY is 9.8 (downward).
                // Subtracting accel vectors makes objects fall opposite to physical gravity.
                // Multiplying by a multiplier for "floaty" feel.
                velX += (-currentAccelX) * 0.4f 
                velY += (-currentAccelY) * 0.4f 
                
                // Add tiny friction
                velX *= 0.98f
                velY *= 0.98f
                rotVel *= 0.98f
                
                posX += velX
                posY += velY
                rot += rotVel
                
                // Calculate bounds relative to the initial center position
                val halfWidth = (containerSize.width - elementSize.width) / 2f
                val halfHeight = (containerSize.height - elementSize.height) / 2f
                
                // Bounce logic on edges with dampening (0.6f)
                if (posX < -halfWidth) {
                    posX = -halfWidth
                    velX = -velX * 0.6f
                    rotVel += velY * 0.1f // spin when hitting wall
                } else if (posX > halfWidth) {
                    posX = halfWidth
                    velX = -velX * 0.6f
                    rotVel -= velY * 0.1f
                }
                
                if (posY < -halfHeight) {
                    posY = -halfHeight
                    velY = -velY * 0.6f
                    rotVel -= velX * 0.1f
                } else if (posY > halfHeight) {
                    posY = halfHeight
                    velY = -velY * 0.6f
                    rotVel += velX * 0.1f
                }
                
                delay(16) // roughly 60fps
            }
        } else {
            // Animate properties back to 0 smoothly when reset
            while (isActive && (kotlin.math.abs(posX) > 0.5f || kotlin.math.abs(posY) > 0.5f || kotlin.math.abs(rot) > 0.5f)) {
                posX += (0 - posX) * 0.1f
                posY += (0 - posY) * 0.1f
                rot += (0 - rot) * 0.1f
                velX = 0f
                velY = 0f
                rotVel = 0f
                delay(16)
            }
            posX = 0f
            posY = 0f
            rot = 0f
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = posX
                translationY = posY
                rotationZ = rot
            }
            .onSizeChanged { elementSize = it },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
