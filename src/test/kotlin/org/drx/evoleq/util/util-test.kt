package org.drx.evoleq.util

import org.junit.Test

class UtilTest {

    @Test fun tailOfArrayList() {
        val list1 = arrayListOf<Int>(1,2,3)
        val tail1 = list1.tail()

        assert(tail1[0] == 2)
        assert(tail1[1] == 3)

        val list2 = arrayListOf<Int>()
        val tail2 = list2.tail()

        assert(tail2 == list2)

    }

}