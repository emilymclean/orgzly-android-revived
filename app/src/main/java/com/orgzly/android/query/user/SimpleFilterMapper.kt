package com.orgzly.android.query.user

import com.orgzly.android.query.Condition
import com.orgzly.android.query.Options
import com.orgzly.android.query.Query
import com.orgzly.android.query.QueryInterval
import com.orgzly.android.query.QueryTokenizer
import com.orgzly.android.query.Relation
import com.orgzly.android.query.RelativeDateOption
import com.orgzly.android.query.SimpleFilter
import com.orgzly.android.query.SimpleQuery
import com.orgzly.android.query.SimpleFilterBuilder
import com.orgzly.android.query.StateType
import javax.inject.Inject
import kotlin.collections.forEach


class SimpleFilterMapper @Inject constructor() {

    fun fromQuery(query: Query): SimpleQuery {
        val result = SimpleFilterBuilder()
        val flattenedConditions = query.condition?.let { flattenCondition(it) }
        val search = flattenedConditions?.filterIsInstance<Condition.HasText>()
            ?.joinToString(" ") {
                when (it.isQuoted) {
                    true -> "\"${it.text}\""
                    else -> it.text
                }
            } ?: ""

        flattenedConditions?.let {
            rejectIf(it.count { it is Condition.Event } > 1)
            rejectIf(it.count { it is Condition.Scheduled } > 1)
            rejectIf(it.count { it is Condition.Deadline } > 1)
            rejectIf(it.count { it is Condition.Closed } > 1)
            rejectIf(it.count { it is Condition.Created } > 1)
        }

        flattenedConditions?.forEach { c ->
            when (c) {
                is Condition.InBook -> {
                    rejectIf(c.not)
                    result.books += c.name
                }

                is Condition.HasState -> {
                    rejectIf(c.not)
                    result.states += c.state
                }

                is Condition.HasStateType -> {
                    if (c.type == StateType.DONE && c.not) {
                        result.excludeDone = true
                    } else {
                        rejectIf(c.not)
                        result.stateTypes += c.type
                    }
                }

                is Condition.HasPriority -> {
                    rejectIf(c.not)
                    result.priorities += c.priority
                }

                is Condition.HasSetPriority -> {
                    rejectIf(c.not)
                    result.setPriorities += c.priority
                }

                is Condition.HasTag -> {
                    rejectIf(c.not)
                    result.tags += c.tag
                }

                is Condition.HasOwnTag -> {
                    rejectIf(c.not)
                    result.ownTags += c.tag
                }

                is Condition.Event -> {
                    result.event = mapDate(c.interval, c.relation)
                }

                is Condition.Scheduled -> {
                    result.scheduled = mapDate(c.interval, c.relation)
                }

                is Condition.Deadline -> {
                    result.deadline = mapDate(c.interval, c.relation)
                }

                is Condition.Closed -> {
                    result.closed = mapDate(c.interval, c.relation)
                }

                is Condition.Created -> {
                    result.created = mapDate(c.interval, c.relation)
                }

                else -> {
                    throw UnsupportedSimpleFilterException(
                        "Unsupported condition: $c"
                    )
                }
            }
        }

        return SimpleQuery(
            search,
            result.build()
        )
    }

    fun toQuery(search: String, filter: SimpleFilter) = Query(
        Condition.And(
            buildList {
                addAll(
                    filter.books.map {
                        Condition.InBook(it)
                    }
                )

                addAll(
                    filter.states.map {
                        Condition.HasState(it)
                    }
                )

                addAll(
                    filter.stateTypes.map {
                        Condition.HasStateType(it)
                    }
                )

                if (filter.excludeDone) {
                    add(
                        Condition.HasStateType(
                            StateType.DONE,
                            true
                        )
                    )
                }

                addAll(
                    filter.priorities.map {
                        Condition.HasPriority(it)
                    }
                )

                addAll(
                    filter.setPriorities.map {
                        Condition.HasSetPriority(it)
                    }
                )

                addAll(
                    filter.tags.map {
                        Condition.HasTag(it)
                    }
                )

                addAll(
                    filter.ownTags.map {
                        Condition.HasOwnTag(it)
                    }
                )

                filter.event?.let {
                    add(
                        Condition.Event(
                            interval = it.toInterval(),
                            relation = it.toRelation()
                        )
                    )
                }

                filter.scheduled?.let {
                    add(
                        Condition.Scheduled(
                            interval = it.toInterval(),
                            relation = it.toRelation()
                        )
                    )
                }

                filter.deadline?.let {
                    add(
                        Condition.Deadline(
                            interval = it.toInterval(),
                            relation = it.toRelation()
                        )
                    )
                }

                filter.closed?.let {
                    add(
                        Condition.Closed(
                            interval = it.toInterval(),
                            relation = it.toRelation()
                        )
                    )
                }

                filter.created?.let {
                    add(
                        Condition.Created(
                            interval = it.toInterval(),
                            relation = it.toRelation()
                        )
                    )
                }

                search.let {
                    val tokenizer = QueryTokenizer(it, "(", ")")

                    tokenizer.tokens.map {
                        val unquoted = QueryTokenizer.Companion.unquote(it)
                        Condition.HasText(
                            unquoted,
                            unquoted == it
                        )
                    }
                }
            }
        ),
        listOfNotNull(filter.sortOrder),
        Options(
            agendaDays = if (filter.isAgenda) 3 else 0
        )
    )

    private fun flattenCondition(condition: Condition): List<Condition> =
        when (condition) {
            is Condition.And ->
                condition.operands.flatMap(::flattenCondition)

            is Condition.Or ->
                throw UnsupportedSimpleFilterException(
                    "OR conditions are unsupported"
                )

            else ->
                listOf(condition)
        }

    private fun RelativeDateOption.toInterval(): QueryInterval =
        when (this) {
            RelativeDateOption.FUTURE ->
                QueryInterval(QueryInterval.Unit.DAY)

            RelativeDateOption.PAST ->
                QueryInterval(QueryInterval.Unit.DAY)

            RelativeDateOption.TODAY ->
                QueryInterval(QueryInterval.Unit.DAY, 0)

            RelativeDateOption.YESTERDAY ->
                QueryInterval(QueryInterval.Unit.DAY, -1)

            RelativeDateOption.TOMORROW ->
                QueryInterval(QueryInterval.Unit.DAY, 1)

            RelativeDateOption.LAST_7_DAYS ->
                QueryInterval(QueryInterval.Unit.DAY, -7)

            RelativeDateOption.LAST_30_DAYS ->
                QueryInterval(QueryInterval.Unit.DAY, -30)

            RelativeDateOption.NEXT_7_DAYS ->
                QueryInterval(QueryInterval.Unit.DAY, 7)

            RelativeDateOption.NEXT_30_DAYS ->
                QueryInterval(QueryInterval.Unit.DAY, 30)
        }

    private fun RelativeDateOption.toRelation(): Relation =
        when (this) {
            RelativeDateOption.FUTURE ->
                Relation.GE

            RelativeDateOption.PAST ->
                Relation.LT

            RelativeDateOption.TODAY ->
                Relation.EQ

            RelativeDateOption.YESTERDAY ->
                Relation.EQ

            RelativeDateOption.TOMORROW ->
                Relation.EQ

            RelativeDateOption.LAST_7_DAYS,
            RelativeDateOption.LAST_30_DAYS ->
                Relation.GE

            RelativeDateOption.NEXT_7_DAYS,
            RelativeDateOption.NEXT_30_DAYS ->
                Relation.LE
        }

    private fun rejectIf(value: Boolean) {
        if (value) {
            throw UnsupportedSimpleFilterException(
                "NOT conditions are unsupported"
            )
        }
    }
}

private fun mapDate(
    interval: QueryInterval,
    relation: Relation
): RelativeDateOption {

    return when {
        interval.unit == QueryInterval.Unit.DAY &&
                interval.value == 0 &&
                relation == Relation.EQ ->
            RelativeDateOption.TODAY

        interval.unit == QueryInterval.Unit.DAY &&
                interval.value == 1 &&
                relation == Relation.EQ ->
            RelativeDateOption.TOMORROW

        interval.unit == QueryInterval.Unit.DAY &&
                relation == Relation.GE ->
            RelativeDateOption.FUTURE

        interval.unit == QueryInterval.Unit.DAY &&
                relation == Relation.LT ->
            RelativeDateOption.PAST

        relation == Relation.GE &&
                interval.value == 7 &&
                interval.unit == QueryInterval.Unit.WEEK ->
            RelativeDateOption.NEXT_7_DAYS

        relation == Relation.LT &&
                interval.value == 7 &&
                interval.unit == QueryInterval.Unit.WEEK ->
            RelativeDateOption.LAST_7_DAYS

        relation == Relation.GE &&
                interval.value == 1 &&
                interval.unit == QueryInterval.Unit.MONTH ->
            RelativeDateOption.NEXT_30_DAYS

        relation == Relation.LT &&
                interval.value == 1 &&
                interval.unit == QueryInterval.Unit.MONTH ->
            RelativeDateOption.LAST_30_DAYS

        else ->
            throw UnsupportedSimpleFilterException(
                "Unsupported date filter: $relation $interval"
            )
    }
}

class UnsupportedSimpleFilterException(
    message: String
) : RuntimeException(message)