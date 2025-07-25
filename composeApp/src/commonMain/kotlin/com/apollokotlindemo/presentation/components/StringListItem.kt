package com.apollokotlindemo.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Component for displaying a single string item in the list
 */
@Composable
fun StringListItem(
  string: String,
  index: Int,
  isEditing: Boolean,
  editText: String,
  onEditTextChange: (String) -> Unit,
  onEditStart: () -> Unit,
  onEditSave: () -> Unit,
  onEditCancel: () -> Unit,
  onDelete: () -> Unit,
  isUpdating: Boolean = false,
  isDeleting: Boolean = false,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (isEditing) {
        EditingContent(
          editText = editText,
          onEditTextChange = onEditTextChange,
          onEditSave = onEditSave,
          onEditCancel = onEditCancel,
          isUpdating = isUpdating
        )
      } else {
        ViewContent(
          string = string,
          onEditStart = onEditStart,
          onDelete = onDelete,
          isDeleting = isDeleting
        )
      }
    }
  }
}

@Composable
private fun RowScope.EditingContent(
  editText: String,
  onEditTextChange: (String) -> Unit,
  onEditSave: () -> Unit,
  onEditCancel: () -> Unit,
  isUpdating: Boolean
) {
  OutlinedTextField(
    value = editText,
    onValueChange = onEditTextChange,
    modifier = Modifier.weight(1f),
    enabled = !isUpdating,
    singleLine = true
  )

  IconButton(
    onClick = {
      if (editText.isNotBlank()) {
        onEditSave()
      }
    },
    enabled = editText.isNotBlank() && !isUpdating
  ) {
    if (isUpdating) {
      CircularProgressIndicator(
        modifier = Modifier.size(16.dp),
        strokeWidth = 2.dp
      )
    } else {
      Icon(
        imageVector = Icons.Default.Save,
        contentDescription = "Save",
        tint = MaterialTheme.colorScheme.primary
      )
    }
  }

  IconButton(
    onClick = onEditCancel,
    enabled = !isUpdating
  ) {
    Icon(
      imageVector = Icons.Default.Cancel,
      contentDescription = "Cancel",
      tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
private fun RowScope.ViewContent(
  string: String,
  onEditStart: () -> Unit,
  onDelete: () -> Unit,
  isDeleting: Boolean
) {
  Text(
    text = string,
    modifier = Modifier.weight(1f),
    style = MaterialTheme.typography.bodyLarge
  )

  IconButton(
    onClick = onEditStart,
    enabled = !isDeleting
  ) {
    Icon(
      imageVector = Icons.Default.Edit,
      contentDescription = "Edit",
      tint = MaterialTheme.colorScheme.primary
    )
  }

  IconButton(
    onClick = onDelete,
    enabled = !isDeleting
  ) {
    if (isDeleting) {
      CircularProgressIndicator(
        modifier = Modifier.size(16.dp),
        strokeWidth = 2.dp
      )
    } else {
      Icon(
        imageVector = Icons.Default.Delete,
        contentDescription = "Delete",
        tint = MaterialTheme.colorScheme.error
      )
    }
  }
}