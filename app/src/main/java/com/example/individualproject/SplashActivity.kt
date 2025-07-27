package com.example.individualproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.individualproject.repository.UserRepositoryImpl
import com.example.individualproject.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SplashBody()
        }
    }
}

@Composable
fun SplashBody() {
    val context = LocalContext.current
    val activity = context as? Activity
    val repo = remember { UserRepositoryImpl() }
    val viewModel = remember { UserViewModel(repo) }
    val scope = rememberCoroutineScope()
    val firebaseAuth = FirebaseAuth.getInstance()


    DisposableEffect(Unit) {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            scope.launch {
                val user = auth.currentUser
                if (user != null) {
                    val userModel = viewModel.getUserById(user.uid)

                    if (userModel == null) {
                        Toast.makeText(context, "Could not find user data.", Toast.LENGTH_SHORT).show()
                        context.startActivity(Intent(context, LoginActivity::class.java))
                        activity?.finish()
                        return@launch
                    }

                    val intent = if (userModel.role.equals("Admin", ignoreCase = true)) {
                        Intent(context, DashboardActivity::class.java)
                    } else {
                        Intent(context, UserProductViewActivity::class.java)
                    }
                    context.startActivity(intent)
                    activity?.finish()

                } else {
                    context.startActivity(Intent(context, LoginActivity::class.java))
                    activity?.finish()
                }
            }
        }

        firebaseAuth.addAuthStateListener(authStateListener)

        onDispose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .background(color = Color.White)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            CircularProgressIndicator()
        }
    }
}

@Preview
@Composable
fun PreviewSplash() {
    SplashBody()
}