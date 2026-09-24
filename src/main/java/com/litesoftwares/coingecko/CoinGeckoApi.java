package com.litesoftwares.coingecko;

import com.litesoftwares.coingecko.constant.TokenType;
import com.litesoftwares.coingecko.domain.ApiToken;
import com.litesoftwares.coingecko.exception.CoinGeckoApiException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class CoinGeckoApi {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CoinGeckoApi.class);
    private static final String API_BASE_URL_PUBLIC = "https://api.coingecko.com/api/v3/";

    private static final String API_BASE_URL_PRO = "https://pro-api.coingecko.com/api/v3/";

    private OkHttpClient okHttpClient = null;
    private Retrofit retrofit = null;

    public <S> S createService(
            Class<S> serviceClass,
            Long connectionTimeoutSeconds,
            Long readTimeoutSeconds,
            Long writeTimeoutSeconds,
            ApiToken apiToken) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
                .connectTimeout(connectionTimeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(writeTimeoutSeconds, TimeUnit.SECONDS);

        if (apiToken != null) {
            httpClientBuilder.addInterceptor(chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header(apiToken.getType().getHeaderName(), apiToken.getValue())
                        .method(original.method(), original.body())
                        .build();

                return chain.proceed(request);
            });
        }
        httpClientBuilder.addInterceptor(
                new HttpLoggingInterceptor(log::trace).setLevel(HttpLoggingInterceptor.Level.HEADERS));

        okHttpClient = httpClientBuilder.build();

        retrofit = new Retrofit.Builder()
                .baseUrl(
                        apiToken != null && TokenType.PRO.equals(apiToken.getType())
                                ? API_BASE_URL_PRO
                                : API_BASE_URL_PUBLIC)
                .client(okHttpClient)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        return retrofit.create(serviceClass);
    }

    public <T> T executeSync(Call<T> call) {
        try {
            Response<T> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            } else if (response.code() == 429) {
                // When the client gets rate limited the response is a CloudFlare error page,
                // not a regular error body.
                CoinGeckoApiError apiError = new CoinGeckoApiError();
                apiError.setCode(1015);
                apiError.setMessage("Rate limited");
                throw new CoinGeckoApiException(apiError);
            } else {
                try {
                    CoinGeckoApiError apiError = getCoinGeckoApiError(response);
                    apiError.setCode(response.code());
                    throw new CoinGeckoApiException(apiError);
                } catch (IOException e) {
                    throw new CoinGeckoApiException(response.toString(), e);
                }
            }
        } catch (IOException e) {
            throw new CoinGeckoApiException(e);
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        if (okHttpClient != null) {
            okHttpClient.dispatcher().executorService().shutdown();
            okHttpClient.connectionPool().evictAll();
        }
    }

    private CoinGeckoApiError getCoinGeckoApiError(Response<?> response) throws IOException {
        return (CoinGeckoApiError) retrofit.responseBodyConverter(CoinGeckoApiError.class, new Annotation[0])
                .convert(response.errorBody());
    }
}
