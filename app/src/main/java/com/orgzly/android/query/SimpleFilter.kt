package com.orgzly.android.query

import androidx.compose.runtime.Immutable

data class SimpleQuery(
    val search: String,
    val filter: SimpleFilter,
)

@Immutable
data class SimpleFilter(
    val books: Set<String> = emptySet(),

    val states: Set<String> = emptySet(),
    val stateTypes: Set<StateType> = emptySet(),
    val excludeDone: Boolean = true,

    val priorities: Set<String> = emptySet(),
    val setPriorities: Set<String> = emptySet(),

    val tags: Set<String> = emptySet(),
    val ownTags: Set<String> = emptySet(),

    val event: RelativeDateOption? = null,
    val scheduled: RelativeDateOption? = null,
    val deadline: RelativeDateOption? = null,
    val closed: RelativeDateOption? = null,
    val created: RelativeDateOption? = null,

    val sortOrder: SortOrder? = null,
    val isAgenda: Boolean = true
)

data class SimpleFilterBuilder(
    val books: MutableSet<String> = mutableSetOf(),

    val states: MutableSet<String> = mutableSetOf(),
    val stateTypes: MutableSet<StateType> = mutableSetOf(),
    var excludeDone: Boolean = true,

    val priorities: MutableSet<String> = mutableSetOf(),
    val setPriorities: MutableSet<String> = mutableSetOf(),

    val tags: MutableSet<String> = mutableSetOf(),
    val ownTags: MutableSet<String> = mutableSetOf(),

    var event: RelativeDateOption? = null,
    var scheduled: RelativeDateOption? = null,
    var deadline: RelativeDateOption? = null,
    var closed: RelativeDateOption? = null,
    var created: RelativeDateOption? = null,

    var sortOrder: SortOrder? = null,
    var isAgenda: Boolean = true
) {
    fun build(): SimpleFilter = SimpleFilter(
        books = books.toSet(),
        states = states.toSet(),
        stateTypes = stateTypes.toSet(),
        excludeDone = excludeDone,
        priorities = priorities.toSet(),
        setPriorities = setPriorities.toSet(),
        tags = tags.toSet(),
        ownTags = ownTags.toSet(),
        event = event,
        scheduled = scheduled,
        deadline = deadline,
        closed = closed,
        created = created,
        sortOrder = sortOrder,
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