package com.jujodevs.cursotestingandroid.checkout.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jujodevs.cursotestingandroid.cart.domain.usecase.GetCartSummaryUseCase
import com.jujodevs.cursotestingandroid.checkout.domain.usecase.IsCartEmptyUseCase
import com.jujodevs.cursotestingandroid.checkout.domain.usecase.PlaceOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val placeOrderUseCase: PlaceOrderUseCase,
    getCartSummaryUseCase: GetCartSummaryUseCase,
    isCartEmptyUseCase: IsCartEmptyUseCase,
): ViewModel() {

    private val formState = MutableStateFlow(CheckoutForm())

    private val submission = MutableStateFlow<Submission>(Submission.Idle)

    private val _events = MutableSharedFlow<CheckoutEvent>(extraBufferCapacity = 1)
    val event: SharedFlow<CheckoutEvent> = _events.asSharedFlow()

    val uiState: StateFlow<CheckoutUiState> = combine(
        getCartSummaryUseCase(), formState, submission
    ) { summary, form, submission ->
        when(submission) {
            is Submission.Success -> CheckoutUiState.Success(submission.confirmation)
            is Submission.Failed -> CheckoutUiState.Error(submission.message)
            Submission.Idle, Submission.Submitting -> {
                val errors = form.validate()
                val isCartEmpty = isCartEmptyUseCase()
                val isSubmitting = submission == Submission.Submitting
                CheckoutUiState.Idle(
                    summary = summary,
                    form = form,
                    errors = errors,
                    isCartEmpty = isCartEmpty,
                    isSubmitting = isSubmitting,
                    canSubmit = !isCartEmpty && !isSubmitting && errors.isValid
                )
            }
        }
    }.catch { e ->
        _events.tryEmit(CheckoutEvent.ShowMessage(e.message.orEmpty()))
        emit(CheckoutUiState.Error(e.message.orEmpty()))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CheckoutUiState.Loading,
    )
}
