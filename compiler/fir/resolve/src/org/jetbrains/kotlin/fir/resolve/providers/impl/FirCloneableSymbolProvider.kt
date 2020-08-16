/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.providers.impl

import org.jetbrains.kotlin.builtins.KotlinBuiltInsNames
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.Visibilities
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.builder.buildRegularClass
import org.jetbrains.kotlin.fir.declarations.builder.buildSimpleFunction
import org.jetbrains.kotlin.fir.declarations.impl.FirDeclarationStatusImpl
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProvider
import org.jetbrains.kotlin.fir.scopes.FirScope
import org.jetbrains.kotlin.fir.scopes.FirScopeProvider
import org.jetbrains.kotlin.fir.symbols.CallableId
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class FirCloneableSymbolProvider(session: FirSession, scopeProvider: FirScopeProvider) : FirSymbolProvider() {
    companion object {
        val CLONEABLE: Name = Name.identifier("Cloneable")
        val CLONEABLE_CLASS_ID: ClassId = ClassId(KotlinBuiltInsNames.BUILT_INS_PACKAGE_FQ_NAME, CLONEABLE)

        val CLONE: Name = Name.identifier("clone")
    }

    private val klass = buildRegularClass {
        resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES
        origin = FirDeclarationOrigin.Library
        this.session = session
        status = FirDeclarationStatusImpl(
            Visibilities.Public,
            Modality.ABSTRACT
        )
        classKind = ClassKind.INTERFACE
        declarations += buildSimpleFunction {
            this.session = session
            resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES
            origin = FirDeclarationOrigin.Library
            returnTypeRef = buildResolvedTypeRef {
                type = session.builtinTypes.anyType.type
            }
            status = FirDeclarationStatusImpl(Visibilities.Protected, Modality.OPEN)
            name = CLONE
            symbol = FirNamedFunctionSymbol(CallableId(CLONEABLE_CLASS_ID, CLONE))
        }
        this.scopeProvider = scopeProvider
        name = CLONEABLE
        symbol = FirRegularClassSymbol(CLONEABLE_CLASS_ID)
    }

    override fun getClassLikeSymbolByFqName(classId: ClassId): FirClassLikeSymbol<*>? {
        return if (classId == CLONEABLE_CLASS_ID) klass.symbol else null
    }

    override fun getTopLevelCallableSymbols(packageFqName: FqName, name: Name): List<FirCallableSymbol<*>> {
        return emptyList()
    }

    override fun getNestedClassifierScope(classId: ClassId): FirScope? {
        return null
    }

    override fun getPackage(fqName: FqName): FqName? {
        return null
    }
}