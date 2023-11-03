package com.github.truefmartin.photomapper.NewEditTaskActivity

import java.time.LocalDateTime

enum class RecurringState (val displayStr: String) {

    NONE("None") {
        override fun modifyDate(startDate: LocalDateTime): LocalDateTime {
            return startDate
        }
    }, DAILY("Daily") {
        override fun modifyDate(startDate: LocalDateTime): LocalDateTime {
            return startDate.plusDays(1)
        }
    }, WEEKLY("Weekly") {
        override fun modifyDate(startDate: LocalDateTime): LocalDateTime {
            return startDate.plusWeeks(1)
        }
    }, MONTHLY("Monthly") {
        override fun modifyDate(startDate: LocalDateTime): LocalDateTime {
            return startDate.plusMonths(1)
        }
    }, YEARLY("Yearly") {
        override fun modifyDate(startDate: LocalDateTime): LocalDateTime {
            return startDate.plusYears(1)
        }
    };

    abstract fun modifyDate(startDate: LocalDateTime): LocalDateTime
}