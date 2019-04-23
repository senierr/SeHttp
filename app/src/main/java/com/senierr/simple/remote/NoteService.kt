package com.senierr.simple.remote

import com.google.gson.Gson
import com.senierr.simple.app.SessionApplication
import io.reactivex.Observable

/**
 * Note模块
 *
 * @author zhouchunjie
 * @date 2018/9/22
 */
class NoteService {

    companion object {
        private const val API_NOTE_BASE = "https://api.bmob.cn/1/classes"
        private const val API_NOTE = "/note"
    }

    fun get(objectId: String): Observable<Note> {
        return SessionApplication.application.dataHttp
                .get("$API_NOTE/$objectId")
                .setBaseUrl(API_NOTE_BASE)
                .execute(BmobObjectConverter(Note::class.java))
    }

    fun getAll(): Observable<MutableList<Note>> {
        return SessionApplication.application.dataHttp
                .get(API_NOTE)
                .setBaseUrl(API_NOTE_BASE)
                .addUrlParam("order", "updatedAt")
                .execute(BmobArrayConverter(Note::class.java))
                .map {
                    it.results
                }
    }

    fun insert(content: String): Observable<BmobInsert> {
        val param = mapOf(Pair("content", content))
        return SessionApplication.application.dataHttp
                .post(API_NOTE)
                .setBaseUrl(API_NOTE_BASE)
                .setRequestBody4JSon(Gson().toJson(param))
                .execute(BmobObjectConverter(BmobInsert::class.java))
    }

    fun update(note: Note): Observable<BmobUpdate> {
        val param = mapOf(Pair("content", note.content))
        return SessionApplication.application.dataHttp
                .put("$API_NOTE/${note.objectId}")
                .setBaseUrl(API_NOTE_BASE)
                .setRequestBody4JSon(Gson().toJson(param))
                .execute(BmobObjectConverter(BmobUpdate::class.java))
    }

    fun delete(objectId: String): Observable<BmobDelete> {
        return SessionApplication.application.dataHttp
                .delete("$API_NOTE/$objectId")
                .setBaseUrl(API_NOTE_BASE)
                .execute(BmobObjectConverter(BmobDelete::class.java))
    }
}