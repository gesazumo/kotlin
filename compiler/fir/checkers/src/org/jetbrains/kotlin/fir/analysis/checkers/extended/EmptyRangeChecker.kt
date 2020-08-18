/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.extended

import com.intellij.lang.LighterASTNode
import com.intellij.openapi.util.Ref
import org.jetbrains.kotlin.fir.FirFakeSourceElement
import org.jetbrains.kotlin.fir.FirLightSourceElement
import org.jetbrains.kotlin.fir.FirPsiSourceElement
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirBasicExpresionChecker
import org.jetbrains.kotlin.fir.analysis.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirStatement

object EmptyRangeChecker : FirBasicExpresionChecker() {
    override fun check(functionCall: FirStatement, context: CheckerContext, reporter: DiagnosticReporter) {
        if (functionCall.source is FirFakeSourceElement<*>) return
        if (functionCall !is FirFunctionCall) return
        val (left, right) = when (val source = functionCall.source) {
            is FirLightSourceElement -> {
                val arr = Ref<Array<LighterASTNode>>()

                1L to 2L
            }
            is FirPsiSourceElement<*> -> {
                val range = source.psi
                val left = range.children.getOrNull(0)?.text?.toLongOrNull() ?: return
                val right = range.children.getOrNull(2)?.text?.toLongOrNull() ?: return

                left to right
            }
            else -> return
        }


        val needReport = when (functionCall.calleeReference.name.asString()) {
            "rangeTo" -> {
                left > right
            }
            "downTo" -> {
                right > left
            }
            "until" -> {
                left >= right
            }
            else -> false
        }

        if (needReport) {
            reporter.report(functionCall.source, FirErrors.EMPTY_RANGE)

        }
    }

}