package com.futabooo.android.booklife.data.network

import kotlinx.serialization.json.JsonObject
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * All bookmeter.com endpoints. HTML endpoints return [ResponseBody] (scraped with Jsoup, see
 * `HtmlParsers`), JSON endpoints return [JsonObject] whose `resources` array is decoded by the
 * repositories. Base URL: https://bookmeter.com
 */
interface BookmeterApi {

    // ---- Login ---------------------------------------------------------------------------

    @GET("/login")
    suspend fun loginPage(): ResponseBody

    @FormUrlEncoded
    @POST("/login")
    suspend fun login(
        @Field("session[email_address]") email: String,
        @Field("session[password]") password: String,
        @Field("authenticity_token") authenticityToken: String,
        @Field("session[keep]") keep: String = "1",
    ): ResponseBody

    // ---- Home ----------------------------------------------------------------------------

    @GET("/home")
    suspend fun home(): ResponseBody

    /** Same page as [home] but keeps the HTTP response (needed to inspect the final URL). */
    @GET("/home")
    suspend fun homeResponse(): Response<ResponseBody>

    @GET("/home.json")
    suspend fun homeJson(
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): JsonObject

    // ---- Book list -----------------------------------------------------------------------

    @GET("/users/{user_id}/books/{book_list_menu}")
    suspend fun bookList(
        @Path("user_id") userId: Int,
        @Path("book_list_menu") bookListMenu: String,
    ): ResponseBody

    @GET("/users/{user_id}/books/{book_list_menu}.json")
    suspend fun bookListJson(
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("user_id") userId: Int,
        @Path("book_list_menu") bookListMenu: String,
        @Query("attach_review") attachReview: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): JsonObject

    // ---- Book detail ---------------------------------------------------------------------

    @GET("/books/{book_id}")
    suspend fun bookDetail(@Path("book_id") bookId: Int): ResponseBody

    @GET("/books/{book_id}.json")
    suspend fun bookDetailJson(
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("book_id") bookId: Int,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): JsonObject

    @GET("/books/{book_id}/reviews.json")
    suspend fun bookReviewsJson(
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("book_id") bookId: Int,
        @Query("review_filter") filter: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): JsonObject

    // ---- Search --------------------------------------------------------------------------

    @GET("/search")
    suspend fun search(@Query("keyword") keyword: String): ResponseBody

    @GET("/search.json")
    suspend fun searchJson(
        @Header("X-CSRF-Token") csrfToken: String,
        @Query("keyword") keyword: String,
        @Query("sort") sort: String,
        @Query("type") type: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int,
    ): JsonObject

    // ---- Actions -------------------------------------------------------------------------

    @FormUrlEncoded
    @POST("/users/{user_id}/books/{book_list_menu}")
    suspend fun addBook(
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("user_id") userId: Int,
        @Path("book_list_menu") bookListMenu: String,
        @Field("book[book_id]") bookId: Int,
    ): JsonObject

    @FormUrlEncoded
    @POST("/users/{user_id}/books/read.json")
    suspend fun addReadBook(
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("user_id") userId: Int,
        @Field("read_book[book_id]") bookId: Int,
        @Field("read_book[read_at]") readAt: String,
        @Field("read_book[review]") review: String,
        @Field("read_book[review_is_netabare]") netabare: Int,
    ): JsonObject

    @FormUrlEncoded
    @PUT("/read_books/{id}.json")
    suspend fun updateReadBook(
        @Header("X-CSRF-Token") csrfToken: String,
        @Path("id") id: Int,
        @Field("read_book[book_id]") bookId: Int,
        @Field("read_book[read_at]") readAt: String,
        @Field("read_book[review]") review: String,
        @Field("read_book[review_is_netabare]") netabare: Int,
    )
}
