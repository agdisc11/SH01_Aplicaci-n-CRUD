package com.upchiapas.mitiendita.products.presentation

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Formatos de presentación (moneda y fecha en español de México). */
private val MX_LOCALE: Locale = Locale.forLanguageTag("es-MX")
private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", MX_LOCALE)

fun Double.toCurrency(): String = NumberFormat.getCurrencyInstance(MX_LOCALE).format(this)

fun LocalDate.toDisplayDate(): String = format(DATE_FORMATTER)
