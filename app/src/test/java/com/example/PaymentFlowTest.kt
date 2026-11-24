package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.ui.screens.OrderPaymentFlow
import com.example.ui.screens.WalletTopUpFlow
import com.example.ui.theme.QuickKartTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Payment-flow guarantees: money / orders are only processed after a successful payment.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp-xxhdpi")
class PaymentFlowTest {

    @get:Rule
    val compose = createComposeRule()

    private fun waitForProcessing() {
        // Processing step simulates a ~2s gateway round-trip.
        compose.mainClock.advanceTimeBy(2_500)
        compose.waitForIdle()
    }

    @Test
    fun orderPayment_failedUpi_doesNotPlaceOrder_andOffersRetry() {
        var placedWithNote: String? = null
        compose.setContent {
            QuickKartTheme {
                OrderPaymentFlow(
                    amountInr = 188,
                    preferCard = false,
                    onDismiss = {},
                    onPaymentSuccess = { note -> placedWithNote = note }
                )
            }
        }
        compose.onNodeWithTag("wallet_top_up_upi_id_input").performScrollTo().performTextInput("fail@okaxis")
        compose.onNodeWithTag("wallet_top_up_pay_button").performScrollTo().performClick()
        waitForProcessing()

        assertNull("Order must not be placed when payment fails", placedWithNote)
        compose.onNodeWithTag("wallet_top_up_failed").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Retry Payment").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun orderPayment_successfulUpi_placesOrderWithPaidViaNote() {
        var placedWithNote: String? = null
        compose.setContent {
            QuickKartTheme {
                OrderPaymentFlow(
                    amountInr = 188,
                    preferCard = false,
                    onDismiss = {},
                    onPaymentSuccess = { note -> placedWithNote = note }
                )
            }
        }
        compose.onNodeWithTag("wallet_top_up_pay_button").performScrollTo().performClick() // GPay preselected
        waitForProcessing()

        assertEquals("Paid via UPI • GPay", placedWithNote)
    }

    @Test
    fun walletTopUp_creditsOnlyAfterSuccess_withMethodNote() {
        val credits = mutableListOf<Pair<Int, String>>()
        compose.setContent {
            QuickKartTheme {
                WalletTopUpFlow(
                    currentBalanceInr = 100,
                    presetAmountInr = 200,
                    onDismiss = {},
                    onPaymentSuccess = { amount, note -> credits += amount to note }
                )
            }
        }
        compose.onNodeWithTag("wallet_top_up_method_netbanking").performScrollTo().performClick()
        assertTrue("Nothing is credited before paying", credits.isEmpty())

        compose.onNodeWithTag("wallet_top_up_pay_button").performScrollTo().performClick()
        waitForProcessing()

        assertEquals(listOf(200 to "Added via Netbanking • HDFC Bank"), credits)
        compose.onNodeWithTag("wallet_top_up_success").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun walletTopUp_invalidUpiId_isRejectedBeforePayment() {
        val credits = mutableListOf<Pair<Int, String>>()
        compose.setContent {
            QuickKartTheme {
                WalletTopUpFlow(
                    currentBalanceInr = 100,
                    presetAmountInr = 200,
                    onDismiss = {},
                    onPaymentSuccess = { amount, note -> credits += amount to note }
                )
            }
        }
        compose.onNodeWithTag("wallet_top_up_upi_id_input").performScrollTo().performTextInput("not-a-upi-id")
        compose.onNodeWithTag("wallet_top_up_pay_button").performScrollTo().performClick()
        compose.waitForIdle()

        compose.onNodeWithText("Enter a valid UPI ID, e.g. name@okhdfcbank.").performScrollTo().assertIsDisplayed()
        assertTrue(credits.isEmpty())
    }
}
