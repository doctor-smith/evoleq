package org.drx.evoleq.math

import org.drx.evoleq.coroutines.SuspendedFunctions
import org.drx.evoleq.coroutines.suspended
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.util.tail

suspend fun <S,T> process(first:(S)-> Evolving<T>, vararg steps: (T)-> Evolving<T>): (S)-> Evolving<T> =
    when(steps.isEmpty()) {
        true -> first
        false -> {
            val next = steps.first()
            val tail = arrayListOf(*steps).tail()
            process(first * next, tail)
        }
    }
tailrec suspend fun <S,T> process(first: (S)-> Evolving<T>, steps: ArrayList<(T)-> Evolving<T>>): (S)-> Evolving<T> =
    when (steps.isEmpty()) {
        true -> first
        false -> {
            val next = steps.first()
            val tail = steps.tail()
            process(first * next, tail)
        }
    }

suspend fun <S,T> process(first: suspend (S)-> Evolving<T>, vararg steps: (T)-> Evolving<T>): suspend (S)-> Evolving<T> =
    when(steps.isEmpty()) {
        true -> first
        false -> {
            val next = steps.first()
            val tail = arrayListOf(*steps).tail()
            process( suspended(first * next), tail)
        }
    }
tailrec suspend fun <S,T> process(first: suspend (S)-> Evolving<T>, steps: ArrayList<(T)-> Evolving<T>>):suspend (S)-> Evolving<T> =
    when (steps.isEmpty()) {
        true -> first
        false -> {
            val next = steps.first()
            val tail = steps.tail()
            process(suspended(first * next), tail)
        }
    }

suspend fun <S,T> process(first: (S)-> Evolving<T>, vararg steps: suspend (T)-> Evolving<T>): suspend(S)-> Evolving<T> =
    when(steps.isEmpty()) {
        true -> suspended ( first )
        false -> {
            val next = steps.first()
            val tail = SuspendedFunctions(*steps).tail()
            process(first * next, tail)
        }
    }
suspend fun <S,T> process(first: suspend (S)-> Evolving<T>, vararg steps: suspend (T)-> Evolving<T>): suspend(S)-> Evolving<T> =
    when(steps.isEmpty()) {
        true ->  first
        false -> {
            val next = steps.first()
            val tail = SuspendedFunctions(*steps).tail()
            process(first * next, tail)
        }
    }

tailrec suspend fun <S,T> process(first: suspend (S)-> Evolving<T>, steps: SuspendedFunctions<T, Evolving<T>>): suspend (S)-> Evolving<T> =
    when (steps.isEmpty()) {
        true -> first
        false -> {
            val next = steps.first()
            val tail = steps.tail()
            process(first * next, tail)
        }
    }

