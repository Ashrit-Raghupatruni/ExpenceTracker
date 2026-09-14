package com.shakeexpense.app.ui.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shakeexpense.app.data.database.AppDatabase
import com.shakeexpense.app.data.database.entity.CategoryEntity
import com.shakeexpense.app.data.repository.AuthRepository
import com.shakeexpense.app.domain.model.TransactionSource
import com.shakeexpense.app.domain.model.TransactionType
import com.shakeexpense.app.domain.usecase.AddExpenseUseCase
import com.shakeexpense.app.domain.usecase.GetCategoriesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExpenseEntryViewModel(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val addExpenseUseCase: AddExpenseUseCase,
    private val authRepository: AuthRepository? = null
) : ViewModel() {

    private val _state = MutableStateFlow(ExpenseEntryState())
    val state: StateFlow<ExpenseEntryState> = _state.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase().collect { categories ->
                _state.update { current ->
                    val defaultCategory = current.selectedCategory ?: categories.firstOrNull()
                    current.copy(
                        categories = categories,
                        selectedCategory = defaultCategory,
                        isOthersSelected = defaultCategory?.name.equals("Others", ignoreCase = true)
                    )
                }
            }
        }
    }

    fun onCategorySelected(category: CategoryEntity) {
        val isOthers = category.name.equals("Others", ignoreCase = true)
        _state.update {
            it.copy(
                selectedCategory = category,
                isOthersSelected = isOthers
            )
        }
    }

    fun onNoteChanged(note: String) {
        _state.update { it.copy(noteInput = note, customCategoryName = note) }
    }

    fun onCustomCategoryNameChanged(name: String) {
        _state.update { it.copy(customCategoryName = name, noteInput = name) }
    }

    fun onTransactionTypeChanged(type: TransactionType) {
        _state.update { it.copy(transactionType = type) }
    }

    fun prefillTransaction(
        amountCents: Long,
        type: TransactionType,
        categoryId: Long?,
        customName: String?
    ) {
        val amountStr = if (amountCents % 100 == 0L) {
            "${amountCents / 100}"
        } else {
            "%.2f".format(amountCents / 100.0)
        }

        _state.update { current ->
            val matchedCategory = if (categoryId != null) {
                current.categories.firstOrNull { it.id == categoryId }
            } else null ?: current.selectedCategory

            current.copy(
                amountInput = amountStr,
                transactionType = type,
                selectedCategory = matchedCategory,
                noteInput = customName ?: "",
                customCategoryName = customName ?: "",
                isOthersSelected = matchedCategory?.name.equals("Others", ignoreCase = true)
            )
        }
    }

    fun onDigitPressed(digit: String) {
        _state.update { current ->
            val currentInput = current.amountInput
            if (currentInput.contains(".") && currentInput.substringAfter(".").length >= 2) {
                return@update current // Limit to 2 decimal places
            }
            if (currentInput.length >= 8) {
                return@update current // Cap input length
            }
            if (currentInput == "0" && digit != ".") {
                current.copy(amountInput = digit, errorMessage = null)
            } else {
                current.copy(amountInput = currentInput + digit, errorMessage = null)
            }
        }
    }

    fun onDecimalPressed() {
        _state.update { current ->
            val currentInput = current.amountInput
            if (!currentInput.contains(".")) {
                val newInput = if (currentInput.isEmpty()) "0." else "$currentInput."
                current.copy(amountInput = newInput, errorMessage = null)
            } else {
                current
            }
        }
    }

    fun onBackspacePressed() {
        _state.update { current ->
            if (current.amountInput.isNotEmpty()) {
                current.copy(amountInput = current.amountInput.dropLast(1), errorMessage = null)
            } else {
                current
            }
        }
    }

    fun onClearPressed() {
        _state.update { it.copy(amountInput = "", noteInput = "", customCategoryName = "", errorMessage = null) }
    }

    fun saveExpense(
        source: TransactionSource = TransactionSource.MANUAL_SHAKE,
        onSuccess: () -> Unit = {}
    ) {
        val currentState = _state.value
        val category = currentState.selectedCategory ?: run {
            _state.update { it.copy(errorMessage = "Please select a category.") }
            return
        }

        val amountCents = currentState.amountCents
        if (amountCents <= 0) {
            _state.update { it.copy(errorMessage = "Please enter an amount greater than zero.") }
            return
        }

        _state.update { it.copy(isSaving = true, errorMessage = null) }

        viewModelScope.launch {
            val currentUserId = authRepository?.getCurrentProfile()?.userId ?: AppDatabase.DEFAULT_USER_ID
            val note = currentState.noteInput.trim().ifBlank {
                if (currentState.isOthersSelected) currentState.customCategoryName.trim().ifBlank { null } else null
            }
            val result = addExpenseUseCase(
                categoryId = category.id,
                categoryName = category.name,
                categoryColorHex = category.colorHex,
                amountCents = amountCents,
                type = currentState.transactionType,
                source = source,
                customName = note,
                userId = currentUserId
            )

            result.onSuccess {
                _state.update { it.copy(isSaving = false, saveSuccess = true) }
                onSuccess()
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Failed to save expense."
                    )
                }
            }
        }
    }

    fun resetSaveState() {
        _state.update {
            it.copy(
                amountInput = "",
                noteInput = "",
                customCategoryName = "",
                saveSuccess = false,
                errorMessage = null
            )
        }
    }

    companion object {
        fun provideFactory(
            getCategoriesUseCase: GetCategoriesUseCase,
            addExpenseUseCase: AddExpenseUseCase,
            authRepository: AuthRepository? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ExpenseEntryViewModel(getCategoriesUseCase, addExpenseUseCase, authRepository) as T
            }
        }
    }
}
