package com.paoapps.fifi.sample.di

import com.paoapps.fifi.sample.model.AppModel
import com.paoapps.fifi.sample.model.AppModelEnvironment
import com.paoapps.fifi.api.ServerErrorParser
import com.paoapps.fifi.api.TokenDecoder
import com.paoapps.fifi.api.domain.ApiResponse
import com.paoapps.fifi.api.domain.Failure
import com.paoapps.fifi.api.domain.FailureKind
import com.paoapps.fifi.auth.AuthApi
import com.paoapps.fifi.auth.Claims
import com.paoapps.fifi.auth.IdentifiableClaims
import com.paoapps.fifi.auth.Tokens
import com.paoapps.fifi.di.initKoinShared
import com.paoapps.fifi.localization.CommonStringsProvider
import com.paoapps.fifi.localization.DefaultLanguageProvider
import com.paoapps.fifi.model.Model
import com.paoapps.fifi.sample.api.Api
import com.paoapps.fifi.sample.domain.ModelData
import com.paoapps.fifi.sample.model.CoffeeModel
import com.paoapps.fifi.sample.model.CoffeeModelImpl
import io.ktor.client.statement.HttpResponse
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoinApp(
    sharedAppModule: Module,
    model: () -> AppModel,
    environment: AppModelEnvironment,
    isDebugMode: Boolean,
    commonStringsProvider: CommonStringsProvider,
    appDeclaration: KoinAppDeclaration = {}
) = initKoinShared(
    "com.paoapps.fifi.sample",
    sharedAppModule,
    model,
    object: TokenDecoder<IdentifiableClaims, Claims> {
        override fun accessTokenClaims(accessToken: String): IdentifiableClaims {
            TODO("Not yet implemented")
        }
        override fun refreshTokenClaims(refreshToken: String): Claims? {
            TODO("Not yet implemented")
        }
        override fun encodeRefreshTokenClaims(claims: Claims): String {
            TODO("Not yet implemented")
        }
        override fun encodeAccessTokenClaims(claims: IdentifiableClaims): String {
            TODO("Not yet implemented")
        }
    },
    { object: AuthApi<Unit> {
        override suspend fun refresh(refreshToken: String): ApiResponse<Tokens, Unit> {
            TODO("Not yet implemented")
        }
    } },
    DefaultLanguageProvider("en"),
    commonStringsProvider,
    object: ServerErrorParser<Unit> {
        override suspend fun parseError(response: HttpResponse): Unit? {
            return null
        }

        override suspend fun <T> toClientFailure(response: HttpResponse, error: Unit?, exception: Exception): Failure<T, Unit> {
            return Failure(response.status.value, response.status.description, FailureKind.CLIENT, exception, error)
        }
    }
) {
    appDeclaration()
    modules(module {
        single { get<Model<ModelData, IdentifiableClaims, AppModelEnvironment, Int, Api>>() as AppModel }

        single<CoffeeModel> { CoffeeModelImpl(get(), get()) }
    }, sharedAppModule)
}

