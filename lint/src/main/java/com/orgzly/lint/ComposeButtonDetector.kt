package com.orgzly.lint

import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiMethod
import org.jetbrains.uast.UCallExpression

/**
 * Custom lint detector that identifies usage of Jetpack Compose's Button
 * and suggests using the wrapped OrgzlyButton API instead.
 */
class ComposeButtonDetector : Detector(), SourceCodeScanner {

    companion object {
        private const val COMPOSE_BUTTON_FQN = "androidx.compose.material3.Button"
        private const val COMPOSE_BUTTON_SIMPLE_NAME = "Button"

        val ISSUE = Issue.create(
            id = "UseOrgzlyButton",
            briefDescription = "Use OrgzlyButton instead of Compose Button",
            explanation = """
                Direct usage of Jetpack Compose's `Button` is discouraged in this project. \
                Use the wrapped `OrgzlyButton` API instead to ensure consistent styling, \
                theming, and behavior across the application.
                
                The `OrgzlyButton` provides a standardized interface that aligns with the \
                app's design system and makes it easier to maintain UI consistency.
            """,
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.WARNING,
            implementation = Implementation(
                ComposeButtonDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }

    override fun getApplicableMethodNames(): List<String> {
        return listOf(COMPOSE_BUTTON_SIMPLE_NAME)
    }

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        // Check if this is the Compose Button we're looking for
        if (!context.evaluator.isMemberInClass(method, COMPOSE_BUTTON_FQN)) {
            return
        }

        // Report the issue
        context.report(
            issue = ISSUE,
            scope = node,
            location = context.getNameLocation(node),
            message = "Use `OrgzlyButton` instead of `Button`",
            quickfixData = createQuickFix(node)
        )
    }

    private fun createQuickFix(node: UCallExpression): LintFix {
        val callText = node.asSourceString()

        // Simple replacement: Button -> OrgzlyButton
        val replacementText = callText.replaceFirst("Button", "OrgzlyButton")

        return fix()
            .name("Replace with OrgzlyButton")
            .replace()
            .text(callText)
            .with(replacementText)
            .autoFix()
            .build()
    }
}