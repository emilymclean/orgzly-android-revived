package com.orgzly.android.query

import androidx.compose.runtime.Immutable

data class SimpleQuery(
    val search: String,
    val filter: SimpleFilter,
)

@Immutable
data class SimpleFilter(
    val books: Set<String> = emptySet(),

    val excludeDone: Boolean = false,
    val state: String? = null,

    val priority: String? = null,

    val tags: Set<String> = emptySet(),

    val event: RelativeDateOption? = null,
    val scheduled: RelativeDateOption? = null,
    val deadline: RelativeDateOption? = null,
    val closed: RelativeDateOption? = null,
    val created: RelativeDateOption? = null,

    val sortOrder: SimpleSortOrder = SimpleSortOrder.DEFAULT,
    val sortDescending: Boolean = true,

    val isAgenda: Boolean = false
)

data class SimpleFilterBuilder(
    val books: MutableSet<String> = mutableSetOf(),

    var excludeDone: Boolean = false,
    var state: String? = null,

    var priority: String? = null,

    val tags: MutableSet<String> = mutableSetOf(),

    var event: RelativeDateOption? = null,
    var scheduled: RelativeDateOption? = null,
    var deadline: RelativeDateOption? = null,
    var closed: RelativeDateOption? = null,
    var created: RelativeDateOption? = null,

    var sortOrder: SimpleSortOrder = SimpleSortOrder.DEFAULT,
    var sortDescending: Boolean = true,

    var isAgenda: Boolean = false
) {
    fun build(): SimpleFilter = SimpleFilter(
        books = books.toSet(),
        excludeDone = excludeDone,
        state = state,
        priority = priority,
        tags = tags.toSet(),
        event = event,
        scheduled = scheduled,
        deadline = deadline,
        closed = closed,
        created = created,
        sortOrder = sortOrder,
        sortDescending = sortDescending,
        isAgenda = isAgenda
    )
}

enum class RelativeDateOption {
    FUTURE,

    TODAY,
    TOMORROW,

    PAST,

    YESTERDAY,

    LAST_7_DAYS,
    LAST_30_DAYS,

    NEXT_7_DAYS,
    NEXT_30_DAYS
}

enum class SimpleSortOrder {
    DEFAULT,
    BOOK,
    TITLE,
    SCHEDULED,
    DEADLINE,
    EVENT,
    CLOSED,
    CREATED,
    PRIORITY,
    STATE
}