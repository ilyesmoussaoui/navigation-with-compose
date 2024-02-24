
package com.example.lunchtray.ui

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import com.example.lunchtray.model.MenuItem
import com.example.lunchtray.model.MenuItem.AccompanimentItem
import com.example.lunchtray.model.MenuItem.EntreeItem
import com.example.lunchtray.model.MenuItem.SideDishItem
import com.example.lunchtray.model.OrderUiState
import com.ilyes.navigationincompose.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.NumberFormat

class OrderViewModel : ViewModel() {

    private val taxRate = 0.08

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    fun updateEntree(entree: EntreeItem) {
        val previousEntree = _uiState.value.entree
        updateItem(entree, previousEntree)
    }

    fun updateSideDish(sideDish: SideDishItem) {
        val previousSideDish = _uiState.value.sideDish
        updateItem(sideDish, previousSideDish)
    }

    fun updateAccompaniment(accompaniment: AccompanimentItem) {
        val previousAccompaniment = _uiState.value.accompaniment
        updateItem(accompaniment, previousAccompaniment)
    }

    fun resetOrder() {
        _uiState.value = OrderUiState()
    }

    private fun updateItem(newItem: MenuItem, previousItem: MenuItem?) {
        _uiState.update { currentState ->
            val previousItemPrice = previousItem?.price ?: 0.0

            val itemTotalPrice = currentState.itemTotalPrice - previousItemPrice + newItem.price

            val tax = itemTotalPrice * taxRate
            currentState.copy(
                itemTotalPrice = itemTotalPrice,
                orderTax = tax,
                orderTotalPrice = itemTotalPrice + tax,
                entree = if (newItem is EntreeItem) newItem else currentState.entree,
                sideDish = if (newItem is SideDishItem) newItem else currentState.sideDish,
                accompaniment =
                    if (newItem is AccompanimentItem) newItem else currentState.accompaniment
            )
        }
    }
}

fun Double.formatPrice(): String {
    return NumberFormat.getCurrencyInstance().format(this)
}
 fun submitOrderAndShare(
    navController: NavHostController,
    orderUiState: OrderUiState
) {
    val subject = "Order Summary"
    val summary = buildOrderSummary(orderUiState)
    shareOrder(navController.context, subject, summary)
}
fun buildOrderSummary(orderUiState: OrderUiState): String {
    val entreeSummary = buildItemSummary("Entree", orderUiState.entree)
    val sideDishSummary = buildItemSummary("Side Dish", orderUiState.sideDish)
    val accompanimentSummary = buildItemSummary("Accompaniment", orderUiState.accompaniment)

    val subtotal = "Subtotal: ${orderUiState.itemTotalPrice.formatPrice()}\n"
    val tax = "Tax: ${orderUiState.orderTax.formatPrice()}\n"
    val total = "Total: ${orderUiState.orderTotalPrice.formatPrice()}\n"

    return """
        $entreeSummary
        $sideDishSummary
        $accompanimentSummary
        $subtotal
        $tax
        $total
    """.trimIndent()
}

fun buildItemSummary(itemType: String, item: MenuItem?): String {
    return if (item != null) {
        "$itemType: ${item.name} - ${item.getFormattedPrice()}\n"
    } else {
        ""
    }
}
 fun shareOrder(context: Context, subject: String, summary: String) {
    // Create an ACTION_SEND implicit intent with order details in the intent extras
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, summary)
    }
    context.startActivity(
        Intent.createChooser(
            intent,
            context.getString(R.string.new_lunch_tray_order)
        )
    )
}
