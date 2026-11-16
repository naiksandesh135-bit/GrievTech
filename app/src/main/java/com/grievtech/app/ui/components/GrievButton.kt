package com.grievtech.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grievtech.app.ui.theme.GrievTechTheme
import com.grievtech.app.ui.theme.Shapes

enum class GrievButtonType {
    PRIMARY,
    SECONDARY,
    OUTLINED
}

@Composable
fun GrievButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: GrievButtonType = GrievButtonType.PRIMARY,
    enabled: Boolean = true
) {
    val shape = Shapes.medium
    val heightModifier = modifier.height(52.dp)

    when (type) {
        GrievButtonType.PRIMARY -> {
            Button(
                onClick = onClick,
                modifier = heightModifier,
                enabled = enabled,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
        }
        GrievButtonType.SECONDARY -> {
            Button(
                onClick = onClick,
                modifier = heightModifier,
                enabled = enabled,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
        }
        GrievButtonType.OUTLINED -> {
            OutlinedButton(
                onClick = onClick,
                modifier = heightModifier,
                enabled = enabled,
                shape = shape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Preview(name = "Light Mode - Primary", showBackground = true)
@Composable
fun GrievButtonPrimaryPreview() {
    GrievTechTheme(darkTheme = false) {
        GrievButton(
            text = "Submit Complaint",
            onClick = {},
            type = GrievButtonType.PRIMARY,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Dark Mode - Primary", showBackground = true)
@Composable
fun GrievButtonPrimaryDarkPreview() {
    GrievTechTheme(darkTheme = true) {
        GrievButton(
            text = "Submit Complaint",
            onClick = {},
            type = GrievButtonType.PRIMARY,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Light Mode - Secondary", showBackground = true)
@Composable
fun GrievButtonSecondaryPreview() {
    GrievTechTheme(darkTheme = false) {
        GrievButton(
            text = "Track Status",
            onClick = {},
            type = GrievButtonType.SECONDARY,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Light Mode - Outlined", showBackground = true)
@Composable
fun GrievButtonOutlinedPreview() {
    GrievTechTheme(darkTheme = false) {
        GrievButton(
            text = "Cancel",
            onClick = {},
            type = GrievButtonType.OUTLINED,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
