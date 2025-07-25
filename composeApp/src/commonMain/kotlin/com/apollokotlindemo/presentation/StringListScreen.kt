package com.apollokotlindemo.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key.Companion.R
import androidx.compose.ui.input.key.Key.Companion.Ro
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.apollokotlindemo.presentation.components.StringListItem
import org.jetbrains.compose.resources.painterResource

/**
 * Main screen for displaying and managing the strings list
 */
@Composable
fun StringListScreen(
  viewModel: StringsViewModel = remember { StringsViewModel() },
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  StringListContent(
    uiState = uiState,
    onInputTextChange = viewModel::updateInputText,
    onAddString = viewModel::addString,
    onStartEditing = viewModel::startEditing,
    onEditTextChange = viewModel::updateEditText,
    onSaveEdit = viewModel::saveEdit,
    onCancelEdit = viewModel::cancelEdit,
    onDeleteString = viewModel::deleteString,
    onClearError = viewModel::clearError,
    modifier = modifier
  )
}

@Composable
private fun StringListContent(
  uiState: StringsUiState,
  onInputTextChange: (String) -> Unit,
  onAddString: () -> Unit,
  onStartEditing: (Int, String) -> Unit,
  onEditTextChange: (String) -> Unit,
  onSaveEdit: () -> Unit,
  onCancelEdit: () -> Unit,
  onDeleteString: (Int) -> Unit,
  onClearError: () -> Unit,
  modifier: Modifier = Modifier
) {
  MaterialTheme {
    Column(
      modifier = modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Title
      Text(
        text = "Word List",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
      )

      // Error message display
      uiState.errorMessage?.let { error ->
        ErrorCard(
          message = error,
          onDismiss = onClearError
        )
      }

      // Add string input section
      AddStringSection(
        inputText = uiState.inputText,
        onInputTextChange = onInputTextChange,
        onAddString = onAddString,
        isLoading = uiState.isAddingString,
        isInputValid = uiState.isInputValid
      )

      // Main content area
      MainContent(
        uiState = uiState,
        onStartEditing = onStartEditing,
        onEditTextChange = onEditTextChange,
        onSaveEdit = onSaveEdit,
        onCancelEdit = onCancelEdit,
        onDeleteString = onDeleteString
      )
    }
  }
}

@Composable
private fun ErrorCard(
  message: String,
  onDismiss: () -> Unit
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = message,
        modifier = Modifier.weight(1f),
        color = MaterialTheme.colorScheme.onErrorContainer
      )
      TextButton(onClick = onDismiss) {
        Text(
          text = "Dismiss",
          color = MaterialTheme.colorScheme.onErrorContainer
        )
      }
    }
  }
}

@Composable
private fun AddStringSection(
  inputText: String,
  onInputTextChange: (String) -> Unit,
  onAddString: () -> Unit,
  isLoading: Boolean,
  isInputValid: Boolean
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    OutlinedTextField(
      value = inputText,
      onValueChange = onInputTextChange,
      label = { Text("Enter a word") },
      modifier = Modifier.weight(1f),
      enabled = !isLoading
    )

    Button(
      onClick = onAddString,
      enabled = isInputValid && !isLoading,
    ){
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp
          )
        }else{
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add"
          )
        }
        Text(text = "Add")
      }
    }
  }
}

@Composable
private fun MainContent(
  uiState: StringsUiState,
  onStartEditing: (Int, String) -> Unit,
  onEditTextChange: (String) -> Unit,
  onSaveEdit: () -> Unit,
  onCancelEdit: () -> Unit,
  onDeleteString: (Int) -> Unit
) {
  when {
    uiState.isInitialLoading && !uiState.hasStrings -> {
      LoadingIndicator()
    }

    uiState.showEmptyState -> {
      EmptyStateCard()
    }

    else -> {
      StringsList(
        uiState = uiState,
        onStartEditing = onStartEditing,
        onEditTextChange = onEditTextChange,
        onSaveEdit = onSaveEdit,
        onCancelEdit = onCancelEdit,
        onDeleteString = onDeleteString
      )
    }
  }
}

@Composable
private fun LoadingIndicator() {
  Box(
    modifier = Modifier.fillMaxWidth(),
    contentAlignment = Alignment.Center
  ) {
    CircularProgressIndicator()
  }
}

@Composable
private fun EmptyStateCard() {
  Card(
    modifier = Modifier.fillMaxWidth()
  ) {
    Text(
      text = "Please add some words to the empty list",
      modifier = Modifier
        .fillMaxWidth()
        .padding(32.dp),
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
private fun StringsList(
  uiState: StringsUiState,
  onStartEditing: (Int, String) -> Unit,
  onEditTextChange: (String) -> Unit,
  onSaveEdit: () -> Unit,
  onCancelEdit: () -> Unit,
  onDeleteString: (Int) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    itemsIndexed(uiState.strings) { index, string ->
      StringListItem(
        string = string,
        index = index,
        isEditing = uiState.editingIndex == index,
        editText = uiState.editText,
        onEditTextChange = onEditTextChange,
        onEditStart = {
          onStartEditing(index, string)
        },
        onEditSave = onSaveEdit,
        onEditCancel = onCancelEdit,
        onDelete = {
          onDeleteString(index)
        },
        isUpdating = uiState.isUpdatingString && uiState.editingIndex == index,
        isDeleting = uiState.isDeletingString
      )
    }
  }
}