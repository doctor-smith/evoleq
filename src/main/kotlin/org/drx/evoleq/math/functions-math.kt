package org.drx.evoleq.math


infix fun <R,S,T> ((R)->S).then(f:(S)->T): (R)->T = {r -> f(this(r))}
infix fun <R,S,T> ((R)->S).then(f:suspend (S)->T): suspend (R)->T = {r -> f(this(r))}
infix fun <R,S,T> (suspend (R)->S).then(f:(S)->T): suspend (R)->T = {r -> f(this(r))}
infix fun <R,S,T> (suspend(R)->S).then(f: suspend (S)->T): suspend (R)->T = {r -> f(this(r))}


infix fun <R,S,T> ((S)->T).after(f:(R)->S): (R)->T = {r -> this(f(r))}
infix fun <R,S,T> ((S)->T).after(f:suspend(R)->S): suspend (R)->T = {r -> this(f(r))}
infix fun <R,S,T> (suspend(S)->T).after(f:(R)->S): suspend (R)->T = {r -> this(f(r))}
infix fun <R,S,T> (suspend(S)->T).after(f:suspend(R)->S): suspend (R)->T = {r -> this(f(r))}