package com.orgzly.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API

/**
 * Registry for custom Orgzly lint rules.
 */
class OrgzlyIssueRegistry : IssueRegistry() {

    override val issues = listOf(
        ComposeButtonDetector.ISSUE
    )

    override val api: Int = CURRENT_API

    override val minApi: Int = 21

    override val vendor: Vendor = Vendor(
        vendorName = "Orgzly",
        feedbackUrl = "https://github.com/orgzly-revived/orgzly-android-revived/issues",
        contact = "https://github.com/orgzly-revived/orgzly-android-revived"
    )
}