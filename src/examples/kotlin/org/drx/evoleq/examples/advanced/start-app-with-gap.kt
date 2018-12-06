package org.drx.evoleq.examples.advanced

import org.drx.evoleq.Immediate
import org.drx.evoleq.gap.Gap

interface AppStub {

}

data class Data(val preData: PreData, val appStub: AppStub)
data class PreData(val data: String)

val mainGap = Gap({ data: Data -> Immediate { data.preData } },
    { data -> { pd -> Immediate { data.copy(preData = pd) } } })

val appGap = Gap({ appStub: AppStub -> Immediate { appStub } },
    { appStub: AppStub -> { _ -> Immediate { appStub } } })

class App(gap: Gap<AppStub, () -> AppStub>) : tornadofx.App() {

    init{

    }


}