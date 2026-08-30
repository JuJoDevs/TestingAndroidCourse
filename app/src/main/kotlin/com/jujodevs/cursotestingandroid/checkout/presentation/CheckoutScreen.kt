package com.jujodevs.cursotestingandroid.checkout.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jujodevs.cursotestingandroid.R
import com.jujodevs.cursotestingandroid.core.presentation.components.MarketTopAppBar
import com.jujodevs.cursotestingandroid.core.presentation.ui.ObserveAsEvents

@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel<CheckoutViewModel>(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.event) { event ->
        when(event) {
            is CheckoutEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            CheckoutEvent.GoBack -> onBack()
        }
    }

    CheckoutContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction
    )
}

@Composable
fun CheckoutContent(
    uiState: CheckoutUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (CheckoutAction) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            MarketTopAppBar(
                title = stringResource(R.string.checkout_title),
                onBack = { onAction(CheckoutAction.GoBack) },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when(uiState) {
                CheckoutUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is CheckoutUiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    ) {
                        Text(uiState.message)
                        Button(
                            onClick = { onAction(CheckoutAction.Retry) }
                        ) {
                            Text(stringResource(R.string.checkout_retry))
                        }
                    }
                }
                is CheckoutUiState.Success -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    ) {
                        Text(
                            text = stringResource(
                                R.string.checkout_order_confirmed,
                                uiState.confirmation.orderId,
                            ),
                        )
                        Text(
                            text = stringResource(
                                R.string.checkout_estimated_time,
                                uiState.confirmation.etaMinutes,
                            ),
                        )
                        Text(
                            text = stringResource(
                                R.string.checkout_price,
                                uiState.confirmation.total,
                            ),
                        )
                    }
                }
                is CheckoutUiState.Idle -> {
                    CheckoutContentIdle(
                        uiState = uiState,
                        onAction = onAction
                    )
                }
            }
        }
    }
}

@Composable
fun CheckoutContentIdle(
    uiState: CheckoutUiState.Idle,
    onAction: (CheckoutAction) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.checkout_total, uiState.summary.finalTotal),
            style = MaterialTheme.typography.titleLarge,
        )
        OutlinedTextField(
            value = uiState.form.name,
            onValueChange = { onAction(CheckoutAction.ChangeName(it)) },
            label = { Text(text = stringResource(R.string.checkout_name_label)) },
            isError = uiState.errors.nameError != null,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.form.address,
            onValueChange = { onAction(CheckoutAction.ChangeAddress(it)) },
            label = { Text(text = stringResource(R.string.checkout_address_label)) },
            isError = uiState.errors.addressError != null,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.form.email,
            onValueChange = { onAction(CheckoutAction.ChangeEmail(it)) },
            label = { Text(text = stringResource(R.string.checkout_email_label)) },
            isError = uiState.errors.emailError != null,
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.isCartEmpty) {
            Text(text = stringResource(R.string.checkout_empty_cart))
        }

        Button(
            onClick = { onAction(CheckoutAction.Confirm) },
            enabled = uiState.canSubmit,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text =
                    stringResource(
                        if (uiState.isSubmitting) {
                            R.string.checkout_processing_payment
                        } else {
                            R.string.checkout_confirm_order
                        },
                    ),
            )
        }
    }
}
