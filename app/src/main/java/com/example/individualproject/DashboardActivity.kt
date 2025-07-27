package com.example.individualproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.individualproject.model.ProductModel
import com.example.individualproject.repository.ProductRepositoryImpl
import com.example.individualproject.repository.UserRepositoryImpl
import com.example.individualproject.viewmodel.ProductViewModel
import com.example.individualproject.viewmodel.UserViewModel

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DashboardBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardBody() {
    val productRepo = remember { ProductRepositoryImpl() }
    val productViewModel = remember { ProductViewModel(productRepo) }

    // ✅ Instantiate UserViewModel for logout
    val userRepo = remember { UserRepositoryImpl() }
    val userViewModel = remember { UserViewModel(userRepo) }

    val context = LocalContext.current
    val activity = context as? Activity

    val productsState by productViewModel.allProducts.observeAsState(initial = emptyList())
    val products = productsState.filterNotNull()
    val loading by productViewModel.loading.observeAsState(initial = true)

    LaunchedEffect(Unit) {
        Log.d("Dashboard", "Starting to fetch products...")
        productViewModel.getAllProduct()
    }

    Scaffold(
        // ✅ Add TopAppBar for the title and logout button
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {
                        userViewModel.logout { success, message ->
                            if (success) {
                                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                                context.startActivity(Intent(context, LoginActivity::class.java))
                                activity?.finish()
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    context.startActivity(Intent(context, AddProductActivity::class.java))
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
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No products found. Tap the + button to add one.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products) { eachProduct ->
                        ProductCard(
                            product = eachProduct,
                            onEditClick = {
                                val intent = Intent(context, EditProductActivity::class.java).apply {
                                    putExtra(EditProductActivity.EXTRA_PRODUCT_ID, eachProduct.productId)
                                    putExtra(EditProductActivity.EXTRA_PRODUCT_NAME, eachProduct.productName)
                                    putExtra(EditProductActivity.EXTRA_PRODUCT_PRICE, eachProduct.productPrice)
                                    putExtra(EditProductActivity.EXTRA_PRODUCT_DESCRIPTION, eachProduct.productDesc)
                                    putExtra(EditProductActivity.EXTRA_PRODUCT_IMAGE, eachProduct.productImage)
                                }
                                context.startActivity(intent)
                            },
                            onDeleteClick = {
                                val productId = eachProduct.productId
                                productViewModel.deleteProduct(productId) { success, message ->
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    if (success) {
                                        productViewModel.getAllProduct()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// ProductCard Composable remains the same
@Composable
fun ProductCard(
    product: ProductModel,
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
            Text(
                text = product.productName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Price: $${String.format("%.2f", product.productPrice)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.productDesc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

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