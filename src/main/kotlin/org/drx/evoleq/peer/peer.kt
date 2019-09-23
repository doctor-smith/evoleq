/**
 * Copyright (c) 2018-2019 Dr. Florian Schmidt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drx.evoleq.peer

import org.drx.evoleq.coroutines.DuplicatorMessage
import org.drx.evoleq.coroutines.Receiver
import org.drx.evoleq.stub.Stub

interface Peer<D, I, O> : Stub<D>{
    /**
     * Receive input of type I
     * [Receiver]
     */
    val input: Receiver<I>
    /**
     * Manage peer;
     * Receive duplicator-messages - output type O
     * [Receiver], [DuplicatorMessage]
     */
    val manager: Receiver<DuplicatorMessage<O>>
}

