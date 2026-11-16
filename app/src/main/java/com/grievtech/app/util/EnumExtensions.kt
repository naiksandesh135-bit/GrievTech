package com.grievtech.app.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.grievtech.app.R
import com.grievtech.app.data.model.Category
import com.grievtech.app.data.model.ComplaintStatus
import com.grievtech.app.data.model.Priority
import com.grievtech.app.data.model.UserRole

@Composable
fun Category.labelResource(): String {
    return stringResource(this.labelRes)
}

@Composable
fun ComplaintStatus.labelResource(): String {
    return when (this) {
        ComplaintStatus.SUBMITTED -> stringResource(R.string.status_submitted)
        ComplaintStatus.ASSIGNED -> stringResource(R.string.status_assigned)
        ComplaintStatus.IN_PROGRESS -> stringResource(R.string.status_in_progress)
        ComplaintStatus.RESOLVED -> stringResource(R.string.status_resolved)
        ComplaintStatus.CLOSED -> stringResource(R.string.status_closed)
    }
}

@Composable
fun Priority.labelResource(): String {
    return when (this) {
        Priority.NORMAL -> stringResource(R.string.priority_normal)
        Priority.EMERGENCY -> stringResource(R.string.priority_emergency)
    }
}

@Composable
fun UserRole.labelResource(): String {
    return when (this) {
        UserRole.CITIZEN -> stringResource(R.string.role_citizen)
        UserRole.ADMIN -> stringResource(R.string.role_admin)
    }
}
