package com.example.individualproject

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotApplyResult.Success
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.example.individualproject.model.UserModel
import com.example.individualproject.repository.UserRepositoryImpl
import com.example.individualproject.viewmodel.UserViewModel

class RegistrationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Scaffold { innerPadding ->
                RegBody(innerPadding)
            }
        }
    }
}

@Composable
fun RegBody(innerPaddingValues: PaddingValues) {

    val repo = remember { UserRepositoryImpl() }
    val userViewModel = remember { UserViewModel(repo) }

    val context = LocalContext.current
    val activity = context as? Activity

    var firstName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var expandedCountry by remember { mutableStateOf(false) }
    var expandedRole by remember { mutableStateOf(false) }

    var selectedCountry by remember { mutableStateOf("Select Country") }
    var selectedRole by remember { mutableStateOf("Normal") }

    val countries = listOf("Nepal", "India", "China")
    val roles = listOf("Normal", "Admin")

    var countryTextFieldSize by remember { mutableStateOf(Size.Zero) }
    var roleTextFieldSize by remember { mutableStateOf(Size.Zero) }

    Column(
        modifier = Modifier
            .padding(innerPaddingValues)
            .padding(horizontal = 10.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Make it scrollable
            .background(color = Color.White)
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                placeholder = { Text("Firstname") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(10.dp))
            OutlinedTextField(
                value = lastname,
                onValueChange = { lastname = it },
                placeholder = { Text("Lastname") },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("abc@gmail.com") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedCountry,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        countryTextFieldSize = coordinates.size.toSize()
                    }
                    .clickable { expandedCountry = true },
                placeholder = { Text("Select Country") },
                enabled = false,
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = Color.White,
                    disabledTextColor = Color.Black
                ),
                trailingIcon = { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null) }
            )
            DropdownMenu(
                expanded = expandedCountry,
                onDismissRequest = { expandedCountry = false },
                modifier = Modifier.width(with(LocalDensity.current) { countryTextFieldSize.width.toDp() })
            ) {
                countries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedCountry = option
                            expandedCountry = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedRole,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        roleTextFieldSize = coordinates.size.toSize()
                    }
                    .clickable { expandedRole = true },
                placeholder = { Text("Select Role") },
                enabled = false,
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = Color.White,
                    disabledTextColor = Color.Black
                ),
                trailingIcon = { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null) }
            )
            DropdownMenu(
                expanded = expandedRole,
                onDismissRequest = { expandedRole = false },
                modifier = Modifier.width(with(LocalDensity.current) { roleTextFieldSize.width.toDp() })
            ) {
                roles.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedRole = option
                            expandedRole = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("*******") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            userViewModel.register(email,password){
                    success,message,userId ->
                if (success){
                    val userModel = UserModel(
                        userId,
                        email,
                        firstName,
                        lastname,
                        "Male",
                        selectedCountry,
                        selectedRole
                    )
                    userViewModel.addUserToDatabase(userId,userModel){
                            successDb, messageDb ->
                        if (successDb){
                            Toast.makeText(context, "Registration Successful", Toast.LENGTH_LONG).show()
                            activity?.finish()
                        }else{
                            Toast.makeText(context, messageDb, Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    Toast.makeText(context,message, Toast.LENGTH_LONG).show()
                }
            }
        },
            modifier = Modifier.fillMaxWidth()) {
            Text("Register")
        }
    }
}