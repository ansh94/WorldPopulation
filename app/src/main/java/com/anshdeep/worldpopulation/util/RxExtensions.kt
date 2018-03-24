package com.anshdeep.worldpopulation.util

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by ansh on 23/03/18.
 */
operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
    add(disposable)
}