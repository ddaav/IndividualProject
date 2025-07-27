package com.example.individualproject


import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.individualproject.model.ProductModel
import com.example.individualproject.repository.ProductRepositoryImpl
import com.example.individualproject.utils.ImageUtils
import com.example.individualproject.viewmodel.ProductViewModel

class EditProductActivity : ComponentActivity() {
    private lateinit var imageUtils: ImageUtils
    private var selectedImageUri by mutableStateOf<Uri?>(null)
    private var productId: String? = null

    companion object {
        const val EXTRA_PRODUCT_ID = "product_id"
        const val EXTRA_PRODUCT_NAME = "product_name"
        const val EXTRA_PRODUCT_PRICE = "product_price"
        const val EXTRA_PRODUCT_DESCRIPTION = "product_description"
        const val EXTRA_PRODUCT_IMAGE = "product_image"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get product data from intent
        productId = intent.getStringExtra(EXTRA_PRODUCT_ID)
        val productName = intent.getStringExtra(EXTRA_PRODUCT_NAME) ?: ""
        val productPrice = intent.getDoubleExtra(EXTRA_PRODUCT_PRICE, 0.0)
        val productDescription = intent.getStringExtra(EXTRA_PRODUCT_DESCRIPTION) ?: ""
        val productImage = intent.getStringExtra(EXTRA_PRODUCT_IMAGE) ?: ""

        imageUtils = ImageUtils(this, this)
        imageUtils.registerLaunchers { uri ->
            selectedImageUri = uri
            Log.d("EditProduct", "New image selected: $uri")
        }

        setContent {
            EditProductBody(
                productId = productId,
                initialProductName = productName,
                initialProductPrice = productPrice,
                initialProductDescription = productDescription,
                initialProductImage = productImage,
                selectedImageUri = selectedImageUri,
                onPickImage = {
                    Log.d("EditProduct", "Pick image clicked")
                    imageUtils.launchImagePicker()
                },
                onRemoveNewImage = {
                    selectedImageUri = null
                }
            )
        }
    }
}

// Data class for edit form state management
data class EditProductFormState(
    val productName: String = "",
    val productPrice: String = "",
    val productDescription: String = "",
    val isSubmitting: Boolean = false,
    val isDeleting: Boolean = false,
    val nameError: String? = null,
    val priceError: String? = null,
    val descriptionError: String? = null,
    val showDeleteDialog: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductBody(
    productId: String?,
    initialProductName: String,
    initialProductPrice: Double,
    initialProductDescription: String,
    initialProductImage: String,
    selectedImageUri: Uri?,
    onPickImage: () -> Unit,
    onRemoveNewImage: () -> Unit = {}
) {
    var formState by remember {
        mutableStateOf(
            EditProductFormState(
                productName = initialProductName,
                productPrice = if (initialProductPrice > 0) initialProductPrice.toString() else "",
                productDescription = initialProductDescription
            )
        )
    }

    val repo = remember { ProductRepositoryImpl() }
    val viewModel = remember { ProductViewModel(repo) }
    val context = LocalContext.current
    val activity = context as? Activity
    val focusManager = LocalFocusManager.current

    // Validation functions
    fun validateForm(): Boolean {
        var isValid = true
        var newFormState = formState

        if (formState.productName.isBlank()) {
            newFormState = newFormState.copy(nameError = "Product name is required")
            isValid = false
        } else {
            newFormState = newFormState.copy(nameError = null)
        }

        if (formState.productDescription.isBlank()) {
            newFormState = newFormState.copy(descriptionError = "Product description is required")
            isValid = false
        } else {
            newFormState = newFormState.copy(descriptionError = null)
        }

        if (formState.productPrice.isBlank()) {
            newFormState = newFormState.copy(priceError = "Product price is required")
            isValid = false
        } else {
            try {
                val price = formState.productPrice.toDouble()
                if (price <= 0) {
                    newFormState = newFormState.copy(priceError = "Price must be greater than 0")
                    isValid = false
                } else {
                    newFormState = newFormState.copy(priceError = null)
                }
            } catch (e: NumberFormatException) {
                newFormState = newFormState.copy(priceError = "Please enter a valid price")
                isValid = false
            }
        }

        formState = newFormState
        return isValid
    }

    fun handleUpdate() {
        if (productId == null) {
            Toast.makeText(context, "Product ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        if (!validateForm()) return

        if (formState.isSubmitting) return

        formState = formState.copy(isSubmitting = true)
        Log.d("EditProduct", "Starting update process...")

        val price = formState.productPrice.toDouble()

        // If new image is selected, upload it first
        if (selectedImageUri != null) {
            viewModel.uploadImage(context, selectedImageUri) { imageUrl ->
                if (imageUrl != null) {
                    Log.d("EditProduct", "New image uploaded successfully: $imageUrl")

                    val updatedModel = ProductModel(
                        productId = productId,
                        productName = formState.productName.trim(),
                        productPrice = price,
                        productDesc = formState.productDescription.trim(),
                        productImage = imageUrl
                    )

                    viewModel.updateProduct(updatedModel) { success, message ->
                        formState = formState.copy(isSubmitting = false)
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        Log.d("EditProduct", "Update product result: success=$success, message=$message")

                        if (success) {
                            activity?.finish()
                        }
                    }
                } else {
                    formState = formState.copy(isSubmitting = false)
                    Log.e("EditProduct", "Failed to upload new image")
                    Toast.makeText(
                        context,
                        "Failed to upload new image. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            // No new image, update with existing image
            val updatedModel = ProductModel(
                productId = productId,
                productName = formState.productName.trim(),
                productPrice = price,
                productDesc = formState.productDescription.trim(),
                productImage = initialProductImage
            )

            viewModel.updateProduct(updatedModel) { success, message ->
                formState = formState.copy(isSubmitting = false)
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                Log.d("EditProduct", "Update product result: success=$success, message=$message")

                if (success) {
                    activity?.finish()
                }
            }
        }
    }

    fun handleDelete() {
        if (productId == null) {
            Toast.makeText(context, "Product ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        formState = formState.copy(isDeleting = true, showDeleteDialog = false)
        Log.d("EditProduct", "Starting delete process...")

        viewModel.deleteProduct(productId) { success, message ->
            formState = formState.copy(isDeleting = false)
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            Log.d("EditProduct", "Delete product result: success=$success, message=$message")

            if (success) {
                activity?.finish()
            }
        }
    }

    // Delete confirmation dialog
    if (formState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { formState = formState.copy(showDeleteDialog = false) },
            title = { Text("Delete Product") },
            text = { Text("Are you sure you want to delete this product? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { handleDelete() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { formState = formState.copy(showDeleteDialog = false) }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Product",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { formState = formState.copy(showDeleteDialog = true) },
                        enabled = !formState.isSubmitting && !formState.isDeleting
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Product",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Image Selection Section
            EditImageSelectionSection(
                currentImageUrl = initialProductImage,
                selectedImageUri = selectedImageUri,
                onPickImage = onPickImage,
                onRemoveNewImage = onRemoveNewImage
            )

            // Product Name
            EditProductTextField(
                label = "Product Name",
                value = formState.productName,
                onValueChange = { formState = formState.copy(productName = it, nameError = null) },
                placeholder = "Enter product name",
                errorMessage = formState.nameError,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            // Product Description
            EditProductTextField(
                label = "Product Description",
                value = formState.productDescription,
                onValueChange = { formState = formState.copy(productDescription = it, descriptionError = null) },
                placeholder = "Enter product description",
                errorMessage = formState.descriptionError,
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            // Product Price
            EditProductTextField(
                label = "Product Price",
                value = formState.productPrice,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        formState = formState.copy(productPrice = newValue, priceError = null)
                    }
                },
                placeholder = "Enter price (e.g., 29.99)",
                errorMessage = formState.priceError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    handleUpdate()
                }),
                prefix = { Text("$") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Update Button
                Button(
                    onClick = { handleUpdate() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !formState.isSubmitting && !formState.isDeleting,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (formState.isSubmitting) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 8.dp)
                        )
                        Text("Updating Product...")
                    } else {
                        Text(
                            "Update Product",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Delete Button
                OutlinedButton(
                    onClick = { formState = formState.copy(showDeleteDialog = true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !formState.isSubmitting && !formState.isDeleting,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (formState.isDeleting) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 8.dp)
                        )
                        Text("Deleting Product...")
                    } else {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 8.dp)
                        )
                        Text(
                            "Delete Product",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditImageSelectionSection(
    currentImageUrl: String,
    selectedImageUri: Uri?,
    onPickImage: () -> Unit,
    onRemoveNewImage: () -> Unit
) {
    Column {
        Text(
            text = "Product Image",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (selectedImageUri != null) {
            Text(
                text = "New image selected (will replace current image when saved)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onPickImage() }
            ) {
                // Show new selected image or current image
                val imageToShow = selectedImageUri ?: currentImageUrl

                if (imageToShow != null && imageToShow.toString().isNotEmpty()) {
                    AsyncImage(
                        model = imageToShow,
                        contentDescription = "Product Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Show remove button only for newly selected image
                    if (selectedImageUri != null) {
                        IconButton(
                            onClick = onRemoveNewImage,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove New Image",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Change image overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black.copy(alpha = 0.7f)
                            )
                        ) {
                            Text(
                                text = "Tap to change image",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(48.dp)
                                .padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Tap to select image",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditProductTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    errorMessage: String? = null,
    minLines: Int = 1,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    prefix: @Composable (() -> Unit)? = null
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text(placeholder) },
            value = value,
            onValueChange = onValueChange,
            minLines = minLines,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            prefix = prefix,
            isError = errorMessage != null,
            supportingText = errorMessage?.let { { Text(it, color = MaterialTheme.colorScheme.error) } }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditProductBodyPreview() {
    EditProductBody(
        productId = "sample_id",
        initialProductName = "Sample Product",
        initialProductPrice = 29.99,
        initialProductDescription = "This is a sample product description",
        initialProductImage = "",
        selectedImageUri = null,
        onPickImage = {},
        onRemoveNewImage = {}
    )
}