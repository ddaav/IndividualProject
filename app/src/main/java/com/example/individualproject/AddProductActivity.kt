package com.example.individualproject

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.individualproject.model.ProductModel
import com.example.individualproject.repository.ProductRepositoryImpl
import com.example.individualproject.utils.ImageUtils
import com.example.individualproject.viewmodel.ProductViewModel

class AddProductActivity : ComponentActivity() {
    private lateinit var imageUtils: ImageUtils
    private var selectedImageUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        imageUtils = ImageUtils(this, this)
        imageUtils.registerLaunchers { uri ->
            selectedImageUri = uri
            Log.d("AddProduct", "Image selected: $uri")
        }

        setContent {
            AddProductBody(
                selectedImageUri = selectedImageUri,
                onPickImage = {
                    Log.d("AddProduct", "Pick image clicked")
                    imageUtils.launchImagePicker()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductBody(
    selectedImageUri: Uri?,
    onPickImage: () -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var productDescription by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val repo = remember { ProductRepositoryImpl() }
    val viewModel = remember { ProductViewModel(repo) }

    val context = LocalContext.current
    val activity = context as? Activity

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Product",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Image Selection Section
                Text(
                    text = "Product Image",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            2.dp,
                            if (selectedImageUri != null) MaterialTheme.colorScheme.primary
                            else Color.Gray.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onPickImage() }
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Product Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Tap to select image",
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            item {
                // Product Name
                Text(
                    text = "Product Name",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Enter product name") },
                    value = productName,
                    onValueChange = { productName = it }
                )
            }

            item {
                // Product Description
                Text(
                    text = "Product Description",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Enter product description") },
                    value = productDescription,
                    onValueChange = { productDescription = it },
                    minLines = 3,
                    maxLines = 5
                )
            }

            item {
                // Product Price
                Text(
                    text = "Product Price",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = { Text("Enter price (e.g., 29.99)") },
                    value = productPrice,
                    onValueChange = {
                        // Only allow numbers and decimal point
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            productPrice = it
                        }
                    },
                    prefix = { Text("$") }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = {
                        // Basic validation
                        if (productName.isBlank()) {
                            Toast.makeText(context, "Please enter product name", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (productDescription.isBlank()) {
                            Toast.makeText(context, "Please enter product description", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (productPrice.isBlank()) {
                            Toast.makeText(context, "Please enter product price", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val price = try {
                            productPrice.toDouble()
                        } catch (e: NumberFormatException) {
                            Toast.makeText(context, "Please enter a valid price", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (price <= 0) {
                            Toast.makeText(context, "Price must be greater than 0", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (selectedImageUri != null && !isSubmitting) {
                            isSubmitting = true
                            Log.d("AddProduct", "Starting upload process...")

                            viewModel.uploadImage(context, selectedImageUri) { imageUrl ->
                                if (imageUrl != null) {
                                    Log.d("AddProduct", "Image uploaded successfully: $imageUrl")

                                    val model = ProductModel(
                                        productName = productName.trim(), // Let backend generate ID
                                        productPrice = price,
                                        productDesc = productDescription.trim(),
                                        productImage = imageUrl
                                    )

                                    viewModel.addProduct(model) { success, message ->
                                        isSubmitting = false
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                        Log.d("AddProduct", "Add product result: success=$success, message=$message")

                                        if (success) {
                                            activity?.finish()
                                        }
                                    }
                                } else {
                                    isSubmitting = false
                                    Log.e("AddProduct", "Failed to upload image")
                                    Toast.makeText(
                                        context,
                                        "Failed to upload image. Please try again.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        } else if (selectedImageUri == null) {
                            Toast.makeText(
                                context,
                                "Please select an image first",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 8.dp)
                        )
                        Text("Adding Product...")
                    } else {
                        Text(
                            "Add Product",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddProductBodyPreview() {
    AddProductBody(
        selectedImageUri = null,
        onPickImage = {}
    )
}