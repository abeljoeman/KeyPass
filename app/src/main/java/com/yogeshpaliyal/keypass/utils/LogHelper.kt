package com.yogeshpaliyal.keypass.utils

/*
* @author Yogesh Paliyal
* techpaliyal@gmail.com
* https://techpaliyal.com
* created on 26-12-2020 20:21
*/

@JvmName("LogHelper")
fun Any?.systemOutPrint() = Unit

fun Any?.systemErrPrint() = Unit

fun Exception?.debugPrintStackTrace() = Unit

fun Throwable?.debugPrintStackTrace() = Unit

fun Any?.logD(tag: String?) = Unit

fun Any?.logE(tag: String?) = Unit

fun Any?.logI(tag: String?) = Unit

fun Any?.logV(tag: String?) = Unit

fun Any?.logW(tag: String?) = Unit
