package com.topenclaw.noteshade.ui.components

import java.text.DateFormat
import java.util.Date

fun formatTimestamp(timestamp: Long): String = DateFormat.getDateTimeInstance(
    DateFormat.MEDIUM,
    DateFormat.SHORT,
).format(Date(timestamp))
