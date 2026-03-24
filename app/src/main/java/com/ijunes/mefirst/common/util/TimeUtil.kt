package com.ijunes.mefirst.common.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toDateString(): String = SimpleDateFormat("EEEE MMM dd, yyyy", Locale.getDefault()).format(Date(this))

fun Long.toTimeString(): String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(this))

