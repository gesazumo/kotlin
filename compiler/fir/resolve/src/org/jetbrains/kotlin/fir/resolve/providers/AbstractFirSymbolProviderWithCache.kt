/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.providers

import org.jetbrains.kotlin.fir.PrivateForInline
import org.jetbrains.kotlin.fir.symbols.CallableId
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

abstract class AbstractFirSymbolProviderWithCache<C : FirClassLikeSymbol<*>> : FirSymbolProvider() {
    protected val classCache = SymbolProviderCache<ClassId, C>()
    protected val topLevelCallableCache = SymbolProviderCache<CallableId, List<FirCallableSymbol<*>>>()
    protected val packageCache = SymbolProviderCache<FqName, FqName>()
}

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
inline class SymbolProviderCache<K, V : Any> @PrivateForInline constructor(@PrivateForInline val cache: HashMap<K, Any>) {
    @OptIn(PrivateForInline::class)
    constructor() : this(HashMap())

    @PrivateForInline
    object NullReplacer

    @OptIn(PrivateForInline::class)
    inline fun lookupCacheOrCalculate(key: K, crossinline l: (K) -> V?): V? {
        @Suppress("UNCHECKED_CAST")
        return when (val value = cache[key]) {
            null -> {
                val calculated = l(key)
                cache[key] = calculated ?: NullReplacer
                calculated
            }
            NullReplacer -> null
            else -> value as V
        }
    }

    @OptIn(PrivateForInline::class)
    inline fun <T> lookupCacheOrCalculateWithPostCompute(
        key: K, crossinline l: (K) -> Pair<V?, T>, postCompute: (V, T) -> Unit
    ): V? {
        @Suppress("UNCHECKED_CAST")
        return when (val value = cache[key]) {
            null -> {
                val calculated = l(key)
                cache[key] = calculated.first ?: NullReplacer
                calculated.first?.let { postCompute(it, calculated.second) }
                calculated.first
            }
            NullReplacer -> null
            else -> value as V
        }
    }

    @OptIn(PrivateForInline::class)
    operator fun contains(key: K): Boolean = key in cache

    @Suppress("UNCHECKED_CAST")
    @OptIn(PrivateForInline::class)
    operator fun get(key: K): V? = cache[key].takeIf { it !== NullReplacer } as V?

    @OptIn(PrivateForInline::class)
    operator fun set(key: K, value: V) {
        cache[key] = value
    }

    @OptIn(PrivateForInline::class)
    fun remove(key: K) {
        cache.remove(key)
    }
}
