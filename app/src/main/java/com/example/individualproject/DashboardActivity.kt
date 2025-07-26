package com.example.individualproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.individualproject.repository.ProductRepositoryImpl
import com.example.individualproject.viewmodel.ProductViewModel

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DashboardBody()
        }
    }
}

@Composable
fun DashboardBody() {
    val repo = remember { ProductRepositoryImpl() }
    val viewModel = remember { ProductViewModel(repo) }

    val context = LocalContext.current
    val activity = context as? Activity

    val products = viewModel.allProducts.observeAsState(initial = emptyList())
    val loading = viewModel.loading.observeAsState(initial = true)

    // Debug logging
    LaunchedEffect(Unit) {
        Log.d("Dashboard", "Starting to fetch products...")
        viewModel.getAllProduct()
    }

    // Log state changes
    LaunchedEffect(loading.value, products.value.size) {
        Log.d("Dashboard", "Loading: ${loading.value}, Products count: ${products.value.size}")
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    try {
                        val intent = Intent(context, AddProductActivity::class.java)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "AddProductActivity not found: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e("Dashboard", "Error opening AddProductActivity", e)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Product",
                    tint = Color.White
                )
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Debug info
            Text(
                text = "Debug Info:",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text("Loading: ${loading.value}")
            Text("Products count: ${products.value.size}")

            Spacer(modifier = Modifier.height(16.dp))

            // Force stop loading button for testing
            Button(
                onClick = {
                    Log.d("Dashboard", "Force refreshing products...")
                    viewModel.getAllProduct()
                }
            ) {
                Text("Refresh Products")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (loading.value) {
                // Show loading indicator
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading products...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "If this takes too long, check your network connection or API",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            } else if (products.value.isEmpty()) {
                // Show empty state
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No products found",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap the + button to add your first product",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                // Show products list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products.value) { eachProduct ->
                        ProductCard(
                            product = eachProduct,
                            onEditClick = {
                                Toast.makeText(context, "Edit feature coming soon!", Toast.LENGTH_SHORT).show()
                            },
                            onDeleteClick = {
                                try {
                                    val productId = getProductId(eachProduct)
                                    if (productId != null) {
                                        viewModel.deleteProduct(productId) { success, message ->
                                            Toast.makeText(
                                                context,
                                                message,
                                                if (success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
                                            ).show()

                                            if (success) {
                                                viewModel.getAllProduct()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Cannot delete: Product ID is null", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Log.e("Dashboard", "Error deleting product", e)
                                    Toast.makeText(context, "Error deleting product: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Any?,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Product Name
            Text(
                text = getProductName(product) ?: "Unknown Product",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Product Price
            Text(
                text = "Price: $${getProductPrice(product) ?: "0.00"}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Product Description
            Text(
                text = getProductDesc(product) ?: "No description available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onEditClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Product"
                    )
                }

                IconButton(
                    onClick = onDeleteClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Product"
                    )
                }
            }
        }
    }
}

// Helper functions with better error handling
private fun getProductId(product: Any?): String? {
    return try {
        when (product) {
            null -> null
            else -> {
                val field = product.javaClass.getDeclaredField("productId")
                field.isAccessible = true
                field.get(product)?.toString()
            }
        }
    } catch (e: Exception) {
        Log.e("Dashboard", "Error getting product ID", e)
        null
    }
}

private fun getProductName(product: Any?): String? {
    return try {
        when (product) {
            null -> "Unknown Product"
            else -> {
                val field = product.javaClass.getDeclaredField("productName")
                field.isAccessible = true
                field.get(product)?.toString() ?: "Unknown Product"
            }
        }
    } catch (e: Exception) {
        Log.e("Dashboard", "Error getting product name", e)
        "Unknown Product"
    }
}

private fun getProductPrice(product: Any?): String? {
    return try {
        when (product) {
            null -> "0.00"
            else -> {
                val field = product.javaClass.getDeclaredField("productPrice")
                field.isAccessible = true
                val price = field.get(product)
                when (price) {
                    is Number -> String.format("%.2f", price.toDouble())
                    else -> price?.toString() ?: "0.00"
                }
            }
        }
    } catch (e: Exception) {
        Log.e("Dashboard", "Error getting product price", e)
        "0.00"
    }
}

private fun getProductDesc(product: Any?): String? {
    return try {
        when (product) {
            null -> "No description available"
            else -> {
                val field = product.javaClass.getDeclaredField("productDesc")
                field.isAccessible = true
                field.get(product)?.toString() ?: "No description available"
            }
        }
    } catch (e: Exception) {
        Log.e("Dashboard", "Error getting product description", e)
        "No description available"
    }
}

@Preview
@Composable
fun PreviewDashboard() {
    DashboardBody()
}