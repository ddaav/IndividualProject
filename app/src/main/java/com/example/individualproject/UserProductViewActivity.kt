package com.example.individualproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.individualproject.model.CartModel
import com.example.individualproject.model.ProductModel
import com.example.individualproject.repository.ProductRepositoryImpl
import com.example.individualproject.repository.UserRepositoryImpl
import com.example.individualproject.repository.cart.CartRepositoryImpl
import com.example.individualproject.viewmodel.CartViewModel
import com.example.individualproject.viewmodel.ProductViewModel
import com.example.individualproject.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class UserProductViewActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UserProductViewBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProductViewBody() {
    val context = LocalContext.current
    val activity = context as? Activity
    val productRepo = remember { ProductRepositoryImpl() }
    val productViewModel = remember { ProductViewModel(productRepo) }

    val cartRepo = remember { CartRepositoryImpl() }
    val cartViewModel = remember { CartViewModel(cartRepo) }

    // ✅ Instantiate UserViewModel for logout
    val userRepo = remember { UserRepositoryImpl() }
    val userViewModel = remember { UserViewModel(userRepo) }


    val productsState by productViewModel.allProducts.observeAsState(initial = emptyList())
    val products = productsState.filterNotNull()
    val loading by productViewModel.loading.observeAsState(initial = true)

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(Unit) {
        productViewModel.getAllProduct()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Browse Products", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                // ✅ Add logout action
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
                }
            )
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
                    Text("No products available at the moment.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(products) { product ->
                        UserProductCard(
                            product = product,
                            onAddToCart = {
                                if (currentUserId != null) {
                                    val cartItem = CartModel(
                                        productId = product.productId,
                                        productName = product.productName,
                                        productPrice = product.productPrice,
                                        productImage = product.productImage,
                                        quantity = 1
                                    )
                                    cartViewModel.addToCart(currentUserId, cartItem) { success, message ->
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "You must be logged in to add items.", Toast.LENGTH_LONG).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
// UserProductCard Composable remains the same
@Composable
fun UserProductCard(product: ProductModel, onAddToCart: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            AsyncImage(
                model = product.productImage,
                contentDescription = product.productName,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = product.productName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.productDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$${String.format("%.2f", product.productPrice)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(onClick = onAddToCart) {
                        Text("Add to Cart")
                    }
                }
            }
        }
    }
}